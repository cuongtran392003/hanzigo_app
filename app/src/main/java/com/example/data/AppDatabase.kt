package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Flashcard::class, ProgressStats::class, CommunityPost::class, WeeklyAssessment::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chinese_study_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.studyDao())
                }
            }
        }

        suspend fun populateDatabase(dao: StudyDao) {
            // Check if already populated to prevent duplicate inserts
            if (dao.getCount() > 0) return

            // 1. Core Seed Flashcards (HSK 1, Du lịch, Giao tiếp)
            val initialCards = listOf(
                Flashcard(
                    character = "你好",
                    pinyin = "nǐ hǎo",
                    meaning = "Xin chào (Chào bạn)",
                    exampleCn = "你好！很高兴认识你。",
                    exampleVi = "Xin chào! Rất vui được gặp bạn.",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "谢谢",
                    pinyin = "xièxie",
                    meaning = "Cảm ơn",
                    exampleCn = "谢谢你的帮助！",
                    exampleVi = "Cảm ơn sự giúp đỡ của bạn!",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "再见",
                    pinyin = "zàijiàn",
                    meaning = "Tạm biệt",
                    exampleCn = "明天见，再见！",
                    exampleVi = "Mai gặp lại, tạm biệt nhé!",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "学习",
                    pinyin = "xuéxí",
                    meaning = "Học tập (Học)",
                    exampleCn = "我们每天都努力学习汉语。",
                    exampleVi = "Chúng tôi mỗi ngày đều nỗ lực học tiếng Trung.",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "汉语",
                    pinyin = "Hànyǔ",
                    meaning = "Tiếng Trung (Hán ngữ)",
                    exampleCn = "我想掌握流利的汉语学府。",
                    exampleVi = "Tôi muốn thành thạo tiếng Trung trôi chảy.",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "喜欢",
                    pinyin = "xǐhuan",
                    meaning = "Thích",
                    exampleCn = "你喜欢吃中国菜吗？",
                    exampleVi = "Bạn có thích ăn món ăn Trung Quốc không?",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "朋友",
                    pinyin = "péngyou",
                    meaning = "Bạn bè (Bạn)",
                    exampleCn = "他是我非常要好的朋友。",
                    exampleVi = "Anh ấy là người bạn rất thân của tôi.",
                    category = "HSK 1",
                    level = 1
                ),
                Flashcard(
                    character = "买单",
                    pinyin = "mǎi dān",
                    meaning = "Thanh toán (Tính tiền)",
                    exampleCn = "服务员, 我要买单。",
                    exampleVi = "Phục vụ ơi, tôi muốn tính tiền.",
                    category = "Du Lịch",
                    level = 1
                ),
                Flashcard(
                    character = "多少钱",
                    pinyin = "duōshao qián",
                    meaning = "Bao nhiêu tiền",
                    exampleCn = "这个红色的包包多少钱？",
                    exampleVi = "Cái túi xách màu đỏ này bao nhiêu tiền?",
                    category = "Du Lịch",
                    level = 1
                ),
                Flashcard(
                    character = "在哪里",
                    pinyin = "zài nǎlǐ",
                    meaning = "Ở đâu",
                    exampleCn = "请问，洗手间在哪里？",
                    exampleVi = "Xin hỏi, nhà vệ sinh ở đâu vậy?",
                    category = "Du Lịch",
                    level = 1
                ),
                Flashcard(
                    character = "加油",
                    pinyin = "jiāyóu",
                    meaning = "Cố lên (Thêm dầu)",
                    exampleCn = "这次考试一定要加油啊！",
                    exampleVi = "Kỳ thi lần này nhất định phải cố lên nhé!",
                    category = "Giao Tiếp",
                    level = 2
                ),
                Flashcard(
                    character = "没关系",
                    pinyin = "méi guānxi",
                    meaning = "Không sao đâu (Không có gì)",
                    exampleCn = "没关系，下次再努力就行。",
                    exampleVi = "Không sao đâu, lần sau nỗ lực nữa là được.",
                    category = "Giao Tiếp",
                    level = 1
                ),
                Flashcard(
                    character = "放心",
                    pinyin = "fàngxīn",
                    meaning = "Yên tâm",
                    exampleCn = "你放心，这件事交给我办。",
                    exampleVi = "Bạn yên tâm, việc này cứ giao tôi làm.",
                    category = "Giao Tiếp",
                    level = 2
                )
            )
            dao.insertFlashcards(initialCards)

            // 2. Pre-populate ProgressStats
            dao.insertOrUpdateProgress(
                ProgressStats(
                    id = 1,
                    userName = "Học viên Hoa Ngữ",
                    level = 1,
                    currentXp = 10,
                    nextLevelXp = 100,
                    streak = 3,
                    lastStudyDate = System.currentTimeMillis() - 86400000, // Yesterday
                    wordsMastered = 3,
                    pronunciationScore = 85.0f
                )
            )

            // 3. Pre-populate beautiful Chinese idiom and educational community posts
            val basePosts = listOf(
                CommunityPost(
                    title = "Thành Ngữ 'Tái Ông Thất Mã' (塞翁失马)",
                    author = "Cô giáo Vương Lệ",
                    authorAvatar = "teacher_wang",
                    content = """
                        Câu thành ngữ nổi tiếng '塞翁失马, 焉知非福' (Tái ông mất ngựa, họa phúc khôn lường) khuyên chúng ta hãy giữ tâm thế bình tĩnh trước biến cố cuộc đời. 
                        
                        ✨ Trong quá trình học tiếng Trung cũng vậy, đôi khi việc phát âm sai hay thi trượt một điểm thi là thử thách tạm thời, giúp bạn nhìn nhận lại cách học để tiến bộ rực rỡ hơn trong tương lai. Hãy kiên trì mỗi ngày cùng Hoa Ngữ Smartech nhé!
                    """.trimIndent(),
                    category = "Thành ngữ",
                    likes = 42,
                    replyCount = 5
                ),
                CommunityPost(
                    title = "Bí quyết nhớ 214 Bộ Thủ nhanh nhất!",
                    author = "Minh Tuấn (HSK 6)",
                    authorAvatar = "user_tuan",
                    content = """
                        Xin chào mọi người! Mình vừa đỗ HSK 6 tháng trước và muốn chia sẻ bí quyết học. Thay vì học vẹt 214 Bộ Thủ một cách rời rạc, các bạn hãy ghép chúng thành những câu chuyện hình dung hài hước. 
                        
                        Ví dụ: Bộ Nhân (人 - Người) đứng cạnh bộ Mộc (木 - Cây) tạo thành chữ Hưu (休 - Nghỉ ngơi), vì người tựa vào cây chính là đang nghỉ ngơi! Chúc các bạn áp dụng thành công và đạt điểm cao trong học tập!
                    """.trimIndent(),
                    category = "Kinh nghiệm",
                    likes = 89,
                    replyCount = 14
                ),
                CommunityPost(
                    title = "Thành Ngữ 'Ếch Ngồi Đáy Giếng' (井底之蛙)",
                    author = "Thầy Trần Hải",
                    authorAvatar = "teacher_tran",
                    content = """
                        Hôm nay chúng ta cùng học thành ngữ '井底之蛙' (Jǐng dǐ zhī wā) nói về chú ếch luôn huênh hoang coi trời bằng vung. 
                        
                        💡 Bài học cốt lõi: Thế giới ngôn ngữ vô cùng kỳ diệu, học tiếng Trung không chỉ học từ vựng mà là khám phá văn hóa rực rỡ. Đừng hài lòng với vốn từ hạn hẹp mà hãy liên tục thử thách bản thân bằng cách trò chuyện với AI Tutor, làm các bài test tuần để mở rộng giới hạn tri thức nhé.
                    """.trimIndent(),
                    category = "Thành ngữ",
                    likes = 55,
                    replyCount = 8
                )
            )
            for (p in basePosts) {
                dao.insertPost(p)
            }
        }
    }
}
