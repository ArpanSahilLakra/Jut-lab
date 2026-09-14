import re

with open('app/src/main/java/com/example/data/AcademicEntities.kt', 'r') as f:
    content = f.read()

# Replace QuestionEntity completely
new_question_entity = """@Entity(tableName = "questions")
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
)"""

content = re.sub(
    r'@Entity\(tableName = "questions"\)\s*data class QuestionEntity\(.*?val active: Boolean = true\n\)',
    new_question_entity,
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/data/AcademicEntities.kt', 'w') as f:
    f.write(content)
