package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AppAudioManager
import com.example.audio.AppHapticManager
import com.example.audio.AppNotificationManager
import com.example.data.AppPreferences
import com.example.data.*
import com.example.data.LabDatabase
import com.example.repository.AuthRepository
import com.example.repository.AuthSessionManager
import com.example.repository.AuthState
import androidx.lifecycle.lifecycleScope
import com.example.repository.ExperimentRepository
import com.example.repository.ProgressRepository
import com.example.repository.ReportRepository
import com.example.repository.SyncRepository
import com.example.ui.components.SyncStatusIndicator
import com.example.ui.components.WatermarkOverlay
import com.example.ui.screens.*
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OffWhite
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    private lateinit var appPreferences: AppPreferences
    private lateinit var audioManager: AppAudioManager
    private lateinit var hapticManager: AppHapticManager
    private lateinit var notificationManager: AppNotificationManager

    override fun onDestroy() {
        super.onDestroy()
        if (::audioManager.isInitialized) {
            audioManager.release()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val stackTrace = android.util.Log.getStackTraceString(exception)
            getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putString("last_crash", stackTrace)
                .commit()
            defaultHandler?.uncaughtException(thread, exception)
        }


        val prefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
        val lastCrash = prefs.getString("last_crash", null)
        if (lastCrash != null) {
            super.onCreate(savedInstanceState)
            setContent {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Red)) {
                    androidx.compose.foundation.lazy.LazyColumn {
                        item {
                            androidx.compose.material3.Text(text = "CRASH LOG:\n$lastCrash", color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(16.dp))
                        }
                        item {
                            androidx.compose.material3.Button(onClick = { prefs.edit().clear().apply(); finish() }) {
                                androidx.compose.material3.Text("Clear Crash Log & Exit")
                            }
                        }
                    }
                }
            }
            return
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = LabDatabase.getDatabase(applicationContext)
        val labDao = database.labDao()
        appPreferences = AppPreferences(applicationContext)
        AuthSessionManager.initialize(labDao, lifecycleScope)
        audioManager = AppAudioManager(applicationContext, appPreferences)
        hapticManager = AppHapticManager(applicationContext, appPreferences)
        notificationManager = AppNotificationManager(applicationContext, appPreferences)

        setContent {
            CompositionLocalProvider(
                LocalAppPreferences provides appPreferences,
                LocalAudioManager provides audioManager,
                LocalHapticManager provides hapticManager,
                LocalNotificationManager provides notificationManager
            ) {
                MyApplicationTheme {
                    


                    val context = LocalContext.current
                    val prefs = context.getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
                    val lastCrash = prefs.getString("last_crash", null)
                    
/* MOVED */ if (false) {
                        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Red)) {
                            androidx.compose.foundation.lazy.LazyColumn {
                                item {
                                    androidx.compose.material3.Text(text = "CRASH LOG:\n$lastCrash", color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.padding(16.dp))
                                }
                                item {
                                    androidx.compose.material3.Button(onClick = { prefs.edit().clear().apply() }) {
                                        androidx.compose.material3.Text("Clear Crash Log")
                                    }
                                }
                            }
                        }
                    } else {

                    SyncTrigger(labDao = labDao)
                    val connectivityObserver = remember { NetworkConnectivityObserver(context) }
                    val networkStatus by connectivityObserver.observe().collectAsState(initial = NetworkStatus.Available)
                    
                    var currentRoute by remember { mutableStateOf("startup") }
                                        val authState by AuthSessionManager.authState.collectAsState()
                    var selectedExpId by remember { mutableStateOf("") }
                    var selectedSemesterId by remember { mutableStateOf("sem_1") }
                    var selectedSubjectId by remember { mutableStateOf("") }
                    var selectedUnitId by remember { mutableStateOf("") }
                    var selectedTopicId by remember { mutableStateOf("") }
                    var selectedReportId by remember { mutableStateOf("") }
                    
                    val experimentRepository = remember { ExperimentRepository(labDao) }
                    val syncRepository = remember { SyncRepository(labDao) }
                    val authRepository = remember { AuthRepository() }
                    val progressRepository = remember { ProgressRepository(labDao, syncRepository, authRepository) }
                    val reportRepository = remember { ReportRepository(labDao, syncRepository, authRepository) }
                    val academicDao = LabDatabase.getDatabase(context).academicDao()
                    val academicViewModel: AcademicViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AcademicViewModelFactory(academicDao))
                    val mockExamViewModel: com.example.ui.screens.MockExamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.ui.screens.MockExamViewModelFactory(academicDao))
                    val vivaEngineViewModel: com.example.ui.screens.VivaEngineViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.ui.screens.VivaEngineViewModelFactory(academicDao))

                    var userRole by remember { mutableStateOf("student") }
                    
                    // No mandatory auth gate. App opens directly into Home.
                    LaunchedEffect(currentRoute) {
                        val profile = labDao.getStudentProfile().firstOrNull()
                        if (profile != null) {
                            userRole = profile.role
                        }
                    }

                    Scaffold { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                AnimatedContent(
                                    targetState = currentRoute,
                                    label = "route_transition",
                                    transitionSpec = {
                                        fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                                    }
                                ) { targetRoute ->
                                    when (targetRoute) {
                                        "startup" -> StartupScreen(labDao = labDao, onStartupComplete = { currentRoute = "home" })
                                        "quiz" -> QuizScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "calculator" -> ElectronicsCalculatorScreen(onBack = { currentRoute = "home" })
                                        "ic_pinout" -> IcPinoutScreen(onBack = { currentRoute = "home" })
                                        "bookmarks" -> BookmarksScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "engineering_converter" -> EngineeringConverterScreen(onBack = { currentRoute = "home" })
                                        "ai_tutor" -> AiTutorScreen(onBack = { currentRoute = "home" }, onNavigateToSettings = { currentRoute = "settings" })
                                        "learning_material" -> LearningMaterialScreen(onBack = { currentRoute = "home" })

                                        
                                        "home" -> HomeScreen(labDao = labDao, onNavigateToExperiment = { selectedExpId = it; currentRoute = "dynamic_experiment" }, onNavigate = { currentRoute = it })
                                        "logic_gate" -> LogicGateScreen(onBack = { currentRoute = "home" })
                                        "oscilloscope" -> OscilloscopeScreen(onBack = { currentRoute = "home" })
                                        "breadboard" -> BreadboardScreen(onBack = { currentRoute = "home" })
                                        "manuals" -> {
                                            val viewModel: ManualsViewModel = viewModel(factory = ManualsViewModelFactory(experimentRepository))
                                            ManualsScreen(
                                                viewModel = viewModel,
                                                onSelectExperiment = { expId ->
                                                    selectedExpId = expId
                                                    currentRoute = "dynamic_experiment"
                                                },
                                                onTakeQuiz = { expId ->
                                                    selectedExpId = expId
                                                    currentRoute = "viva_quiz"
                                                },
                                                onBack = { currentRoute = "home" }
                                            )
                                        }
                                        "dynamic_experiment" -> DynamicExperimentScreen(
                                            experimentId = selectedExpId,
                                            labDao = labDao,
                                            progressRepository = progressRepository,
                                            reportRepository = reportRepository,
                                            onNavigateToSimulator = { currentRoute = it },
                                            onNavigateToQuiz = { expId ->
                                                selectedExpId = expId
                                                currentRoute = "viva_quiz"
                                            },
                                            onNavigateToReport = { expId ->
                                                selectedExpId = expId
                                                currentRoute = "lab_report"
                                            },
                                            onBack = { currentRoute = "home" }
                                        )
                                        "viva_quiz" -> VivaQuizScreen(
                                            experimentId = selectedExpId,
                                            labDao = labDao,
                                            onBack = { currentRoute = "home" }
                                        )
                                        "student_dashboard" -> StudentDashboardScreen(labDao = labDao)
                                        "teacher_workspace" -> TeacherWorkspaceScreen(labDao = labDao, onNavigateToSubmission = { id -> selectedReportId = id; currentRoute = "submission_detail" })
                                        
                                        "semester_list" -> SemesterListScreen(
                                            viewModel = academicViewModel,
                                            onNavigateToSubjects = { id -> selectedSemesterId = id; currentRoute = "subject_list" },
                                            onBack = { currentRoute = "home" }
                                        )
                                        "subject_list" -> SubjectListScreen(
                                            semesterId = selectedSemesterId,
                                            viewModel = academicViewModel,
                                            onNavigateToSubjectDetail = { id -> selectedSubjectId = id; currentRoute = "subject_detail" },
                                            onNavigateToExam = { currentRoute = "mock_exam" },
                                            onBack = { currentRoute = "semester_list" }
                                        )
                                        "subject_detail" -> SubjectDetailScreen(
                                            subjectId = selectedSubjectId,
                                            viewModel = academicViewModel,
                                            onNavigateToTopic = { id -> selectedUnitId = id; currentRoute = "topic_list" },
                                            onNavigateToViva = { id -> selectedSubjectId = id; currentRoute = "viva_engine" },
                                            onBack = { currentRoute = "subject_list" }
                                        )
                                        "topic_list" -> TopicListScreen(
                                            unitId = selectedUnitId,
                                            viewModel = academicViewModel,
                                            onNavigateToTopicDetail = { id -> selectedTopicId = id; currentRoute = "topic_detail" },
                                            onBack = { currentRoute = "subject_detail" }
                                        )
                                        "topic_detail" -> TopicDetailScreen(
                                            topicId = selectedTopicId,
                                            viewModel = academicViewModel,
                                            onNavigateToAI = { topicName -> currentRoute = "ai_tutor" },
                                            onBookmark = { topic -> 
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    labDao.insertBookmark(BookmarkEntity(
                                                        id = "bookmark_${topic.id}",
                                                        userId = "",
                                                        type = "TOPIC",
                                                        referenceId = topic.id,
                                                        title = topic.title,
                                                        description = topic.description
                                                    ))
                                                }
                                            },
                                            onBack = { currentRoute = "topic_list" }
                                        )
                                        "viva_engine" -> com.example.ui.screens.VivaEngineScreen(
                                            subjectId = selectedSubjectId,
                                            viewModel = vivaEngineViewModel,
                                            onBack = { currentRoute = "subject_detail" }
                                        )
                                        "mock_exam" -> com.example.ui.screens.MockExamScreen(
                                            semesterId = selectedSemesterId,
                                            viewModel = mockExamViewModel,
                                            onBack = { currentRoute = "subject_list" }
                                        )
                                        "settings" -> SettingsScreen()
                                        "profile" -> ProfileScreen(userRole = userRole, onRoleChange = {}, onBack = { currentRoute = "home" })
                                        "lab_report" -> LabReportScreen(labDao = labDao, reportRepository = reportRepository, onBack = { currentRoute = "home" })
                                        "sync_center" -> SyncCenterScreen(labDao = labDao, onBack = { currentRoute = "home" })
                                        "submission_detail" -> SubmissionDetailScreen(reportId = selectedReportId, onBack = { currentRoute = "teacher_workspace" })
                                    }
                                }
                            }
                            
                            WatermarkOverlay()
                            
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopEnd) {
                                SyncStatusIndicator(modifier = Modifier.clickable { currentRoute = "sync_center" })
                            }
                            
                            if (networkStatus == NetworkStatus.Lost || networkStatus == NetworkStatus.Unavailable) {
                                Box(modifier = Modifier.fillMaxSize().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                                    Text(
                                        text = "OFFLINE MODE: Using Cached Data",
                                        modifier = Modifier.fillMaxWidth().background(DangerRed).padding(8.dp),
                                        color = OffWhite,
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
