package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ChineseApp
import com.example.data.*
import com.example.network.EvaluationResult
import com.example.network.GeminiTutorHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.ceil

class StudyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudyRepository = (application as ChineseApp).repository

    // --- Database Flows ---
    val allFlashcards: StateFlow<List<Flashcard>> = repository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressStats: StateFlow<ProgressStats> = repository.progressStats
        .map { it ?: ProgressStats() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressStats())

    val communityPosts: StateFlow<List<CommunityPost>> = repository.communityPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyAssessments: StateFlow<List<WeeklyAssessment>> = repository.weeklyAssessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Theme State ---
    private val _isDarkTheme = MutableStateFlow(true) // Defaults to Dark Mode for eye strain reduction
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // --- Spaced Repetition Due Cards Flow ---
    val dueFlashcards: StateFlow<List<Flashcard>> = allFlashcards
        .map { list ->
            val now = System.currentTimeMillis()
            list.filter { it.nextReviewDate <= now }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Study Mode / Flashcard Stats ---
    private val _studySessionScore = MutableStateFlow(0)
    val studySessionScore: StateFlow<Int> = _studySessionScore.asStateFlow()

    // --- Speech Recognition States ---
    private var speechRecognizer: SpeechRecognizer? = null
    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _pronunciationFeedback = MutableStateFlow<EvaluationResult?>(null)
    val pronunciationFeedback: StateFlow<EvaluationResult?> = _pronunciationFeedback.asStateFlow()

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    // --- AI Personalized Tutor States ---
    private val _aiLessonContent = MutableStateFlow<String>("")
    val aiLessonContent: StateFlow<String> = _aiLessonContent.asStateFlow()

    private val _isGeneratingLesson = MutableStateFlow(false)
    val isGeneratingLesson: StateFlow<Boolean> = _isGeneratingLesson.asStateFlow()

    // --- Weekly Assessment States ---
    private val _currentQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuizQuestions: StateFlow<List<QuizQuestion>> = _currentQuizQuestions.asStateFlow()

    private val _quizCurrentIndex = MutableStateFlow(0)
    val quizCurrentIndex: StateFlow<Int> = _quizCurrentIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizSelectedAnswer = MutableStateFlow<Int?>(null)
    val quizSelectedAnswer: StateFlow<Int?> = _quizSelectedAnswer.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    // --- Reminder Settings State ---
    private val _reminderTime = MutableStateFlow("20:00")
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    private val _reminderEnabled = MutableStateFlow(true)
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    init {
        initSpeechRecognizer(application)
    }

    // --- Speech Recognizer Setup ---
    private fun initSpeechRecognizer(context: Context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _speechText.value = "Đang lắng nghe..."
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Lỗi audio"
                            SpeechRecognizer.ERROR_CLIENT -> "Lỗi ứng dụng"
                            SpeechRecognizer.ERROR_NETWORK -> "Lỗi kết nối mạng"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Không nhận dạng được giọng. Vùi lòng thử lại."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Hết thời gian phát âm"
                            else -> "Thiếu hỗ trợ động lực âm thanh"
                        }
                        _speechText.value = "Lỗi: $errorMsg"
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        _speechText.value = matches?.firstOrNull() ?: ""
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _speechText.value = matches.first()
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        _pronunciationFeedback.value = null
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN") // Chinese speech input
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent) ?: run {
                _speechText.value = "Không tìm thấy bộ nhận dạng giọng nói hệ thống. Xin sử dụng tính năng mô phỏng hoặc nhập text."
            }
        } catch (e: Exception) {
            _speechText.value = "Lỗi khởi động: ${e.localizedMessage}"
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun skipOrSimulateSpeechInput(simulatedPinyin: String) {
        _speechText.value = simulatedPinyin
        Toast.makeText(getApplication(), "Đã nhập tiếng bồi: $simulatedPinyin", Toast.LENGTH_SHORT).show()
    }

    // --- Voice Evaluation ---
    fun evaluatePronunciation(card: Flashcard) {
        val spoken = _speechText.value
        if (spoken.isEmpty() || spoken == "Đang lắng nghe...") {
            Toast.makeText(getApplication(), "Vui lòng thu âm trước khi đánh giá!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _isEvaluating.value = true
            val result = GeminiTutorHelper.evaluatePronunciation(
                targetChinese = card.character,
                targetPinyin = card.pinyin,
                recognizedText = spoken
            )
            _pronunciationFeedback.value = result
            _isEvaluating.value = false

            // Reward continuous speech practice with XP
            if (result.isCorrect) {
                addXp(15)
            } else {
                addXp(5) // Participation reward
            }
        }
    }

    // --- Spaced Repetition (SuperMemo-2 Algorithm) ---
    fun submitFlashcardScore(card: Flashcard, rating: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            var currentRepetitions = card.repetitions
            var currentEaseFactor = card.easeFactor
            var currentInterval = card.interval

            if (rating >= 3) {
                // Success
                when (currentRepetitions) {
                    0 -> currentInterval = 1
                    1 -> currentInterval = 3 // Standard interval tweaks
                    else -> currentInterval = ceil(currentInterval * currentEaseFactor).toInt()
                }
                currentRepetitions += 1
                // Update Ease Factor: EF'=EF+(0.1-(5-q)*(0.08+(5-q)*0.02))
                val qFactor = 5 - rating
                currentEaseFactor = (currentEaseFactor + (0.1f - qFactor * (0.08f + qFactor * 0.02f)))
                    .coerceAtLeast(1.3f)
            } else {
                // Failure - Reset SM2 state for next review cycle
                currentRepetitions = 0
                currentInterval = 1
                currentEaseFactor = (currentEaseFactor - 0.2f).coerceAtLeast(1.3f)
            }

            // Memory Strength calculation (decay approximation representation)
            val computedStrength = if (rating >= 3) {
                (85 + (rating - 3) * 5).coerceIn(40, 100)
            } else {
                20
            }

            val nextReviewTimestamp = now + (currentInterval * 86400000L) // in Days (1 day = 86,400,000ms)

            val updatedCard = card.copy(
                interval = currentInterval,
                repetitions = currentRepetitions,
                easeFactor = currentEaseFactor,
                nextReviewDate = nextReviewTimestamp,
                strength = computedStrength
            )

            withContext(Dispatchers.IO) {
                repository.updateFlashcard(updatedCard)
            }

            // Reward user with 10 XP for completing a flashcard evaluation, plus streak points
            addXp(10)
            
            // Increment Mastered Vocabs in Progress stats if strength reaches 90%
            if (computedStrength >= 90) {
                val stats = progressStats.value
                val updatedStats = stats.copy(
                    wordsMastered = stats.wordsMastered + 1
                )
                withContext(Dispatchers.IO) {
                    repository.insertOrUpdateProgress(updatedStats)
                }
            }

            Toast.makeText(
                getApplication(),
                if (rating >= 3) "Đã ghi nhớ! Lần học tiếp: $currentInterval ngày tới" else "Lỗi nhịp! Sẽ ôn lại vào ngày mai",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Spaced repetition fast-forward clock (Pro debugging utility)
    fun simulateFastForwardTime() {
        viewModelScope.launch {
            val list = allFlashcards.value
            val now = System.currentTimeMillis()
            var affectedCount = 0
            withContext(Dispatchers.IO) {
                list.forEach { card ->
                    if (card.nextReviewDate > now) {
                        repository.updateFlashcard(card.copy(nextReviewDate = now - 1000L))
                        affectedCount++
                    }
                }
            }
            Toast.makeText(
                getApplication(),
                "Hệ Thống: Đã tua nhanh thời gian! Có $affectedCount chữ Hán đến hạn ôn tập.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Add vocabulary word manually (allows customization)
    fun addCustomFlashcard(chinese: String, pinyin: String, meaning: String, category: String) {
        if (chinese.isEmpty() || pinyin.isEmpty() || meaning.isEmpty()) {
            Toast.makeText(getApplication(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            val card = Flashcard(
                character = chinese.trim(),
                pinyin = pinyin.trim(),
                meaning = meaning.trim(),
                category = category.trim().ifEmpty { "Tự chọn" },
                level = 1
            )
            withContext(Dispatchers.IO) {
                repository.insertFlashcard(card)
            }
            addXp(20)
            Toast.makeText(getApplication(), "Đã thêm từ '$chinese' thành công! (+20 EXP)", Toast.LENGTH_SHORT).show()
        }
    }

    // --- AI Generated Custom Lessons ---
    fun generateAILesson(categoryName: String) {
        viewModelScope.launch {
            _isGeneratingLesson.value = true
            _aiLessonContent.value = "AI Tutor đang biên soạn giáo án cá nhân hóa về chủ đề '$categoryName' cho bạn..."
            val prompt = """
                Hãy viết một giáo án học tiếng Trung về chủ đề '$categoryName'.
                Giáo án gồm 3 phần:
                1. Giới thiệu sơ lược văn hóa/bối cảnh giao tiếp.
                2. 5 từ vựng then chốt (kèm Phồn/Giản thể, Pinyin bản xứ, nghĩa tiếng Việt, câu ví dụ thực tế kèm Pinyin và dịch nghĩa câu).
                3. Một đoạn hội thoại ngắn 4 câu mẫu cực tiện dụng.
                Thể hiện thật đẹp mắt bằng Markdown, dễ thuộc và bổ ích!
            """.trimIndent()
            val text = GeminiTutorHelper.generateCustomLesson(prompt)
            _aiLessonContent.value = text
            _isGeneratingLesson.value = false
            addXp(15)
        }
    }

    // --- Weekly Placement Assessment / Weekly Quizzes ---
    fun startWeeklyQuiz() {
        _quizCompleted.value = false
        _quizCurrentIndex.value = 0
        _quizScore.value = 0
        _quizSelectedAnswer.value = null

        // Generate mock dynamic questions matching current character database
        val allCards = allFlashcards.value
        if (allCards.size < 4) {
            // Guarantee sample questions if user deleted everything or empty
            _currentQuizQuestions.value = generateFallbackQuiz()
            return
        }

        val shuffled = allCards.shuffled()
        val questions = shuffled.take(5).mapIndexed { idx, card ->
            // Create 3 incorrect choices
            val incorrectOptions = allCards.filter { it.id != card.id }
                .shuffled()
                .take(3)
                .map { it.meaning }
            val options = (incorrectOptions + card.meaning).shuffled()
            val correctIdx = options.indexOf(card.meaning)

            QuizQuestion(
                id = idx,
                questionText = "Chữ Hán '${card.character}' (phiên âm '${card.pinyin}') biểu thị nghĩa nào sau đây?",
                options = options,
                correctAnswerIndex = correctIdx,
                cardId = card.id
            )
        }
        _currentQuizQuestions.value = questions
    }

    private fun generateFallbackQuiz(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                0,
                "Chữ Hán '汉语' phát âm là Hànyǔ, nghĩa là gì?",
                listOf("Tiếng Trung", "Tiếng Anh", "Tiếng Hàn", "Tiếng Nhật"),
                0,
                -1
            ),
            QuizQuestion(
                1,
                "Từ '买单' (mǎi dān) hữu dụng trong bối cảnh du lịch nào?",
                listOf("Mua vé xe lửa", "Đặt phòng khách sạn", "Tính tiền thanh toán", "Hỏi đường đi bộ"),
                2,
                -1
            ),
            QuizQuestion(
                2,
                "Câu ví dụ '谢谢你的帮助！' dịch nghĩa thành gì?",
                listOf("Xin chào hẹn gặp lại!", "Cảm ơn sự giúp đỡ của bạn!", "Tôi muốn đi vệ sinh.", "Thức ăn ngon quá!"),
                1,
                -1
            ),
            QuizQuestion(
                3,
                "Thanh điệu trong Pinyin 'nǐ hǎo' biểu thị dấu gì?",
                listOf("Thanh 1 và Thanh 2", "Thanh 4 và Thanh 5", "Thanh 3 và Thanh 3", "Không dấu"),
                2,
                -1
            ),
            QuizQuestion(
                4,
                "Chữ Hán '医生' (yīshēng) đại diện cho ngành nghề nào?",
                listOf("Bác sĩ", "Giáo viên", "Cảnh sát", "Doanh nhân"),
                0,
                -1
            )
        )
    }

    fun selectQuizAnswer(optionIdx: Int) {
        if (_quizSelectedAnswer.value != null) return // Prevent duplicate tapping
        _quizSelectedAnswer.value = optionIdx
        val currentQ = _currentQuizQuestions.value[_quizCurrentIndex.value]
        if (optionIdx == currentQ.correctAnswerIndex) {
            _quizScore.value = _quizScore.value + 20 // Each correct answer = 20 pts (max 100)
            addXp(10)
        } else {
            addXp(2) // Fallback support XP
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = _quizCurrentIndex.value + 1
        if (nextIdx < _currentQuizQuestions.value.size) {
            _quizCurrentIndex.value = nextIdx
            _quizSelectedAnswer.value = null
        } else {
            // Completed quiz! Add test results to Room history & Progress
            _quizCompleted.value = true
            val finalScore = _quizScore.value
            val totalQ = _currentQuizQuestions.value.size

            viewModelScope.launch {
                val evalFeedback = when (finalScore) {
                    100 -> "Xuất sắc! Bạn đạt danh hiệu 'Trạng Nguyên Hoa Ngữ' tuần này."
                    80 -> "Giỏi lắm! Phát âm và nghĩa từ vựng của bạn vô cùng bài bản."
                    60 -> "Khá tốt. Hãy ôn tập lại các thẻ nhớ bị lỡ nhịp."
                    else -> "Nỗ lực hơn nữa nhé! Ôn luyện flashcards 5 phút mỗi ngày sẽ cải thiện điểm số."
                }

                val assessment = WeeklyAssessment(
                    testName = "Bài Kiểm Tra Tuần - Trình Độ ${progressStats.value.level}",
                    score = finalScore,
                    totalQuestions = totalQ,
                    feedback = evalFeedback
                )

                withContext(Dispatchers.IO) {
                    repository.insertAssessment(assessment)

                    // Update main progress stats
                    val stats = repository.getProgressStatsSync() ?: ProgressStats()
                    val xpReward = finalScore // XP reward equal to score percentage (e.g. 100 XP)
                    val updatedStats = stats.copy(
                        currentXp = stats.currentXp + xpReward,
                        lastAssessmentScore = finalScore,
                        lastAssessmentDate = System.currentTimeMillis()
                    )
                    repository.insertOrUpdateProgress(updatedStats)
                }
                
                // Let progress auto recalculate level
                recalculateLevel()
            }
        }
    }

    // --- Community Interactivity (Cộng đồng) ---
    fun submitPost(title: String, content: String, category: String) {
        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(getApplication(), "Tiêu đề và nội dung không được bỏ trống!", Toast.LENGTH_SHORT).show()
            return
        }
        viewModelScope.launch {
            val stats = progressStats.value
            val post = CommunityPost(
                title = title.trim(),
                content = content.trim(),
                category = category.trim().ifEmpty { "Thảo luận" },
                author = stats.userName,
                authorAvatar = "user_current"
            )
            withContext(Dispatchers.IO) {
                repository.insertPost(post)
            }
            addXp(30) // Posting rewards community engagement
            Toast.makeText(getApplication(), "Đã đăng bài viết của bạn lên cộng đồng! (+30 XP)", Toast.LENGTH_SHORT).show()
        }
    }

    fun likeCommunityPost(postId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.likePost(postId)
            }
            addXp(2) // Small reward for liking
        }
    }

    // --- Daily Study Reminder configuration ---
    fun saveReminderSettings(enabled: Boolean, time: String) {
        _reminderEnabled.value = enabled
        _reminderTime.value = time
        val status = if (enabled) "Đã Bật" else "Đã Tắt"
        Toast.makeText(
            getApplication(),
            "Đã cập nhật Nhắc Nhở Thông Minh: $status lúc $time hàng ngày!",
            Toast.LENGTH_LONG
        ).show()
    }

    // --- Profile Editing Tools ---
    fun updateUserName(name: String) {
        if (name.isEmpty()) return
        viewModelScope.launch {
            val current = progressStats.value
            val updated = current.copy(userName = name)
            withContext(Dispatchers.IO) {
                repository.insertOrUpdateProgress(updated)
            }
            Toast.makeText(getApplication(), "Đã đổi học danh thành $name!", Toast.LENGTH_SHORT).show()
        }
    }

    // Utility: Add points (EXP) & level progression
    private fun addXp(amount: Int) {
        viewModelScope.launch {
            val current = progressStats.value
            var newXp = current.currentXp + amount
            var updatedLevel = current.level
            var nextLevelXp = current.nextLevelXp

            if (newXp >= nextLevelXp) {
                newXp -= nextLevelXp
                updatedLevel += 1
                nextLevelXp = (nextLevelXp * 1.5).toInt() // Incremental difficulty
                Toast.makeText(getApplication(), "Chúc mừng! Bạn đã tăng cấp lên Trình độ $updatedLevel 🚀", Toast.LENGTH_LONG).show()
            }

            val updated = current.copy(
                currentXp = newXp,
                level = updatedLevel,
                nextLevelXp = nextLevelXp,
                streak = current.streak // Keep streak unchanged
            )

            withContext(Dispatchers.IO) {
                repository.insertOrUpdateProgress(updated)
            }
        }
    }

    private suspend fun recalculateLevel() {
        val current = repository.getProgressStatsSync() ?: return
        var level = current.level
        var xp = current.currentXp
        var nextLimit = current.nextLevelXp

        while (xp >= nextLimit) {
            xp -= nextLimit
            level += 1
            nextLimit = (nextLimit * 1.5).toInt()
        }

        val updated = current.copy(
            level = level,
            currentXp = xp,
            nextLevelXp = nextLimit
        )
        repository.insertOrUpdateProgress(updated)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

// Struct for quiz holding
data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val cardId: Int
)
