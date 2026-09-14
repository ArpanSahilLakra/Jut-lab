package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val quizTitle: String,
  val score: Int,
  val totalQuestions: Int,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "experiment_progress")
data class ExperimentProgressEntity(
  @PrimaryKey val experimentId: String,
  val userId: String = "",
  val title: String = "",
  val theoryCompleted: Boolean = false,
  val circuitCompleted: Boolean = false,
  val simulationCompleted: Boolean = false,
  val observationCompleted: Boolean = false,
  val calculationCompleted: Boolean = false,
  val vivaCompleted: Boolean = false,
  val reportCompleted: Boolean = false,
  val overallProgress: Int = 0,
  val startedAt: Long = System.currentTimeMillis(),
  val lastOpenedAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null
)

@Entity(tableName = "experiments")
data class ExperimentEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val aim: String,
  val apparatusCommaSeparated: String,
  val theory: String,
  val formula: String,
  val category: String,
  val semester: Int = 0,
  val subjectId: String = "",
  val priority: String = "Medium",
  val difficulty: String = "Intermediate",
  val prerequisites: String = "",
  val tags: String = "",
  val source: String = "JUT/XIPT Academic Document",
  val version: String = "1.0",
  val lastUpdated: Long = System.currentTimeMillis(),
  val active: Boolean = true
)

@Entity(tableName = "viva_voce")
data class VivaVoceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val experimentId: String,
  val userId: String = "",
  val question: String,
  val optionA: String,
  val optionB: String,
  val optionC: String,
  val optionD: String,
  val correctOptionIndex: Int,
  val hinglishExplanation: String,
  val subjectId: String = "",
  val topicId: String = "",
  val difficulty: String = "Intermediate",
  val type: String = "Basic",
  val tags: String = "",
  val source: String = "JUT/XIPT Academic Document",
  val version: String = "1.0",
  val lastUpdated: Long = System.currentTimeMillis(),
  val active: Boolean = true
)

@Dao
interface LabDao {
  @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
  fun getAllQuizResults(): Flow<List<QuizResultEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQuizResult(result: QuizResultEntity)

  @Query("SELECT * FROM experiment_progress")
  fun getAllProgress(): Flow<List<ExperimentProgressEntity>>

  @Query("SELECT * FROM experiment_progress WHERE userId = :userId")
  fun getProgressForUser(userId: String): Flow<List<ExperimentProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProgress(progress: ExperimentProgressEntity)

  @Query("SELECT * FROM experiments")
  fun getAllExperiments(): Flow<List<ExperimentEntity>>

  @Query("SELECT * FROM experiments WHERE id = :expId")
  suspend fun getExperimentById(expId: String): ExperimentEntity?

  @Query("SELECT * FROM viva_voce WHERE experimentId = :expId")
  suspend fun getVivaQuestionsForExperiment(expId: String): List<VivaVoceEntity>
  @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
  suspend fun getPendingSyncOperations(): List<SyncQueueEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSyncOperation(operation: SyncQueueEntity)

  @Query("UPDATE sync_queue SET status = :status, attemptCount = attemptCount + 1, lastAttemptAt = :timestamp, errorMessage = :error WHERE id = :id")
  suspend fun updateSyncStatus(id: String, status: String, timestamp: Long, error: String?)

  @Query("DELETE FROM sync_queue WHERE id = :id")
  suspend fun deleteSyncOperation(id: String)

  @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
  fun getAllAssignments(): Flow<List<AssignmentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssignments(assignments: List<AssignmentEntity>)

  @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
  fun getRecentActivities(userId: String, limit: Int = 10): Flow<List<ActivityEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivity(activity: ActivityEntity)
  @Query("SELECT * FROM teacher_student_summaries ORDER BY xp DESC")
  fun getAllStudentSummaries(): Flow<List<StudentSummaryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudentSummaries(summaries: List<StudentSummaryEntity>)

  @Query("SELECT * FROM teacher_submissions ORDER BY submittedAt DESC")
  fun getAllTeacherSubmissions(): Flow<List<SubmissionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeacherSubmissions(submissions: List<SubmissionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExperiments(experiments: List<ExperimentEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVivaQuestions(questions: List<VivaVoceEntity>)

  @Query("SELECT * FROM student_profile LIMIT 1")
  fun getStudentProfile(): Flow<StudentProfileEntity?>

  @Query("SELECT * FROM student_profile WHERE id = :userId LIMIT 1")
  fun getStudentProfileById(userId: String): Flow<StudentProfileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertStudentProfile(profile: StudentProfileEntity)

  @Query("SELECT * FROM lab_reports ORDER BY updatedAt DESC")
  fun getAllLabReports(): Flow<List<LabReportEntity>>

  @Query("SELECT * FROM lab_reports WHERE userId = :userId ORDER BY updatedAt DESC")
  fun getLabReportsForUser(userId: String): Flow<List<LabReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertLabReport(report: LabReportEntity)

  @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
  fun getAllBookmarks(): Flow<List<BookmarkEntity>>

  @Query("SELECT * FROM bookmarks WHERE userId = :userId ORDER BY createdAt DESC")
  fun getBookmarksForUser(userId: String): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity)

  @Delete
  suspend fun deleteBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM student_profile")
  suspend fun clearProfiles()
  
  @Query("DELETE FROM experiment_progress")
  suspend fun clearProgress()
  
  @Query("DELETE FROM lab_reports")
  suspend fun clearReports()
  
  @Query("DELETE FROM bookmarks")
  suspend fun clearBookmarks()
  
  @Query("DELETE FROM sync_queue")
  suspend fun clearSyncQueue()
  
  @Query("DELETE FROM activities")
  suspend fun clearActivities()
  
  @Transaction
  suspend fun clearUserSpecificData() {
      clearProfiles()
      clearProgress()
      clearReports()
      clearBookmarks()
      clearSyncQueue()
      clearActivities()
  }

}

@Database(
  entities = [
    QuizResultEntity::class, 
    ExperimentProgressEntity::class,
    SyncQueueEntity::class,
    AssignmentEntity::class,
    ActivityEntity::class,
    StudentSummaryEntity::class,
    SubmissionEntity::class, 
    ExperimentEntity::class, 
    VivaVoceEntity::class,
    StudentProfileEntity::class,
    BookmarkEntity::class,
    LabReportEntity::class,
    
    AcademicProgramEntity::class,
    SemesterEntity::class,
    SubjectEntity::class,
    UnitEntity::class,
    TopicEntity::class,
    SubTopicEntity::class,
    QuestionEntity::class,
    FormulaEntity::class,
    StudyResourceEntity::class,
    CareerPathEntity::class,
    ProjectIdeaEntity::class,
    StudyProgressEntity::class,

    QuestionOptionEntity::class,
    QuestionAttemptEntity::class,
    QuestionBookmarkEntity::class,
    QuestionProgressEntity::class,
    VivaSessionEntity::class,
    VivaTurnEntity::class
  ],
  version = 5,
  exportSchema = false
)
abstract class LabDatabase : RoomDatabase() {
  abstract fun labDao(): LabDao
  abstract fun academicDao(): AcademicDao

  companion object {
    @Volatile
    private var INSTANCE: LabDatabase? = null

    fun getDatabase(context: Context): LabDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          LabDatabase::class.java,
          "jut_ece_lab_database"
        )
          .fallbackToDestructiveMigration(true)
          .addCallback(LabDatabaseCallback())
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class LabDatabaseCallback : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          CoroutineScope(Dispatchers.IO).launch {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(database: LabDatabase) {
      val academicDao = database.academicDao()

        
        
        // --- SEMESTER 1 ---
        val sem1 = SemesterEntity(
            id = "sem_1",
            programId = "prog_ece",
            semesterNumber = 1,
            title = "Semester 1",
            description = "Common Foundation",
            version = "NEP-2023",
            source = "JUT"
        )
        academicDao.insertSemesters(listOf(sem1))
        
                // --- SUBJECTS ---
        val math = SubjectEntity(
            id = "sub_bsm01",
            semesterId = "sem_1",
            title = "BSM01 - Engineering Mathematics I",
            description = "Calculus, Matrix Algebra, and Trignometry.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val physics = SubjectEntity(
            id = "sub_bsp01",
            semesterId = "sem_1",
            title = "BSP01 - Engineering Physics",
            description = "Basic physics for engineering.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val electrical = SubjectEntity(
            id = "sub_esee1",
            semesterId = "sem_1",
            title = "ESEE1 - Basics of Electrical Engineering",
            description = "Fundamentals of electrical circuits.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val mechanics = SubjectEntity(
            id = "sub_esem1",
            semesterId = "sem_1",
            title = "ESEM1 - Engineering Mechanics",
            description = "Statics and dynamics of rigid bodies.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val programming = SubjectEntity(
            id = "sub_espp1",
            semesterId = "sem_1",
            title = "ESPP1 - Programming for Problem Solving",
            description = "Fundamentals of C programming.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val iks = SubjectEntity(
            id = "sub_hsm01",
            semesterId = "sem_1",
            title = "HSM01 - Indian Knowledge System",
            description = "Traditional Indian scientific and cultural concepts.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        val dataVis = SubjectEntity(
            id = "sub_vsc01",
            semesterId = "sem_1",
            title = "VSC01 - Data Visualization and Pre-processing",
            description = "Data structures, cleaning, and charting.",
            type = "Theory",
            version = "NEP-2023",
            source = "JUT"
        )
        academicDao.insertSubjects(listOf(math, physics, electrical, mechanics, programming, iks, dataVis))

        
        // --- QUESTIONS ---
        val q1 = QuestionEntity(
            id = "q_sem1_phys_1",
            semesterId = "sem_1",
            subjectId = "sub_bsp01",
            unitId = "u_bsp01_1",
            topicId = "t_bsp01_1_1",
            questionTextEnglish = "What is the SI system and why is dimensional analysis useful?",
            questionType = "SHORT_ANSWER",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "HISTORICAL",
            evidenceNotes = "Extracted from verified JUT ECE Master Bank Research Edition.",
            fullAnswerEnglish = "SI provides internationally standardized base and derived units. Dimensional analysis checks dimensional consistency, helps derive relationships and supports unit conversion. It cannot by itself determine dimensionless constants or guarantee that an equation is physically complete."
        )
        val q2 = QuestionEntity(
            id = "q_sem1_phys_2",
            semesterId = "sem_1",
            subjectId = "sub_bsp01",
            questionTextEnglish = "Differentiate accuracy, precision and resolution.",
            questionType = "COMPARE",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "Accuracy describes closeness to the true value; precision describes repeatability; resolution is the smallest change an instrument can distinguish. A measurement can be precise but inaccurate if a systematic error exists."
        )
        val q3 = QuestionEntity(
            id = "q_sem1_phys_3",
            semesterId = "sem_1",
            subjectId = "sub_bsp01",
            questionTextEnglish = "What is percentage error?",
            questionType = "DEFINITION",
            difficulty = "Intermediate",
            priority = "Important",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "Percentage error is generally |measured − true|/|true| × 100%. For an experimental quantity calculated from several measured values, uncertainty/error propagation depends on the mathematical form of the result."
        )
        val q4 = QuestionEntity(
            id = "q_sem1_ee_1",
            semesterId = "sem_1",
            subjectId = "sub_esee1",
            questionTextEnglish = "State Ohm's law and its limitation.",
            questionType = "CONCEPTUAL",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "For an ohmic element under constant physical conditions, V = IR. It is not universally valid for nonlinear devices such as diodes, lamps or transistors over their entire operating range."
        )
        val q5 = QuestionEntity(
            id = "q_sem1_ee_2",
            semesterId = "sem_1",
            subjectId = "sub_esee1",
            questionTextEnglish = "Why is a multimeter connected differently for voltage and current?",
            questionType = "WHY_HOW",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "A voltmeter is connected in parallel and should have high input resistance; an ammeter is connected in series and should have low resistance. Connecting an ammeter directly across a voltage source can create a near-short circuit."
        )
        val q6 = QuestionEntity(
            id = "q_sem1_phys_4",
            semesterId = "sem_1",
            subjectId = "sub_bsp01",
            questionTextEnglish = "What is the difference between conductor, semiconductor and insulator?",
            questionType = "COMPARE",
            difficulty = "Intermediate",
            priority = "Important",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "The distinction is based largely on energy-band structure and carrier availability. Semiconductors have an intermediate band gap and conductivity that can be strongly controlled by temperature, illumination and doping."
        )
        val q7 = QuestionEntity(
            id = "q_sem1_phys_5",
            semesterId = "sem_1",
            subjectId = "sub_bsp01",
            unitId = "u_bsp01_2",
            topicId = "t_bsp01_2_1",
            questionTextEnglish = "What is a semiconductor? What are intrinsic and extrinsic semiconductors?",
            questionType = "DEFINITION",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "An intrinsic semiconductor is ideally pure and has thermally generated electron-hole pairs. An extrinsic semiconductor is intentionally doped with donor or acceptor impurities to increase either electron or hole concentration."
        )
        val q8 = QuestionEntity(
            id = "q_sem1_ee_3",
            semesterId = "sem_1",
            subjectId = "sub_esee1",
            questionTextEnglish = "Why is earthing necessary in an electrical laboratory?",
            questionType = "WHY_HOW",
            difficulty = "Intermediate",
            priority = "Important",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "Protective earthing provides a low-impedance path for fault current so exposed conductive parts do not remain dangerously energized. It also helps protective devices operate quickly during faults."
        )
        val q9 = QuestionEntity(
            id = "q_sem1_ee_4",
            semesterId = "sem_1",
            subjectId = "sub_esee1",
            questionTextEnglish = "Why should the resistance range of a multimeter not be used on a powered circuit?",
            questionType = "QUICK_VIVA",
            difficulty = "Intermediate",
            priority = "Viva",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "Resistance measurement uses the meter's internal test source. An externally energized circuit can produce incorrect readings or damage the instrument."
        )
        val q10 = QuestionEntity(
            id = "q_sem1_ee_5",
            semesterId = "sem_1",
            subjectId = "sub_esee1",
            questionTextEnglish = "What is a fuse?",
            questionType = "DEFINITION",
            difficulty = "Basic",
            priority = "Remember",
            sourceType = "HISTORICAL",
            fullAnswerEnglish = "A fuse is an overcurrent protection device designed to open the circuit when current exceeds its rated value for sufficient time, limiting damage and fire risk."
        )
        
        academicDao.insertQuestions(listOf(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10))

        // --- UNITS & TOPICS FOR BSP01 (Physics) ---
        val physUnit1 = UnitEntity(
            id = "u_bsp01_1", subjectId = "sub_bsp01", unitNumber = 1,
            title = "Measurements & Units", description = "SI units, errors, accuracy.",
            version = "NEP-2023", source = "JUT"
        )
        val physUnit2 = UnitEntity(
            id = "u_bsp01_2", subjectId = "sub_bsp01", unitNumber = 2,
            title = "Semiconductor Physics", description = "Conductors, semiconductors, insulators.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(physUnit1, physUnit2))
        
        val physTopic1 = TopicEntity(
            id = "t_bsp01_1_1", unitId = "u_bsp01_1",
            title = "SI System & Errors", description = "Dimensional analysis, percentage error.",
            contentEnglish = "The SI system is the standard. Errors can be absolute or relative.",
            version = "NEP-2023", source = "JUT"
        )
        val physTopic2 = TopicEntity(
            id = "t_bsp01_2_1", unitId = "u_bsp01_2",
            title = "Energy Bands", description = "Intrinsic and extrinsic semiconductors.",
            contentEnglish = "Semiconductors have a narrow band gap.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(physTopic1, physTopic2))

        // --- UNITS & TOPICS FOR BSM01 (Math I) ---
        val mathUnit1 = UnitEntity(
            id = "u_bsm01_1", subjectId = "sub_bsm01", unitNumber = 1,
            title = "Algebra & Matrices", description = "Determinants, Matrix algebra.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(mathUnit1))
        
        val mathTopic1 = TopicEntity(
            id = "t_bsm01_1_1", unitId = "u_bsm01_1",
            title = "Matrix Inverse", description = "Finding the inverse of a 3x3 matrix.",
            contentEnglish = "A matrix inverse A^-1 exists if det(A) != 0.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(mathTopic1))
        
        // --- ADDING BSM01 QUESTIONS ---
        val mq1 = QuestionEntity(
            id = "q_sem1_math_1",
            semesterId = "sem_1",
            subjectId = "sub_bsm01",
            unitId = "u_bsm01_1",
            topicId = "t_bsm01_1_1",
            questionTextEnglish = "When does a matrix NOT have an inverse?",
            questionType = "CONCEPTUAL",
            difficulty = "Basic",
            priority = "Important",
            sourceType = "PRACTICE",
            fullAnswerEnglish = "A matrix does not have an inverse if its determinant is zero. Such a matrix is called a singular matrix."
        )
        val mq2 = QuestionEntity(
            id = "q_sem1_math_2",
            semesterId = "sem_1",
            subjectId = "sub_bsm01",
            unitId = "u_bsm01_1",
            topicId = "t_bsm01_1_1",
            questionTextEnglish = "What is the rank of a matrix?",
            questionType = "DEFINITION",
            difficulty = "Intermediate",
            priority = "VVI",
            sourceType = "PRACTICE",
            fullAnswerEnglish = "The rank of a matrix is the maximum number of linearly independent row vectors (or column vectors) in the matrix."
        )
        academicDao.insertQuestions(listOf(mq1, mq2))

        // --- UNITS & TOPICS FOR ESEE1 (Electrical Basics) ---
        val eeUnit1 = UnitEntity(
            id = "u_esee1_1", subjectId = "sub_esee1", unitNumber = 1,
            title = "DC Circuits & Network Theorems", description = "Ohm's Law, KCL, KVL, Thevenin, Superposition.",
            version = "NEP-2023", source = "JUT"
        )
        val eeUnit2 = UnitEntity(
            id = "u_esee1_2", subjectId = "sub_esee1", unitNumber = 2,
            title = "AC Fundamentals", description = "RMS value, Average value, Form factor, Phasors.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(eeUnit1, eeUnit2))
        
        val eeTopic1 = TopicEntity(
            id = "t_esee1_1_1", unitId = "u_esee1_1",
            title = "Network Theorems", description = "Thevenin, Superposition, Maximum Power Transfer.",
            contentEnglish = "Network theorems simplify complex linear circuits.",
            version = "NEP-2023", source = "JUT"
        )
        val eeTopic2 = TopicEntity(
            id = "t_esee1_2_1", unitId = "u_esee1_2",
            title = "AC Waveforms", description = "Sinusoidal steady state analysis.",
            contentEnglish = "AC quantities vary periodically with time.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(eeTopic1, eeTopic2))

        // --- UNITS & TOPICS FOR ESEM1 (Mechanics) ---
        val mechUnit1 = UnitEntity(
            id = "u_esem1_1", subjectId = "sub_esem1", unitNumber = 1,
            title = "Forces & Equilibrium", description = "Resolution of forces, Lami's theorem.",
            version = "NEP-2023", source = "JUT"
        )
        val mechUnit2 = UnitEntity(
            id = "u_esem1_2", subjectId = "sub_esem1", unitNumber = 2,
            title = "Friction", description = "Static and kinetic friction, laws of solid friction.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(mechUnit1, mechUnit2))
        
        val mechTopic1 = TopicEntity(
            id = "t_esem1_1_1", unitId = "u_esem1_1",
            title = "Equilibrium of Coplanar Forces", description = "Conditions of equilibrium, free body diagrams.",
            contentEnglish = "A body is in equilibrium if the net force and net moment are zero.",
            version = "NEP-2023", source = "JUT"
        )
        val mechTopic2 = TopicEntity(
            id = "t_esem1_2_1", unitId = "u_esem1_2",
            title = "Laws of Friction", description = "Limiting friction, coefficient of friction, angle of repose.",
            contentEnglish = "Friction always opposes the impending relative motion.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(mechTopic1, mechTopic2))

        // --- ADDING ESEE1 & ESEM1 QUESTIONS ---
        val eq1 = QuestionEntity(
            id = "q_sem1_ee_6", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_1", topicId = "t_esee1_1_1",
            questionTextEnglish = "Define Thevenin's Theorem and describe the exact procedural steps required to calculate Vth and Rth.",
            questionType = "LONG_ANSWER", difficulty = "Advanced", priority = "VVI", sourceType = "HISTORICAL",
            evidenceNotes = "JUT Diploma ECE Master Bank Research Edition - Network Analysis",
            fullAnswerEnglish = "Any complex, linear, bilateral electrical network can be replaced by an equivalent circuit consisting of a single voltage source (Vth) in series with a single resistance (Rth). Vth is the open-circuit voltage at the terminals. Rth is the equivalent resistance looking back into the open terminals with all independent sources deactivated (voltage sources shorted, current sources opened)."
        )
        val eq2 = QuestionEntity(
            id = "q_sem1_ee_7", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_1", topicId = "t_esee1_1_1",
            questionTextEnglish = "State the Superposition Theorem and explicitly state its primary limitation regarding electrical power.",
            questionType = "CONCEPTUAL", difficulty = "Intermediate", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "In a linear network containing multiple independent sources, the total response in any branch is the algebraic sum of individual responses caused by each source acting alone (with others deactivated). Limitation: It is strictly applicable only to linear responses (voltage/current). It completely fails for power calculations because power is a non-linear function (P = I²R)."
        )
        val eq3 = QuestionEntity(
            id = "q_sem1_ee_8", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_1", topicId = "t_esee1_1_1",
            questionTextEnglish = "Discuss the Maximum Power Transfer Theorem. What are the implications for transmission efficiency?",
            questionType = "EXAMINER_FOLLOW_UP", difficulty = "Advanced", priority = "Important", sourceType = "HISTORICAL",
            fullAnswerEnglish = "A DC source delivers maximum power to a variable load when the load resistance equals the source's internal resistance (R_L = R_th). Implication: At maximum power transfer, exactly half the power is dissipated as waste heat in the source, capping efficiency at 50%. Therefore, it's used in low-power communication circuits (matching) but avoided in heavy power distribution to prevent overheating."
        )
        val mq3 = QuestionEntity(
            id = "q_sem1_mech_1", semesterId = "sem_1", subjectId = "sub_esem1", unitId = "u_esem1_1", topicId = "t_esem1_1_1",
            questionTextEnglish = "State Lami's Theorem for a particle in equilibrium.",
            questionType = "DEFINITION", difficulty = "Intermediate", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "Lami's Theorem states that if three coplanar, concurrent forces acting on a particle keep it in equilibrium, then each force is proportional to the sine of the angle between the other two forces: P/sin(alpha) = Q/sin(beta) = R/sin(gamma)."
        )
        val mq4 = QuestionEntity(
            id = "q_sem1_mech_2", semesterId = "sem_1", subjectId = "sub_esem1", unitId = "u_esem1_2", topicId = "t_esem1_2_1",
            questionTextEnglish = "Differentiate between static friction and kinetic friction.",
            questionType = "COMPARE", difficulty = "Basic", priority = "Important", sourceType = "PRACTICE",
            fullAnswerEnglish = "Static friction is the self-adjusting opposing force that prevents relative motion between two surfaces at rest. Kinetic (or dynamic) friction is the opposing force that acts when the surfaces are in relative motion. Static friction reaches a maximum value (limiting friction) which is always greater than kinetic friction."
        )
        academicDao.insertQuestions(listOf(eq1, eq2, eq3, mq3, mq4))

        // --- ADDING MORE ESEE1 & ESEM1 QUESTIONS (Deep Technical) ---
        val eq4 = QuestionEntity(
            id = "q_sem1_ee_9", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_1", topicId = "t_esee1_1_1",
            questionTextEnglish = "State Kirchhoff's Current Law (KCL).",
            questionType = "DEFINITION", difficulty = "Basic", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "KCL follows conservation of charge: the algebraic sum of currents at a node is zero. Currents entering a node equal currents leaving it."
        )
        val eq5 = QuestionEntity(
            id = "q_sem1_ee_10", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_1", topicId = "t_esee1_1_1",
            questionTextEnglish = "State Kirchhoff's Voltage Law (KVL).",
            questionType = "DEFINITION", difficulty = "Basic", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "KVL follows conservation of energy in the lumped-circuit model: the algebraic sum of voltage rises and drops around a closed loop is zero."
        )
        val eq6 = QuestionEntity(
            id = "q_sem1_ee_11", semesterId = "sem_1", subjectId = "sub_esee1", unitId = "u_esee1_2", topicId = "t_esee1_2_1",
            questionTextEnglish = "What is RMS value?",
            questionType = "CONCEPTUAL", difficulty = "Intermediate", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "RMS (Root Mean Square) is the equivalent DC value that would produce the same heating effect in a resistor. For a pure sinusoid, V_rms = V_m / √2."
        )
        val mq5 = QuestionEntity(
            id = "q_sem1_mech_3", semesterId = "sem_1", subjectId = "sub_esem1", unitId = "u_esem1_1", topicId = "t_esem1_1_1",
            questionTextEnglish = "Differentiate between Centroid and Centre of Gravity.",
            questionType = "COMPARE", difficulty = "Intermediate", priority = "Important", sourceType = "PRACTICE",
            fullAnswerEnglish = "Centroid is the geometric center of a plane area (2D), which depends only on geometry. Centre of Gravity (CG) is the point through which the entire weight of a body acts (3D), which depends on both geometry and mass distribution."
        )
        academicDao.insertQuestions(listOf(eq4, eq5, eq6, mq5))

        // --- UNITS & TOPICS FOR ESPP1 (Programming for Problem Solving) ---
        val progUnit1 = UnitEntity(
            id = "u_espp1_1", subjectId = "sub_espp1", unitNumber = 1,
            title = "C Fundamentals & Memory", description = "Variables, Pointers, Memory Allocation.",
            version = "NEP-2023", source = "JUT"
        )
        val progUnit2 = UnitEntity(
            id = "u_espp1_2", subjectId = "sub_espp1", unitNumber = 2,
            title = "Embedded C & Interrupts", description = "Volatile, ISRs, Watchdog timers.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(progUnit1, progUnit2))
        
        val progTopic1 = TopicEntity(
            id = "t_espp1_1_1", unitId = "u_espp1_1",
            title = "Memory Architecture", description = "Generic C vs Embedded C memory paradigms.",
            contentEnglish = "Embedded systems face severe memory constraints compared to desktop applications.",
            version = "NEP-2023", source = "JUT"
        )
        val progTopic2 = TopicEntity(
            id = "t_espp1_2_1", unitId = "u_espp1_2",
            title = "Real-Time Execution", description = "Interrupts, ISRs, and hardware interaction.",
            contentEnglish = "The volatile keyword and ISRs are the backbone of real-time embedded C.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(progTopic1, progTopic2))

        // --- ADDING ESPP1 QUESTIONS (Deep C & Embedded Concepts) ---
        val pq1 = QuestionEntity(
            id = "q_sem1_prog_1", semesterId = "sem_1", subjectId = "sub_espp1", unitId = "u_espp1_2", topicId = "t_espp1_2_1",
            questionTextEnglish = "Articulate the precise function of the 'volatile' keyword in C. Provide a specific hardware scenario where its omission causes failure.",
            questionType = "LONG_ANSWER", difficulty = "Advanced", priority = "VVI", sourceType = "VERIFIED",
            evidenceNotes = "JUT Master Exam Bank - Embedded C Programming",
            fullAnswerEnglish = "The 'volatile' keyword is a strict directive telling the compiler that a variable's value may change at any time, independently of the sequential code (e.g., modified by hardware or an ISR). It absolutely prevents the compiler from optimizing/caching the variable in a CPU register. Scenario: A while loop waiting for a UART receive buffer full flag. Without 'volatile', the compiler assumes the flag never changes in the loop and optimizes it into an infinite loop, crashing the system."
        )
        val pq2 = QuestionEntity(
            id = "q_sem1_prog_2", semesterId = "sem_1", subjectId = "sub_espp1", unitId = "u_espp1_1", topicId = "t_espp1_1_1",
            questionTextEnglish = "Compare and contrast generic C programming with Embedded C programming, specifically regarding memory allocation.",
            questionType = "COMPARE", difficulty = "Intermediate", priority = "Important", sourceType = "HISTORICAL",
            fullAnswerEnglish = "Generic C (for desktops) relies on OS-managed virtual memory and frequently uses dynamic allocation (malloc/free). Embedded C runs on heavily constrained hardware (kilobytes of RAM), requires direct pointer-based hardware register interaction, and strictly avoids dynamic allocation to prevent heap fragmentation and non-deterministic execution overhead."
        )
        val pq3 = QuestionEntity(
            id = "q_sem1_prog_3", semesterId = "sem_1", subjectId = "sub_espp1", unitId = "u_espp1_2", topicId = "t_espp1_2_1",
            questionTextEnglish = "Explain the concept of Interrupt Service Routines (ISR) and Interrupt Latency.",
            questionType = "CONCEPTUAL", difficulty = "Advanced", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "An ISR is a highly specialized C function that executes in direct response to a hardware interrupt, preempting the main thread. Interrupt latency is the exact temporal delay between the physical hardware request and the execution of the ISR's first instruction. ISRs must be incredibly brief and avoid blocking functions to prevent stack overflow and missed deadlines."
        )
        val pq4 = QuestionEntity(
            id = "q_sem1_prog_4", semesterId = "sem_1", subjectId = "sub_espp1", unitId = "u_espp1_2", topicId = "t_espp1_2_1",
            questionTextEnglish = "Define a Watchdog Timer (WDT) and explain its indispensable role.",
            questionType = "DEFINITION", difficulty = "Intermediate", priority = "VVI", sourceType = "HISTORICAL",
            fullAnswerEnglish = "A WDT is a dedicated hardware timing peripheral that automatically resets the microcontroller if the main program gets stuck in an infinite loop or deadlock. The software must periodically clear ('feed') the timer. It is crucial for mission-critical applications (satellites, automotive) where human physical reset is impossible."
        )
        val pq5 = QuestionEntity(
            id = "q_sem1_prog_5", semesterId = "sem_1", subjectId = "sub_espp1", unitId = "u_espp1_1", topicId = "t_espp1_1_1",
            questionTextEnglish = "Why is dynamic memory allocation (malloc/free) often avoided in embedded C systems?",
            questionType = "WHY_HOW", difficulty = "Intermediate", priority = "Important", sourceType = "PRACTICE",
            fullAnswerEnglish = "Small deterministic systems avoid malloc/free because heap fragmentation, allocation failure risks, variable execution time, and massive memory overhead heavily compromise system reliability."
        )
        academicDao.insertQuestions(listOf(pq1, pq2, pq3, pq4, pq5))

        // --- UNITS & TOPICS FOR HSM01 (Indian Knowledge System) ---
        val hsmUnit1 = UnitEntity(
            id = "u_hsm01_1", subjectId = "sub_hsm01", unitNumber = 1,
            title = "Introduction to IKS", description = "Foundational concepts of traditional Indian knowledge.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(hsmUnit1))
        
        val hsmTopic1 = TopicEntity(
            id = "t_hsm01_1_1", unitId = "u_hsm01_1",
            title = "Core Scientific Concepts", description = "Ancient Indian science and its modern relevance.",
            contentEnglish = "The Indian Knowledge System encompasses a rich heritage of science, mathematics, and philosophy.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(hsmTopic1))

        // --- UNITS & TOPICS FOR VSC01 (Data Visualization) ---
        val vscUnit1 = UnitEntity(
            id = "u_vsc01_1", subjectId = "sub_vsc01", unitNumber = 1,
            title = "Data Fundamentals & Pre-processing", description = "Data cleaning, missing values, outliers.",
            version = "NEP-2023", source = "JUT"
        )
        val vscUnit2 = UnitEntity(
            id = "u_vsc01_2", subjectId = "sub_vsc01", unitNumber = 2,
            title = "Data Visualization", description = "Charts, graphs, and visual interpretation.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertUnits(listOf(vscUnit1, vscUnit2))
        
        val vscTopic1 = TopicEntity(
            id = "t_vsc01_1_1", unitId = "u_vsc01_1",
            title = "Data Cleaning", description = "Techniques for handling messy data.",
            contentEnglish = "Real-world data is often incomplete or inconsistent and requires cleaning.",
            version = "NEP-2023", source = "JUT"
        )
        val vscTopic2 = TopicEntity(
            id = "t_vsc01_2_1", unitId = "u_vsc01_2",
            title = "Visualization Techniques", description = "Selecting the right chart for the data.",
            contentEnglish = "Effective visualization translates complex datasets into clear graphical narratives.",
            version = "NEP-2023", source = "JUT"
        )
        academicDao.insertTopics(listOf(vscTopic1, vscTopic2))

        // --- ADDING HSM01 & VSC01 QUESTIONS ---
        val hq1 = QuestionEntity(
            id = "q_sem1_hsm_1", semesterId = "sem_1", subjectId = "sub_hsm01", unitId = "u_hsm01_1", topicId = "t_hsm01_1_1",
            questionTextEnglish = "What constitutes the Indian Knowledge System (IKS)?",
            questionType = "CONCEPTUAL", difficulty = "Basic", priority = "Important", sourceType = "PRACTICE",
            fullAnswerEnglish = "IKS comprises the traditional knowledge structures developed in India over millennia, encompassing linguistics, mathematics, astronomy, architecture, metallurgy, and philosophy, forming a holistic approach to understanding nature and human existence."
        )
        val vq1 = QuestionEntity(
            id = "q_sem1_vsc_1", semesterId = "sem_1", subjectId = "sub_vsc01", unitId = "u_vsc01_1", topicId = "t_vsc01_1_1",
            questionTextEnglish = "What is data normalization and why is it required before visualization?",
            questionType = "WHY_HOW", difficulty = "Intermediate", priority = "VVI", sourceType = "PRACTICE",
            fullAnswerEnglish = "Data normalization rescales numeric attributes into a standard range (e.g., 0 to 1 or zero mean/unit variance). It is required because features with vastly different scales can distort visualizations, skew machine learning models, and make comparative analysis mathematically misleading."
        )
        val vq2 = QuestionEntity(
            id = "q_sem1_vsc_2", semesterId = "sem_1", subjectId = "sub_vsc01", unitId = "u_vsc01_1", topicId = "t_vsc01_1_1",
            questionTextEnglish = "Explain the standard approaches for handling missing values in a dataset.",
            questionType = "SHORT_ANSWER", difficulty = "Intermediate", priority = "Important", sourceType = "PRACTICE",
            fullAnswerEnglish = "Standard approaches include: 1. Deletion (removing rows/columns if missing data is minimal). 2. Imputation (filling missing values with the mean, median, or mode). 3. Predictive modeling (using algorithms to estimate the missing value based on other features)."
        )
        val vq3 = QuestionEntity(
            id = "q_sem1_vsc_3", semesterId = "sem_1", subjectId = "sub_vsc01", unitId = "u_vsc01_2", topicId = "t_vsc01_2_1",
            questionTextEnglish = "Differentiate between a histogram and a bar chart.",
            questionType = "COMPARE", difficulty = "Basic", priority = "VVI", sourceType = "PRACTICE",
            fullAnswerEnglish = "A bar chart represents categorical data with rectangular bars separated by gaps, where length is proportional to value. A histogram represents the frequency distribution of continuous numerical data, with contiguous bars (no gaps) where the area represents frequency."
        )
        academicDao.insertQuestions(listOf(hq1, vq1, vq2, vq3))





        
      val experiments = listOf(
        ExperimentEntity(
          id = "exp_01",
          title = "P-N Junction Diode I-V Characteristics",
          description = "Study forward and reverse bias characteristics of Si and Ge diodes.",
          aim = "To plot the V-I characteristics of a P-N junction diode and determine static and dynamic resistance.",
          apparatusCommaSeparated = "PN Diode (IN4007), DC Regulated Power Supply (0-30V), Resistor (1kΩ), Voltmeter, Ammeter, Breadboard",
          theory = "A PN junction diode conducts current easily in forward bias when barrier potential (~0.7V for Si) is overcome. In reverse bias, only a minuscule reverse saturation current flows until breakdown.",
          formula = "Static Resistance R_dc = V/I, Dynamic Resistance r_ac = ΔV/ΔI",
          category = "Analog Electronics"
        ),
        ExperimentEntity(
          id = "exp_02",
          title = "Zener Diode Voltage Regulator",
          description = "Analyze Zener diode voltage regulation characteristics under varying load.",
          aim = "To study Zener diode as a voltage regulator and calculate line and load regulation.",
          apparatusCommaSeparated = "Zener Diode (5.1V), DC Power Supply, Load Resistors, Multimeters, Breadboard",
          theory = "A Zener diode operates in the breakdown region maintaining a nearly constant voltage across its terminals despite changes in supply voltage or load current.",
          formula = "Load Regulation % = ((V_NL - V_FL) / V_FL) * 100",
          category = "Analog Electronics"
        ),
        ExperimentEntity(
          id = "exp_03",
          title = "BJT Common Emitter (CE) Characteristics",
          description = "Determine input and output characteristics of an NPN BJT in Common Emitter configuration.",
          aim = "To plot input and output characteristic curves of a transistor and determine h-parameters.",
          apparatusCommaSeparated = "NPN Transistor (BC547), DC Supplies, Potentiometers, Microammeter, Milliammeter",
          theory = "In CE configuration, emitter is common to input (base) and output (collector). It provides high current and voltage gain.",
          formula = "Current Gain β = ΔI_c / ΔI_b at constant V_ce",
          category = "Analog Electronics"
        ),
        ExperimentEntity(
          id = "exp_04",
          title = "Op-Amp (IC 741) Inverting & Non-Inverting Amplifier",
          description = "Study operational amplifier gain configurations using IC 741.",
          aim = "To design and test inverting and non-inverting amplifiers using LM741 Op-Amp.",
          apparatusCommaSeparated = "IC 741, Dual DC Power Supply (+/-12V), Resistors, Function Generator, CRO",
          theory = "An Op-Amp uses high open-loop gain and negative feedback to stabilize closed-loop gain precisely with external resistors.",
          formula = "Inverting Gain A_v = - (R_f / R_in)",
          category = "Analog Electronics"
        ),
        ExperimentEntity(
          id = "exp_05",
          title = "RC Phase Shift Oscillator",
          description = "Generate sustained sine wave oscillations using transistor and RC ladder network.",
          aim = "To generate audio frequency sine waves using RC phase shift oscillator circuit.",
          apparatusCommaSeparated = "Transistor BC547, Capacitors, Resistors, CRO, DC Power Supply",
          theory = "The circuit provides 180° phase shift via three RC networks and 180° phase shift via transistor, satisfying Barkhausen criterion for oscillation.",
          formula = "Frequency f = 1 / (2 * pi * R * C * sqrt(6))",
          category = "Analog Electronics"
        ),
        ExperimentEntity(
          id = "exp_06",
          title = "Verification of De-Morgan's Theorems",
          description = "Verify Boolean algebra laws using logic gate ICs.",
          aim = "To verify De-Morgan's first law (A+B)' = A' . B' and second law (A.B)' = A' + B' using IC 7400 and IC 7432.",
          apparatusCommaSeparated = "IC 7400 (NAND), IC 7432 (OR), IC 7404 (NOT), Breadboard, DC Source, LEDs",
          theory = "De-Morgan's theorems link logical OR and logical AND operations through negation, fundamental to logic circuit simplification.",
          formula = "Logic Expression: (A + B)' = A' . B'",
          category = "Digital Electronics"
        ),
        ExperimentEntity(
          id = "exp_07",
          title = "Design of Half Adder & Full Adder",
          description = "Implement arithmetic binary addition logic using XOR and basic gates.",
          aim = "To construct half adder and full adder circuits and verify truth tables.",
          apparatusCommaSeparated = "IC 7486 (XOR), IC 7408 (AND), IC 7432 (OR), Breadboard, LED indicators",
          theory = "Half adder adds two bits yielding Sum and Carry. Full adder incorporates an input carry bit to add three bits simultaneously.",
          formula = "Sum = A XOR B XOR C_in, Carry = AB + BC_in + AC_in",
          category = "Digital Electronics"
        ),
        ExperimentEntity(
          id = "exp_08",
          title = "Multiplexers & Demultiplexers Implementation",
          description = "Route data lines using IC 74153 MUX and IC 74138 DEMUX.",
          aim = "To implement Boolean functions using Multiplexer IC.",
          apparatusCommaSeparated = "IC 74153 (Dual 4-to-1 MUX), Logic Switches, LEDs, Breadboard",
          theory = "A MUX selects one of many input data sources and forwards it to a single output line based on select control lines.",
          formula = "Output Y = Sigma m(selector combinations)",
          category = "Digital Electronics"
        ),
        ExperimentEntity(
          id = "exp_09",
          title = "Verification of KCL and KVL",
          description = "Validate Kirchhoff's Current Law and Voltage Law in DC resistive networks.",
          aim = "To experimentally verify Kirchhoff's Current Law at a node and Voltage Law in a closed mesh.",
          apparatusCommaSeparated = "Resistors, DC Regulated Power Supply, Digital Multimeter, Breadboard",
          theory = "KCL states total current entering a node equals current leaving (Sigma I = 0). KVL states sum of potential drops in a closed loop equals source EMF (Sigma V = 0).",
          formula = "Sigma I_in = Sigma I_out, Sigma V_drops = E_source",
          category = "Circuit Theory"
        ),
        ExperimentEntity(
          id = "exp_10",
          title = "Verification of Thevenin's and Norton's Theorems",
          description = "Simplify linear active DC networks into equivalent voltage and current sources.",
          aim = "To verify Thevenin's theorem by finding V_th and R_th across load terminals.",
          apparatusCommaSeparated = "Resistors of various values, DC Power Supply, Multimeter",
          theory = "Thevenin's theorem states any two-terminal linear network can be replaced by an equivalent voltage source V_th in series with resistance R_th.",
          formula = "I_L = V_th / (R_th + R_L)",
          category = "Circuit Theory"
        )
      )

      database.labDao().insertExperiments(experiments)

      val questions = listOf(
        // Exp 01
        VivaVoceEntity(
          experimentId = "exp_01",
          question = "What is the knee voltage for a Silicon P-N junction diode?",
          optionA = "0.2 V",
          optionB = "0.7 V",
          optionC = "1.1 V",
          optionD = "0.3 V",
          correctOptionIndex = 1,
          hinglishExplanation = "Toh dekho! Silicon diode ke liye barrier potential ya knee voltage lagbhag 0.7V hota hai, jabki Germanium ke liye 0.3V hota hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_01",
          question = "Why does dynamic resistance decrease at higher forward current?",
          optionA = "Because barrier potential increases",
          optionB = "Because slope of V-I curve (dV/dI) decreases",
          optionC = "Temperature drops",
          optionD = "None of the above",
          correctOptionIndex = 1,
          hinglishExplanation = "Simple hai! V-I curve exponential hoti hai, isliye current badhne par dV/dI (dynamic resistance) kam ho jati hai."
        ),
        // Exp 02
        VivaVoceEntity(
          experimentId = "exp_02",
          question = "In which region does a Zener diode operate for voltage regulation?",
          optionA = "Forward bias region",
          optionB = "Cut-off region",
          optionC = "Reverse breakdown region",
          optionD = "Saturation region",
          correctOptionIndex = 2,
          hinglishExplanation = "Zener diode hamesha reverse breakdown region mein operate karta hai jahan voltage constant rehti hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_02",
          question = "What happens to Zener current when load resistance decreases?",
          optionA = "Zener current increases",
          optionB = "Zener current decreases",
          optionC = "Zener current remains constant",
          optionD = "Diode burns out immediately",
          correctOptionIndex = 1,
          hinglishExplanation = "Load resistance kam hone se load current badh jata hai, jisse total source current mein se Zener current ghat jata hai."
        ),
        // Exp 03
        VivaVoceEntity(
          experimentId = "exp_03",
          question = "Why is Common Emitter (CE) configuration most widely used in amplifiers?",
          optionA = "It has lowest voltage gain",
          optionB = "It provides high voltage and current gain",
          optionC = "Input resistance is extremely high",
          optionD = "It has unity gain",
          correctOptionIndex = 1,
          hinglishExplanation = "CE configuration ka current gain aur voltage gain dono high hote hain, isliye audio aur RF amplifiers mein sabse zyada use hota hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_03",
          question = "What is the typical range of current gain (beta) for a standard BJT?",
          optionA = "1 to 5",
          optionB = "20 to 200",
          optionC = "1000 to 5000",
          optionD = "Zero",
          correctOptionIndex = 1,
          hinglishExplanation = "Standard BJT transistors jaise BC547 ka beta (h_FE) aamtaur par 100 se 300 ke beech hota hai."
        ),
        // Exp 04
        VivaVoceEntity(
          experimentId = "exp_04",
          question = "What is the closed-loop voltage gain formula for an inverting Op-Amp?",
          optionA = "1 + (Rf / Rin)",
          optionB = "- (Rf / Rin)",
          optionC = "Rf * Rin",
          optionD = "Rin / Rf",
          correctOptionIndex = 1,
          hinglishExplanation = "Inverting amplifier mein gain ka formula - (Rf / Rin) hota hai, yahan negative sign 180° phase shift darshata hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_04",
          question = "What is the input impedance of an ideal Operational Amplifier?",
          optionA = "Zero",
          optionB = "100 Ohms",
          optionC = "Infinite",
          optionD = "1 kOhm",
          correctOptionIndex = 2,
          hinglishExplanation = "Ideal Op-Amp ki input impedance infinite hoti hai taaki source current draw na kare."
        ),
        // Exp 05
        VivaVoceEntity(
          experimentId = "exp_05",
          question = "What condition must be satisfied for sustained oscillations (Barkhausen Criterion)?",
          optionA = "Loop gain |Aβ| = 1 and total phase shift 0° or 360°",
          optionB = "Loop gain = 0",
          optionC = "Phase shift = 90°",
          optionD = "Gain < 1",
          correctOptionIndex = 0,
          hinglishExplanation = "Barkhausen criteria ke anusar loop gain ka magnitude 1 hona chahiye aur total phase shift 0° ya 360° honi chahiye."
        ),
        VivaVoceEntity(
          experimentId = "exp_05",
          question = "How many RC sections are used in an RC phase shift network to get 180° phase shift?",
          optionA = "1",
          optionB = "2",
          optionC = "3",
          optionD = "4",
          correctOptionIndex = 2,
          hinglishExplanation = "Har ek RC section lagbhag 60° phase shift deta hai, isliye 180° ke liye kam se kam 3 sections chahiye."
        ),
        // Exp 06
        VivaVoceEntity(
          experimentId = "exp_06",
          question = "According to De-Morgan's first law, (A + B)' equals:",
          optionA = "A' + B'",
          optionB = "A' . B'",
          optionC = "A . B",
          optionD = "A + B",
          correctOptionIndex = 1,
          hinglishExplanation = "De-Morgan ka pehla law kehta hai ki NOR gate ka output equal hota hai AND gate with inverted inputs (A' . B')."
        ),
        VivaVoceEntity(
          experimentId = "exp_06",
          question = "Which IC is commonly used as a Quad 2-input NAND gate?",
          optionA = "IC 7408",
          optionB = "IC 7400",
          optionC = "IC 7432",
          optionD = "IC 7404",
          correctOptionIndex = 1,
          hinglishExplanation = "IC 7400 standard TTL quad 2-input NAND gate IC hai jo digital labs mein bohot use hoti hai."
        ),
        // Exp 07
        VivaVoceEntity(
          experimentId = "exp_07",
          question = "What logic gates are required to build a Half Adder?",
          optionA = "AND and OR gate",
          optionB = "XOR and AND gate",
          optionC = "NAND gate only",
          optionD = "NOT and OR gate",
          correctOptionIndex = 1,
          hinglishExplanation = "Half adder mein Sum ke liye XOR gate aur Carry ke liye AND gate ka use hota hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_07",
          question = "How many inputs does a Full Adder have?",
          optionA = "2",
          optionB = "3",
          optionC = "4",
          optionD = "1",
          correctOptionIndex = 1,
          hinglishExplanation = "Full adder ke 3 inputs hote hain: A, B, aur previous stage se C_in (carry in)."
        ),
        // Exp 08
        VivaVoceEntity(
          experimentId = "exp_08",
          question = "A Multiplexer is known as a:",
          optionA = "Data distributor",
          optionB = "Data selector / Many-to-one circuit",
          optionC = "Decoder",
          optionD = "Code converter",
          correctOptionIndex = 1,
          hinglishExplanation = "Multiplexer ek 'data selector' hai jo kai inputs mein se ek ko select karke single output par bhejta hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_08",
          question = "How many select lines are needed for an 8-to-1 Multiplexer?",
          optionA = "2",
          optionB = "3",
          optionC = "4",
          optionD = "8",
          correctOptionIndex = 1,
          hinglishExplanation = "2^n inputs ke liye n select lines chahiye hoti hain. Toh 8 inputs ke liye 2^3 = 8, yaani 3 select lines!"
        ),
        // Exp 09
        VivaVoceEntity(
          experimentId = "exp_09",
          question = "Kirchhoff's Current Law (KCL) is based on the conservation of:",
          optionA = "Energy",
          optionB = "Charge / Current",
          optionC = "Power",
          optionD = "Flux",
          correctOptionIndex = 1,
          hinglishExplanation = "KCL conservation of electric charge par आधारित hai, yaani node par aane wala total current jaane wale current ke barabar hota hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_09",
          question = "Kirchhoff's Voltage Law (KVL) is based on the conservation of:",
          optionA = "Charge",
          optionB = "Energy",
          optionC = "Momentum",
          optionD = "Mass",
          correctOptionIndex = 1,
          hinglishExplanation = "KVL law of conservation of energy par based hai, jisme closed loop mein total potential drop source EMF ke equal hota hai."
        ),
        // Exp 10
        VivaVoceEntity(
          experimentId = "exp_10",
          question = "Thevenin's equivalent circuit consists of:",
          optionA = "A current source in parallel with resistance",
          optionB = "A voltage source V_th in series with resistance R_th",
          optionC = "Only a capacitor",
          optionD = "Pure inductance",
          correctOptionIndex = 1,
          hinglishExplanation = "Thevenin's theorem kehta hai ki koi bhi complex network ek independent voltage source V_th aur series resistor R_th se replace ho sakta hai."
        ),
        VivaVoceEntity(
          experimentId = "exp_10",
          question = "To find Thevenin's resistance R_th, what must be done to independent voltage sources?",
          optionA = "Short circuited",
          optionB = "Open circuited",
          optionC = "Replaced with 100V",
          optionD = "Left untouched",
          correctOptionIndex = 0,
          hinglishExplanation = "R_th nikalte samay independent voltage sources ko short circuit aur current sources ko open circuit kiya jata hai."
        )
      )

      database.labDao().insertVivaQuestions(questions)

      
      
      // Basic Seed for Phase 3
      val program = AcademicProgramEntity(
          id = "jut_ece_dip",
          title = "Diploma in Electronics & Communication Engineering",
          description = "3 Year Diploma Program under JUT",
          version = "2024",
          source = "JUT Syllabus"
      )
      academicDao.insertPrograms(listOf(program))
      
      val s1 = SemesterEntity(id = "sem_1", programId = "jut_ece_dip", semesterNumber = 1, title = "Semester 1", description = "Foundation Subjects", version = "2024", source = "JUT")
      val s3 = SemesterEntity(id = "sem_3", programId = "jut_ece_dip", semesterNumber = 3, title = "Semester 3", description = "Core Electronics", version = "2024", source = "JUT")
      academicDao.insertSemesters(listOf(s1, s3))
      
      val edc = SubjectEntity(id = "sub_edc", semesterId = "sem_3", title = "Electronic Devices and Circuits", description = "Diodes, BJTs, FETs", type = "Theory", version = "2024", source = "JUT")
      academicDao.insertSubjects(listOf(edc))
      
      val u3 = UnitEntity(id = "unit_3", subjectId = "sub_edc", unitNumber = 3, title = "Bipolar Junction Transistor", description = "Construction, CB, CE, CC", version = "2024", source = "JUT")
      academicDao.insertUnits(listOf(u3))
      
      val t1 = TopicEntity(
          id = "top_bjt_ce",
          unitId = "unit_3",
          title = "CE Configuration",
          description = "Common Emitter Characteristics",
          contentEnglish = "In Common Emitter configuration, the emitter is common to both input and output terminals. It provides high voltage and current gain.",
          contentHinglish = "CE configuration mein emitter input aur output dono ke liye common hota hai. Isme voltage aur current gain dono high hote hain.",
          version = "2024",
          source = "JUT"
      )
      academicDao.insertTopics(listOf(t1))

    }
  }
}
