package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.GeminiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class VivaMessage(
    val sender: String, // "EXAMINER" or "STUDENT"
    val text: String,
    val type: String = "TEXT", // "TEXT", "QUESTION", "FEEDBACK", "FOLLOW_UP"
    val status: String = "" // "CORRECT", "PARTIAL", "INCORRECT", ""
)

class VivaEngineViewModel(private val dao: AcademicDao) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<VivaMessage>>(emptyList())
    val messages: StateFlow<List<VivaMessage>> = _messages.asStateFlow()

    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    private var currentQuestionIndex = 0

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private var currentFollowUpCount = 0
    private val MAX_FOLLOW_UP = 1

    private val geminiApi: GeminiApi? by lazy {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotEmpty() && apiKey != "YOUR_GEMINI_API_KEY") {
            GeminiApi(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )
        } else {
            null
        }
    }

    fun startSession(subjectId: String) {
        viewModelScope.launch {
            _messages.value = listOf(
                VivaMessage("EXAMINER", "Welcome to the interactive Viva session. Let's begin.", "TEXT")
            )
            _score.value = 0
            currentQuestionIndex = 0
            
            dao.getQuestionsForSubject(subjectId).collect { qList ->
                if (qList.isNotEmpty()) {
                    _questions.value = qList.shuffled().take(5) // Take 5 random questions
                    askNextQuestion()
                } else {
                    _messages.value = _messages.value + VivaMessage("EXAMINER", "No questions available for this subject.", "TEXT")
                }
            }
        }
    }

    private fun askNextQuestion() {
        if (currentQuestionIndex < _questions.value.size) {
            val q = _questions.value[currentQuestionIndex]
            _messages.value = _messages.value + VivaMessage(
                sender = "EXAMINER",
                text = "Question ${currentQuestionIndex + 1}: ${q.questionTextEnglish}",
                type = "QUESTION"
            )
            currentFollowUpCount = 0
        } else {
            _messages.value = _messages.value + VivaMessage(
                sender = "EXAMINER",
                text = "Session complete! Final Score: ${_score.value}/${_questions.value.size * 10}",
                type = "TEXT"
            )
        }
    }

    fun submitAnswer(answer: String) {
        if (answer.isBlank() || _isEvaluating.value || _questions.value.isEmpty()) return

        val userMessage = VivaMessage("STUDENT", answer, "TEXT")
        _messages.value = _messages.value + userMessage
        _isEvaluating.value = true

        viewModelScope.launch {
            val currentQ = _questions.value[currentQuestionIndex]
            evaluateAnswer(currentQ, answer)
        }
    }

    private suspend fun evaluateAnswer(question: QuestionEntity, answer: String) {
        var feedbackText = ""
        var status = "PARTIAL"
        var followUp = ""

        if (geminiApi != null) {
            try {
                val prompt = """
                    You are a strict but fair engineering examiner conducting a Viva Voce.
                    Question asked: "${question.questionTextEnglish}"
                    Official Answer Key: "${question.fullAnswerEnglish}"
                    Student's Answer: "$answer"
                    
                    Task:
                    1. Evaluate the student's answer conceptually.
                    2. Provide a single short paragraph of feedback.
                    3. If the answer is mostly correct but lacking depth, or if you want to push them, generate ONE follow-up question. 
                    4. Output EXACTLY in this format:
                    STATUS: [CORRECT / PARTIAL / INCORRECT]
                    FEEDBACK: [Your feedback]
                    FOLLOWUP: [Your follow-up question, or NONE]
                """.trimIndent()
                
                val response = geminiApi?.generateContent(prompt) ?: ""
                
                if (response.contains("STATUS: CORRECT")) status = "CORRECT"
                else if (response.contains("STATUS: INCORRECT")) status = "INCORRECT"
                else status = "PARTIAL"

                val feedbackMatch = Regex("FEEDBACK: (.*?)(FOLLOWUP:|\$)").find(response.replace("\n", " "))
                if (feedbackMatch != null) {
                    feedbackText = feedbackMatch.groupValues[1].trim()
                }

                val followUpMatch = Regex("FOLLOWUP: (.*)").find(response.replace("\n", " "))
                if (followUpMatch != null) {
                    val f = followUpMatch.groupValues[1].trim()
                    if (f != "NONE" && f.isNotEmpty()) {
                        followUp = f
                    }
                }

            } catch (e: Exception) {
                // Fallback on error
                status = mockEvaluate(answer, question.fullAnswerEnglish)
                feedbackText = "Error calling AI. Simulated evaluation applied."
            }
        } else {
            // Simulated evaluation
            delay(1500) // simulate thinking
            status = mockEvaluate(answer, question.fullAnswerEnglish)
            feedbackText = when(status) {
                "CORRECT" -> "Excellent conceptual understanding."
                "PARTIAL" -> "You have the right idea, but missed some key technical details. The correct answer involves: ${question.fullAnswerEnglish.take(50)}..."
                else -> "Incorrect. The correct concept is: ${question.fullAnswerEnglish}"
            }
            if (status == "PARTIAL" && currentFollowUpCount == 0) {
                followUp = "Can you elaborate more on the specific mechanism or law behind this?"
            }
        }

        if (status == "CORRECT") _score.value += 10
        else if (status == "PARTIAL") _score.value += 5

        _messages.value = _messages.value + VivaMessage(
            sender = "EXAMINER",
            text = feedbackText,
            type = "FEEDBACK",
            status = status
        )

        if (followUp.isNotEmpty() && currentFollowUpCount < MAX_FOLLOW_UP) {
            delay(1000)
            _messages.value = _messages.value + VivaMessage(
                sender = "EXAMINER",
                text = "Follow-up: $followUp",
                type = "FOLLOW_UP"
            )
            currentFollowUpCount++
        } else {
            delay(2000)
            currentQuestionIndex++
            askNextQuestion()
        }

        _isEvaluating.value = false
    }

    private fun mockEvaluate(answer: String, key: String): String {
        val ansLower = answer.lowercase()
        val keyLower = key.lowercase()
        val keywords = keyLower.split(" ").filter { it.length > 4 }
        
        var matchCount = 0
        for (word in keywords) {
            if (ansLower.contains(word)) matchCount++
        }
        
        val ratio = if (keywords.isEmpty()) 0f else matchCount.toFloat() / keywords.size
        
        return when {
            ratio > 0.4f -> "CORRECT"
            ratio > 0.15f -> "PARTIAL"
            else -> "INCORRECT"
        }
    }
}

class VivaEngineViewModelFactory(private val dao: AcademicDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VivaEngineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VivaEngineViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
