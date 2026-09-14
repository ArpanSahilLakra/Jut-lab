package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExperimentEntity
import com.example.data.ExperimentProgressEntity
import com.example.data.LabDao
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.max

enum class ExpStep(val title: String) {
    THEORY("Theory"),
    CIRCUIT("Circuit & Sim"),
    OBSERVATION("Observe & Graph"),
    VIVA("Viva"),
    REPORT("Report")
}

@Composable
fun DynamicExperimentScreen(
  experimentId: String,
  labDao: LabDao,
  progressRepository: com.example.repository.ProgressRepository,
  reportRepository: com.example.repository.ReportRepository,
  onNavigateToSimulator: (String) -> Unit,
  onNavigateToQuiz: (String) -> Unit,
  onNavigateToReport: (String) -> Unit = {},
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  var experiment by remember { mutableStateOf<ExperimentEntity?>(null) }
  var progress by remember { mutableStateOf<ExperimentProgressEntity?>(null) }
  var currentStep by remember { mutableStateOf(ExpStep.THEORY) }

  LaunchedEffect(experimentId) {
    experiment = labDao.getExperimentById(experimentId)
    val p = labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").firstOrNull()?.find { it.experimentId == experimentId }
    if (p == null && experiment != null) {
        val newProgress = ExperimentProgressEntity(experimentId = experimentId, title = experiment!!.title)
        progressRepository.saveProgress(newProgress)
        progress = newProgress
    } else {
        progress = p
        // Update last opened at
        progress?.let { progressRepository.saveProgress(it.copy(lastOpenedAt = System.currentTimeMillis())) }
    }
  }

  val currentExp = experiment
  if (currentExp == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = Ink)
    }
    return
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
  ) {
    NeoHeader(
      title = currentExp.title,
      subtitle = "Category: ${currentExp.category}"
    )

    // Stepper / Tabs
    ScrollableTabRow(
        selectedTabIndex = ExpStep.values().indexOf(currentStep),
        containerColor = Color.White,
        contentColor = Ink,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[ExpStep.values().indexOf(currentStep)]),
                color = TechBlue,
                height = 3.dp
            )
        }
    ) {
        ExpStep.values().forEach { step ->
            Tab(
                selected = currentStep == step,
                onClick = { currentStep = step },
                text = { Text(step.title, fontWeight = FontWeight.Bold) }
            )
        }
    }

    Box(modifier = Modifier.weight(1f).padding(16.dp)) {
        when(currentStep) {
            ExpStep.THEORY -> TheoryContent(currentExp, progress) { 
                scope.launch {
                    progress = progress?.copy(theoryCompleted = true, overallProgress = max(progress?.overallProgress ?: 0, 20))
                    progress?.let { progressRepository.saveProgress(it) }
                    currentStep = ExpStep.CIRCUIT
                }
            }
            ExpStep.CIRCUIT -> CircuitContent(currentExp, progress, onNavigateToSimulator) {
                scope.launch {
                    progress = progress?.copy(circuitCompleted = true, simulationCompleted = true, overallProgress = max(progress?.overallProgress ?: 0, 40))
                    progress?.let { progressRepository.saveProgress(it) }
                    currentStep = ExpStep.OBSERVATION
                }
            }
            ExpStep.OBSERVATION -> ObservationContent(currentExp, progress) {
                scope.launch {
                    progress = progress?.copy(observationCompleted = true, calculationCompleted = true, overallProgress = max(progress?.overallProgress ?: 0, 70))
                    progress?.let { progressRepository.saveProgress(it) }
                    currentStep = ExpStep.VIVA
                }
            }
            ExpStep.VIVA -> VivaContent(currentExp, progress, onNavigateToQuiz) {
                scope.launch {
                    progress = progress?.copy(vivaCompleted = true, overallProgress = max(progress?.overallProgress ?: 0, 85))
                    progress?.let { progressRepository.saveProgress(it) }
                    currentStep = ExpStep.REPORT
                }
            }
            ExpStep.REPORT -> ReportContent(currentExp, progress, onNavigateToReport) {
                scope.launch {
                    progress = progress?.copy(reportCompleted = true, overallProgress = 100, completedAt = System.currentTimeMillis())
                    val reportId = java.util.UUID.randomUUID().toString()
                    val newReport = com.example.data.LabReportEntity(
                        id = reportId,
                        experimentId = currentExp.id,
                        title = currentExp.title,
                        aim = currentExp.aim,
                        apparatus = currentExp.apparatusCommaSeparated,
                        theory = currentExp.theory,
                        circuitDescription = "Circuit successfully simulated.",
                        observations = "Recorded V-I characteristics via Interactive Sandbox.",
                        calculations = currentExp.formula,
                        result = "Experiment successfully executed.",
                        conclusion = "All theoretical principles verified.",
                        vivaScore = 10,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    reportRepository.saveReport(newReport)
                    progress?.let { progressRepository.saveProgress(it) }
                    
                    // Increment completed count in profile
                    val profile = labDao.getStudentProfile().firstOrNull()
                    if (profile != null && progress?.overallProgress != 100) {
                        labDao.upsertStudentProfile(profile.copy(
                            experimentsCompleted = profile.experimentsCompleted + 1,
                            xp = profile.xp + 100
                        ))
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.padding(16.dp)) {
        NeoButton(text = "Back to Experiments", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
  }
}

@Composable
fun TheoryContent(exp: ExperimentEntity, progress: ExperimentProgressEntity?, onComplete: () -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "AIM", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TechBlue)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = exp.aim, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "APPARATUS", fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = exp.apparatusCommaSeparated, fontSize = 13.sp)
            }
        }
        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "THEORY", fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = exp.theory, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Formula: ${exp.formula}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
            }
        }
        item {
            NeoButton(
                text = "Mark Theory as Read",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (progress?.theoryCompleted == true) SafeGreen else TechBlue,
                textColor = Color.White
            )
        }
    }
}

@Composable
fun CircuitContent(exp: ExperimentEntity, progress: ExperimentProgressEntity?, onNavigateToSimulator: (String) -> Unit, onComplete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NeoCard(backgroundColor = Color.White) {
            Text(text = "CIRCUIT SIMULATION", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Open the virtual simulator to build the circuit based on the given theory. Make sure you apply correct voltages.", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            NeoButton(
                text = "Open Virtual Simulator",
                onClick = { onNavigateToSimulator(if (exp.category == "Digital Electronics") "logic_gate" else "breadboard") },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TechBlue,
                textColor = Color.White
            )
        }
        NeoButton(
            text = "Mark Simulation as Completed",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (progress?.simulationCompleted == true) SafeGreen else Ink,
            textColor = Color.White
        )
    }
}

@Composable
fun ObservationContent(exp: ExperimentEntity, progress: ExperimentProgressEntity?, onComplete: () -> Unit) {
    var voltageInput by remember { mutableStateOf("") }
    var currentInput by remember { mutableStateOf("") }
    var observationList by remember { mutableStateOf(listOf<Pair<Float, Float>>()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
                Text(text = "OBSERVATION TABLE", fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = voltageInput,
                        onValueChange = { voltageInput = it },
                        label = { Text("Voltage (V)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        label = { Text("Current (mA)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                NeoButton(
                    text = "Add Reading",
                    onClick = {
                        val v = voltageInput.toFloatOrNull()
                        val i = currentInput.toFloatOrNull()
                        if (v != null && i != null) {
                            observationList = observationList + Pair(v, i)
                            voltageInput = ""
                            currentInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        if (observationList.isNotEmpty()) {
            item {
                NeoCard(backgroundColor = Color.White) {
                    Text(text = "RECORDED READINGS", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    observationList.forEachIndexed { index, reading ->
                        Text(text = "${index + 1}. V = ${reading.first} V, I = ${reading.second} mA", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
            
            item {
                NeoCard(backgroundColor = Color.White) {
                    Text(text = "V-I CHARACTERISTICS GRAPH", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).border(1.dp, Ink).background(OffWhite)) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val maxV = observationList.maxOfOrNull { it.first } ?: 10f
                            val maxI = observationList.maxOfOrNull { it.second } ?: 10f
                            
                            // Draw axes
                            drawLine(
                                color = Ink,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = Ink,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 2f
                            )
                            
                            if (maxV > 0 && maxI > 0) {
                                val points = observationList.sortedBy { it.first }
                                for (i in 0 until points.size - 1) {
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val x1 = (p1.first / maxV) * size.width
                                    val y1 = size.height - ((p1.second / maxI) * size.height)
                                    val x2 = (p2.first / maxV) * size.width
                                    val y2 = size.height - ((p2.second / maxI) * size.height)
                                    
                                    drawLine(
                                        color = TechBlue,
                                        start = Offset(x1, y1),
                                        end = Offset(x2, y2),
                                        strokeWidth = 4f
                                    )
                                    drawCircle(color = DangerRed, radius = 6f, center = Offset(x1, y1))
                                }
                                // Draw last point
                                val lastP = points.last()
                                val lastX = (lastP.first / maxV) * size.width
                                val lastY = size.height - ((lastP.second / maxI) * size.height)
                                drawCircle(color = DangerRed, radius = 6f, center = Offset(lastX, lastY))
                            }
                        }
                    }
                }
            }
        }

        item {
            NeoButton(
                text = "Mark Observations Completed",
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (progress?.observationCompleted == true) SafeGreen else Ink,
                textColor = Color.White
            )
        }
    }
}

@Composable
fun VivaContent(exp: ExperimentEntity, progress: ExperimentProgressEntity?, onNavigateToQuiz: (String) -> Unit, onComplete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NeoCard(backgroundColor = Color.White) {
            Text(text = "VIVA VOCE", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Test your understanding of the concepts and simulation results before generating the final report.", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            NeoButton(
                text = "Start Viva Quiz",
                onClick = { onNavigateToQuiz(exp.id) },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TechBlue,
                textColor = Color.White
            )
        }
        NeoButton(
            text = "Mark Viva as Completed",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (progress?.vivaCompleted == true) SafeGreen else Ink,
            textColor = Color.White
        )
    }
}

@Composable
fun ReportContent(exp: ExperimentEntity, progress: ExperimentProgressEntity?, onNavigateToReport: (String) -> Unit, onComplete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        NeoCard(backgroundColor = Color.White) {
            Text(text = "LAB REPORT", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Compile all your progress into a formal Lab Report and export it as a PDF.", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            NeoButton(
                text = "View & Export Report",
                onClick = { onNavigateToReport(exp.id) },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = TechBlue,
                textColor = Color.White
            )
        }
        NeoButton(
            text = "Finish Experiment",
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (progress?.overallProgress == 100) SafeGreen else Ink,
            textColor = Color.White
        )
    }
}
