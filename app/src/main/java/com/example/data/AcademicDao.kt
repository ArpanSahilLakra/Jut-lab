package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {
    @Query("SELECT * FROM academic_programs WHERE active = 1")
    fun getAllPrograms(): Flow<List<AcademicProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<AcademicProgramEntity>)

    @Query("SELECT * FROM semesters WHERE programId = :programId AND active = 1 ORDER BY semesterNumber ASC")
    fun getSemestersForProgram(programId: String): Flow<List<SemesterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemesters(semesters: List<SemesterEntity>)

    @Query("SELECT * FROM subjects WHERE semesterId = :semesterId AND active = 1")
    fun getSubjectsForSemester(semesterId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("SELECT * FROM units WHERE subjectId = :subjectId AND active = 1 ORDER BY unitNumber ASC")
    fun getUnitsForSubject(subjectId: String): Flow<List<UnitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Query("SELECT * FROM topics WHERE unitId = :unitId AND active = 1")
    fun getTopicsForUnit(unitId: String): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Query("SELECT * FROM subtopics WHERE topicId = :topicId AND active = 1")
    fun getSubTopicsForTopic(topicId: String): Flow<List<SubTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTopics(subtopics: List<SubTopicEntity>)

    @Query("SELECT * FROM questions WHERE subjectId = :subjectId AND active = 1")
    fun getQuestionsForSubject(subjectId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE semesterId = :semesterId AND active = 1")
    fun getQuestionsForSemester(semesterId: String): Flow<List<QuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Query("SELECT * FROM formulas WHERE subjectId = :subjectId AND active = 1")
    fun getFormulasForSubject(subjectId: String): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormulas(formulas: List<FormulaEntity>)
    
    @Query("SELECT * FROM study_resources WHERE subjectId = :subjectId AND active = 1")
    fun getStudyResourcesForSubject(subjectId: String): Flow<List<StudyResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyResources(resources: List<StudyResourceEntity>)

    @Query("SELECT * FROM career_paths WHERE active = 1")
    fun getCareerPaths(): Flow<List<CareerPathEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareerPaths(careerPaths: List<CareerPathEntity>)

    @Query("SELECT * FROM project_ideas WHERE semesterNumber = :semesterNumber AND active = 1")
    fun getProjectIdeasForSemester(semesterNumber: Int): Flow<List<ProjectIdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectIdeas(projectIdeas: List<ProjectIdeaEntity>)

    @Query("SELECT * FROM study_progress WHERE topicId = :topicId AND userId = :userId")
    fun getTopicProgress(topicId: String, userId: String = ""): Flow<StudyProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyProgress(progress: StudyProgressEntity)

    @Query("SELECT * FROM study_progress WHERE userId = :userId")
    fun getAllStudyProgress(userId: String = ""): Flow<List<StudyProgressEntity>>


    @Query("SELECT * FROM question_options WHERE questionId = :questionId")
    fun getOptionsForQuestion(questionId: String): Flow<List<QuestionOptionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionOptions(options: List<QuestionOptionEntity>)

    @Query("SELECT * FROM question_attempts WHERE userId = :userId AND questionId = :questionId ORDER BY attemptedAt DESC")
    fun getAttemptsForQuestion(userId: String, questionId: String): Flow<List<QuestionAttemptEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionAttempt(attempt: QuestionAttemptEntity)

    @Query("SELECT * FROM question_bookmarks WHERE userId = :userId")
    fun getQuestionBookmarks(userId: String): Flow<List<QuestionBookmarkEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionBookmark(bookmark: QuestionBookmarkEntity)

    @Query("SELECT * FROM question_progress WHERE userId = :userId AND topicId = :topicId")
    fun getQuestionProgress(userId: String, topicId: String): Flow<QuestionProgressEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionProgress(progress: QuestionProgressEntity)

    @Query("SELECT * FROM viva_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getVivaSessions(userId: String): Flow<List<VivaSessionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVivaSession(session: VivaSessionEntity)

    @Query("SELECT * FROM viva_turns WHERE sessionId = :sessionId ORDER BY turnNumber ASC")
    fun getVivaTurns(sessionId: String): Flow<List<VivaTurnEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVivaTurn(turn: VivaTurnEntity)
}


