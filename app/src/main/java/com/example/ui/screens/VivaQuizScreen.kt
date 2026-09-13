package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.data.VivaVoceEntity
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Ink
import kotlinx.coroutines.launch

import com.example.LocalAudioManager
import com.example.LocalHapticManager
import com.example.LocalAppPreferences
import com.example.audio.AppAudioManager
import com.example.ui.animations.AppAnimations
import androidx.compose.ui.platform.LocalView

@Composable
fun VivaQuizScreen(
  experimentId: String,
  labDao: LabDao,
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val audioManager = LocalAudioManager.current
  val hapticManager = LocalHapticManager.current
  val appPreferences = LocalAppPreferences.current
  val view = LocalView.current
  val reduceMotion by appPreferences.reduceMotion.collectAsState(initial = false)
  
  var questions by remember { mutableStateOf(listOf<VivaVoceEntity>()) }
  var currentIndex by remember { mutableStateOf(0) }
  var selectedOption by remember { mutableStateOf<Int?>(null) }
  var isAnswerRevealed by remember { mutableStateOf(false) }
  var score by remember { mutableStateOf(0) }
  var quizFinished by remember { mutableStateOf(false) }

  LaunchedEffect(experimentId) {
    questions = labDao.getVivaQuestionsForExperiment(experimentId)
  }

  if (questions.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator(color = Ink)
    }
    return
  }

  if (quizFinished) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(OffWhite)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        NeoHeader(
          title = "Viva Voce Completed",
          subtitle = "Performance Summary"
        )
      }
      item {
        NeoCard(backgroundColor = Color.White) {
          Text(text = "YOUR SCORE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "$score / ${questions.size}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = SafeGreen)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Great job! Your viva answers have been recorded for JUT evaluation.", fontSize = 14.sp)
        }
      }
      item {
      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
      }
    }
    return
  }

  val currentQ = questions[currentIndex]
  val options = listOf(currentQ.optionA, currentQ.optionB, currentQ.optionC, currentQ.optionD)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      NeoHeader(
        title = "Viva Voce Assessment",
        subtitle = "Question ${currentIndex + 1} of ${questions.size}"
      )
    }

    // Question Card
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = currentQ.question, fontSize = 16.sp, fontWeight = FontWeight.Black)
      }
    }

    // Options Cards
    items(options.size) { index ->
      val isSelected = selectedOption == index
      val isCorrect = index == currentQ.correctOptionIndex
      val cardColor = when {
        isAnswerRevealed && isCorrect -> Color(0xFFDCFCE7) // SafeGreen light
        isAnswerRevealed && isSelected && !isCorrect -> Color(0xFFFEE2E2) // DangerRed light
        isSelected -> Color(0xFFE2E8F0)
        else -> Color.White
      }

      NeoCard(
        backgroundColor = cardColor,
        modifier = Modifier.clickable(enabled = !isAnswerRevealed) {
          selectedOption = index
          isAnswerRevealed = true
          hapticManager.triggerHapticFeedback()
          if (index == currentQ.correctOptionIndex) {
            score++
            audioManager.playSound(AppAudioManager.SoundType.ANSWER_CORRECT, view)
          } else {
            audioManager.playSound(AppAudioManager.SoundType.ANSWER_INCORRECT, view)
          }
        }
      ) {
        Text(
          text = "${('A' + index)}. ${options[index]}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Hinglish Explanation Dropdown Card (if answered wrong)
    if (isAnswerRevealed) {
      item {
        AppAnimations.StandardAnimatedVisibility(
          visible = true,
          reduceMotion = reduceMotion
        ) {
          NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
            Text(
              text = if (selectedOption == currentQ.correctOptionIndex) "CORRECT!" else "PROFESSOR'S HINGLISH EXPLANATION",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = if (selectedOption == currentQ.correctOptionIndex) SafeGreen else DangerRed
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = currentQ.hinglishExplanation,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }

      item {
        NeoButton(
          text = if (currentIndex < questions.size - 1) "Next Question" else "Finish Quiz",
          onClick = {
            if (currentIndex < questions.size - 1) {
              currentIndex++
              selectedOption = null
              isAnswerRevealed = false
            } else {
              quizFinished = true
              scope.launch { labDao.insertQuizResult(com.example.data.QuizResultEntity(quizTitle = "Viva: ${currentQ.experimentId}", score = score, totalQuestions = questions.size)) }
            }
          },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    item {
      NeoButton(text = "Quit Quiz", onClick = onBack, modifier = Modifier.fillMaxWidth(), backgroundColor = DangerRed, textColor = Color.White)
    }
  }
}
