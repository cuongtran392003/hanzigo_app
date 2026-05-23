package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {
    val allFlashcards: Flow<List<Flashcard>> = studyDao.getAllFlashcards()
    val progressStats: Flow<ProgressStats?> = studyDao.getProgressStatsFlow()
    val communityPosts: Flow<List<CommunityPost>> = studyDao.getCommunityPosts()
    val weeklyAssessments: Flow<List<WeeklyAssessment>> = studyDao.getWeeklyAssessments()
    val categories: Flow<List<String>> = studyDao.getCategories()

    fun getDueFlashcards(currentTime: Long): Flow<List<Flashcard>> = 
        studyDao.getDueFlashcards(currentTime)

    fun getFlashcardsByCategory(category: String): Flow<List<Flashcard>> =
        studyDao.getFlashcardsByCategory(category)

    suspend fun insertFlashcard(flashcard: Flashcard) = 
        studyDao.insertFlashcard(flashcard)

    suspend fun updateFlashcard(flashcard: Flashcard) = 
        studyDao.updateFlashcard(flashcard)

    suspend fun getProgressStatsSync(): ProgressStats? = 
        studyDao.getProgressStats()

    suspend fun insertOrUpdateProgress(stats: ProgressStats) = 
        studyDao.insertOrUpdateProgress(stats)

    suspend fun insertPost(post: CommunityPost) = 
        studyDao.insertPost(post)

    suspend fun likePost(postId: Int) = 
        studyDao.likePost(postId)

    suspend fun insertAssessment(assessment: WeeklyAssessment) = 
        studyDao.insertAssessment(assessment)
}
