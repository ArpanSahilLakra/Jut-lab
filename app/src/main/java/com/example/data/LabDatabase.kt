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
  val category: String
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
  val hinglishExplanation: String
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
    LabReportEntity::class,
    BookmarkEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class LabDatabase : RoomDatabase() {
  abstract fun labDao(): LabDao

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
          .fallbackToDestructiveMigration()
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
            populateInitialData(database.labDao())
          }
        }
      }
    }

    suspend fun populateInitialData(dao: LabDao) {
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

      dao.insertExperiments(experiments)

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

      dao.insertVivaQuestions(questions)
    }
  }
}
