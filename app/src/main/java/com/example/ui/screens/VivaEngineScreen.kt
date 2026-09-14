package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VivaEngineScreen(
    subjectId: String,
    viewModel: VivaEngineViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isEvaluating by viewModel.isEvaluating.collectAsState()
    val score by viewModel.score.collectAsState()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(subjectId) {
        viewModel.startSession(subjectId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("AI Examiner", fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SafeGreen),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Score: $score",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(messages) { msg ->
                    MessageBubble(msg)
                }
                if (isEvaluating) {
                    item {
                        Text(
                            text = "Examiner is evaluating...",
                            color = TechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(2.dp, Ink)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your answer...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4,
                    enabled = !isEvaluating
                )
                
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitAnswer(inputText)
                            inputText = ""
                        }
                    },
                    enabled = !isEvaluating && inputText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = TechBlue)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: VivaMessage) {
    val isStudent = msg.sender == "STUDENT"
    val align = if (isStudent) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = when {
        isStudent -> TechBlue
        msg.status == "CORRECT" -> SafeGreen.copy(alpha = 0.2f)
        msg.status == "PARTIAL" -> AmberAccent.copy(alpha = 0.2f)
        msg.status == "INCORRECT" -> DangerRed.copy(alpha = 0.2f)
        msg.type == "QUESTION" -> CardBackground
        msg.type == "FOLLOW_UP" -> AmberAccent.copy(alpha = 0.1f)
        else -> Color.White
    }
    val textColor = if (isStudent) Color.White else Ink
    val borderColor = if (isStudent) Color.Transparent else Ink

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .then(
                    if (!isStudent) Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    else Modifier
                )
                .padding(12.dp)
        ) {
            if (!isStudent && msg.status.isNotEmpty()) {
                val statusColor = when (msg.status) {
                    "CORRECT" -> SafeGreen
                    "PARTIAL" -> AmberAccent
                    "INCORRECT" -> DangerRed
                    else -> Ink
                }
                Text(
                    text = msg.status,
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else if (!isStudent && msg.type == "QUESTION") {
                Text(
                    text = "QUESTION",
                    color = TechBlue,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            } else if (!isStudent && msg.type == "FOLLOW_UP") {
                Text(
                    text = "EXAMINER FOLLOW-UP",
                    color = AmberAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            Text(
                text = msg.text,
                color = textColor,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
