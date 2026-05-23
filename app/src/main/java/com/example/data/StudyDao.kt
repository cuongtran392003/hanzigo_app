package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    // --- Flashcard Queries ---
    @Query("SELECT * FROM flashcards ORDER BY id ASC")
    fun getAllFlashcards(): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE nextReviewDate <= :currentTime ORDER BY nextReviewDate ASC")
    fun getDueFlashcards(currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE category = :category ORDER BY id ASC")
    fun getFlashcardsByCategory(category: String): Flow<List<Flashcard>>

    @Query("SELECT DISTINCT category FROM flashcards")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getCount(): Int

    // --- Progress Stats Queries ---
    @Query("SELECT * FROM progress_stats WHERE id = 1 LIMIT 1")
    fun getProgressStatsFlow(): Flow<ProgressStats?>

    @Query("SELECT * FROM progress_stats WHERE id = 1 LIMIT 1")
    suspend fun getProgressStats(): ProgressStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(stats: ProgressStats)

    // --- Community Queries ---
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getCommunityPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Query("UPDATE community_posts SET likes = likes + 1 WHERE id = :postId")
    suspend fun likePost(postId: Int)

    // --- Weekly Assessment Queries ---
    @Query("SELECT * FROM weekly_assessments ORDER BY dateCompleted DESC")
    fun getWeeklyAssessments(): Flow<List<WeeklyAssessment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: WeeklyAssessment)
}
