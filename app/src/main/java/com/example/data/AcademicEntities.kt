package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "academic_programs")
data class AcademicProgramEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val branch: String = "Electronics and Communication Engineering",
    val durationYears: Int = 3,
    val totalSemesters: Int = 6,
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "semesters")
data class SemesterEntity(
    @PrimaryKey val id: String,
    val programId: String,
    val semesterNumber: Int,
    val title: String,
    val description: String,
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val semesterId: String,
    val title: String,
    val description: String,
    val type: String, // "Theory", "Practical", "Elective"
    val difficulty: String = "Intermediate",
    val priority: String = "Medium",
    val prerequisites: String = "", // Comma separated IDs or JSON
    val tags: String = "",
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val unitNumber: Int,
    val title: String,
    val description: String,
    val priority: String = "Medium",
    val difficulty: String = "Intermediate",
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: String,
    val unitId: String,
    val title: String,
    val description: String,
    val contentEnglish: String,
    val contentHinglish: String = "",
    val contentHindi: String = "",
    val priority: String = "Medium",
    val difficulty: String = "Intermediate",
    val prerequisites: String = "",
    val tags: String = "",
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "subtopics")
data class SubTopicEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val contentEnglish: String,
    val contentHinglish: String = "",
    val contentHindi: String = "",
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val semesterId: String? = null,
    val subjectId: String? = null,
    val unitId: String? = null,
    val topicId: String? = null,
    val questionTextEnglish: String,
    val questionTextHinglish: String = "",
    val questionType: String,
    val difficulty: String = "Intermediate",
    val priority: String = "Medium",
    
    // Academic Evidence
    val sourceType: String = "PRACTICE", // VERIFIED, HISTORICAL, PRACTICE, PREDICTED, AI_GENERATED
    val sourceTitle: String = "",
    val sourceUrl: String = "",
    val sourceYear: String = "",
    val evidenceNotes: String = "",
    
    // Priority System
    val syllabusCentrality: Int = 0,
    val historicalRecurrence: Int = 0,
    val vivaRelevance: Int = 0,
    val prerequisiteImportance: Int = 0,
    val practicalRelevance: Int = 0,
    val predictionConfidence: Int = 0,
    
    // Answers & Explanations
    val shortAnswerEnglish: String = "",
    val shortAnswerHinglish: String = "",
    val conceptExplanationEnglish: String = "",
    val conceptExplanationHinglish: String = "",
    val fullAnswerEnglish: String = "",
    val fullAnswerHinglish: String = "",
    val hintEnglish: String = "",
    val hintHinglish: String = "",
    val formula: String = "",
    val commonMistakes: String = "",
    val realWorldApplication: String = "",
    val estimatedTime: Int = 0, // in seconds
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "question_options")
data class QuestionOptionEntity(
    @PrimaryKey val id: String,
    val questionId: String,
    val textEnglish: String,
    val textHinglish: String = "",
    val isCorrect: Boolean
)

@Entity(tableName = "question_attempts")
data class QuestionAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val questionId: String,
    val isCorrect: Boolean,
    val timeSpentMs: Long,
    val confidenceLevel: Int = 0,
    val attemptedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_bookmarks")
data class QuestionBookmarkEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val questionId: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_progress")
data class QuestionProgressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val topicId: String,
    val attemptCount: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val averageScore: Float = 0f,
    val masteryLevel: String = "NEW", // NEW, LEARNING, REVIEW, MASTERED
    val consecutiveCorrect: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "viva_sessions")
data class VivaSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subjectId: String?,
    val experimentId: String?,
    val score: Int = 0,
    val maxScore: Int = 0,
    val feedback: String = "",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "viva_turns")
data class VivaTurnEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val turnNumber: Int,
    val examinerQuestion: String,
    val studentAnswer: String,
    val evaluationStatus: String, // CORRECT, PARTIALLY_CORRECT, INCORRECT
    val feedback: String,
    val isFollowUp: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "formulas")
data class FormulaEntity(
    @PrimaryKey val id: String,
    val topicId: String?,
    val subjectId: String?,
    val title: String,
    val description: String,
    val formulaExpression: String,
    val variablesEnglish: String,
    val variablesHinglish: String = "",
    val tags: String = "",
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)


@Entity(tableName = "study_resources")
data class StudyResourceEntity(
    @PrimaryKey val id: String,
    val subjectId: String?,
    val title: String,
    val description: String,
    val type: String, // "PDF", "Video", "Link", "Paper"
    val url: String,
    val version: String,
    val source: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

@Entity(tableName = "career_paths")
data class CareerPathEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "Higher Education", "Government", "Private"
    val description: String,
    val requiredSkills: String, // Comma separated
    val active: Boolean = true
)

@Entity(tableName = "project_ideas")
data class ProjectIdeaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val semesterNumber: Int,
    val description: String,
    val requiredComponents: String,
    val requiredSkills: String,
    val active: Boolean = true
)

@Entity(tableName = "study_progress")
data class StudyProgressEntity(
    @PrimaryKey val id: String, // format: "userId_topicId" or just "topicId" for local
    val userId: String = "",
    val topicId: String,
    val completed: Boolean = false,
    val timeSpentMs: Long = 0,
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
