package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.data.QuizResultEntity
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import kotlinx.coroutines.launch

data class Question(
  val prompt: String,
  val options: List<String>,
  val correctIndex: Int
)

val eceQuestions = listOf(
  Question(
    prompt = "What is the Barkhausen criterion for sustained oscillations?",
    options = listOf("Aβ = 1 with 0° or 360° phase shift", "Aβ = 0", "Aβ > 10", "Aβ = infinity"),
    correctIndex = 0
  ),
  Question(
    prompt = "Which diode is specifically designed to operate in the breakdown region?",
    options = listOf("LED", "Zener Diode", "Schottky Diode", "Photodiode"),
    correctIndex = 1
  ),
  Question(
    prompt = "What is the slew rate of an ideal operational amplifier?",
    options = listOf("Zero", "Finite", "Infinite", "1V/us"),
    correctIndex = 2
  ),
  Question(
    prompt = "In 8085 microprocessor, how many address lines are present?",
    options = listOf("8 lines", "16 lines", "32 lines", "20 lines"),
    correctIndex = 1
  )
)

@Composable
fun QuizScreen(labDao: LabDao, onBack: () -> Unit) {
  val scope = rememberCoroutineScope()
  var currentIndex by remember { mutableStateOf(0) }
  var score by remember { mutableStateOf(0) }
  var selectedOption by remember { mutableStateOf<Int?>(null) }
  var quizFinished by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NeoHeader(
      title = "Viva Voce Quiz",
      subtitle = "Assess Your ECE Core Competency"
    )

    if (!quizFinished) {
      val question = eceQuestions[currentIndex]

      NeoCard(backgroundColor = OffWhite) {
        Text(
          text = "QUESTION ${currentIndex + 1} OF ${eceQuestions.size}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = question.prompt,
          fontSize = 16.sp,
          fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        question.options.forEachIndexed { index, option ->
          val isSelected = selectedOption == index
          NeoButton(
            text = option,
            onClick = { selectedOption = index },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      NeoButton(
        text = if (currentIndex < eceQuestions.size - 1) "Next Question" else "Submit Quiz",
        onClick = {
          if (selectedOption != null) {
            val isCorrect = selectedOption == question.correctIndex
            val updatedScore = score + if (isCorrect) 1 else 0
            if (isCorrect) score = updatedScore
            
            val nextIndex = currentIndex + 1
            if (nextIndex < eceQuestions.size) {
              selectedOption = null
              currentIndex = nextIndex
            } else {
              quizFinished = true
              scope.launch {
                labDao.insertQuizResult(
                  QuizResultEntity(
                    quizTitle = "ECE Core Viva",
                    score = updatedScore,
                    totalQuestions = eceQuestions.size
                  )
                )
              }
            }
          }
        },
        enabled = selectedOption != null
      )
    } else {
      NeoCard(backgroundColor = OffWhite) {
        Text(text = "QUIZ COMPLETED!", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Your Score: $score / ${eceQuestions.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Result has been saved offline to Room DB.", fontSize = 13.sp)
      }

      Spacer(modifier = Modifier.weight(1f))

      NeoButton(
        text = "Retake Quiz",
        onClick = {
          currentIndex = 0
          score = 0
          quizFinished = false
          selectedOption = null
        }
      )
    }

      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
  }
}
