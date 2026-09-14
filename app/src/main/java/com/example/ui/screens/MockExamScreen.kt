package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.components.NeoButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockExamScreen(
    semesterId: String,
    viewModel: MockExamViewModel,
    onBack: () -> Unit
) {
    val questions by viewModel.questions.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val userAnswers by viewModel.userAnswers.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()

    LaunchedEffect(semesterId) {
        viewModel.startExam(semesterId)
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mock Exam", fontWeight = FontWeight.Black)
                        if (!isFinished) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (timeLeft < 300) DangerRed else TechBlue
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = timeString,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Exam")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Loading questions or not enough data...", color = Ink)
            }
        } else if (isFinished) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Exam Finished - Self Review",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TechBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Compare your answers with the official keys below.", color = Ink.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                items(questions) { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Ink)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Q: ${q.questionTextEnglish}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your Answer:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TechBlue
                            )
                            Text(
                                text = userAnswers[q.id] ?: "No answer provided.",
                                fontSize = 14.sp,
                                color = Ink.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Official Answer:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SafeGreen
                            )
                            Text(
                                text = q.fullAnswerEnglish,
                                fontSize = 14.sp,
                                color = Ink
                            )
                        }
                    }
                }
            }
        } else {
            val q = questions[currentIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of ${questions.size}",
                    fontWeight = FontWeight.Bold,
                    color = Ink.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = q.questionTextEnglish,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 26.sp,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                TextField(
                    value = userAnswers[q.id] ?: "",
                    onValueChange = { viewModel.saveAnswer(q.id, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(2.dp, Ink, RoundedCornerShape(8.dp)),
                    placeholder = { Text("Type your answer here...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NeoButton(
                        text = "Previous",
                        onClick = { viewModel.prevQuestion() },
                        enabled = currentIndex > 0,
                        backgroundColor = Color.White
                    )
                    
                    if (currentIndex == questions.size - 1) {
                        NeoButton(
                            text = "Submit Exam",
                            onClick = { viewModel.finishExam() },
                            backgroundColor = DangerRed,
                            textColor = Color.White
                        )
                    } else {
                        NeoButton(
                            text = "Next",
                            onClick = { viewModel.nextQuestion() },
                            backgroundColor = TechBlue,
                            textColor = Color.White
                        )
                    }
                }
            }
        }
    }
}
