package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val character: String,          // e.g. "你好"
    val pinyin: String,             // e.g. "nǐ hǎo"
    val meaning: String,            // e.g. "Xin chào"
    val exampleCn: String = "",     // e.g. "你好！很高兴认识你。"
    val exampleVi: String = "",     // e.g. "Xin chào! Rất vui được gặp bạn."
    val category: String = "HSK 1", // HSK 1, HSK 2, Du Lịch, Giao Tiếp, Custom
    // Spaced Repetition (SuperMemo SM-2) state
    val interval: Int = 0,          // Inter-repetition interval in days
    val repetitions: Int = 0,       // Number of consecutive successful repetitions
    val easeFactor: Float = 2.5f,   // Ease factor, default to 2.5
    val nextReviewDate: Long = System.currentTimeMillis(), // Next review timestamp
    val level: Int = 1,             // Associated difficulty level
    val strength: Int = 0           // Memory percentage (0-100)
)

@Entity(tableName = "progress_stats")
data class ProgressStats(
    @PrimaryKey val id: Int = 1,    // Single row representation
    val userName: String = "Thành viên mới",
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 100,
    val streak: Int = 0,
    val lastStudyDate: Long = 0,
    val wordsMastered: Int = 0,
    val pronunciationScore: Float = 0.0f,
    val lastAssessmentDate: Long = 0,
    val lastAssessmentScore: Int = 0
)

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val authorAvatar: String = "avatar_1", // Vector resource index or id
    val content: String,
    val category: String = "Kinh nghiệm", // Thảo luận, Thành ngữ, Hỏi đáp, Tài liệu
    val likes: Int = 0,
    val replyCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_assessments")
data class WeeklyAssessment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val testName: String, // e.g., "Kiểm tra Tuần 1 - HSK 1"
    val score: Int,       // Percentage or scaled score
    val totalQuestions: Int,
    val dateCompleted: Long = System.currentTimeMillis(),
    val feedback: String   // Personalized text evaluation
)
