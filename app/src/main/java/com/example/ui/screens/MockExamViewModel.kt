package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AcademicDao
import com.example.data.QuestionEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MockExamViewModel(private val dao: AcademicDao) : ViewModel() {
    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions: StateFlow<List<QuestionEntity>> = _questions.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _userAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val userAnswers: StateFlow<Map<String, String>> = _userAnswers.asStateFlow()

    private val _timeLeft = MutableStateFlow(15 * 60) // 15 minutes in seconds
    val timeLeft: StateFlow<Int> = _timeLeft.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun startExam(semesterId: String) {
        viewModelScope.launch {
            // Get questions for semester, take 5 random ones
            dao.getQuestionsForSemester(semesterId).collect { qList ->
                if (qList.isNotEmpty()) {
                    _questions.value = qList.shuffled().take(5)
                }
                _currentIndex.value = 0
                _userAnswers.value = emptyMap()
                _isFinished.value = false
                _timeLeft.value = 15 * 60
                startTimer()
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (_timeLeft.value > 0 && !_isFinished.value) {
                delay(1000)
                _timeLeft.value -= 1
                if (_timeLeft.value <= 0) {
                    finishExam()
                }
            }
        }
    }

    fun saveAnswer(questionId: String, answer: String) {
        val current = _userAnswers.value.toMutableMap()
        current[questionId] = answer
        _userAnswers.value = current
    }

    fun nextQuestion() {
        if (_currentIndex.value < _questions.value.size - 1) {
            _currentIndex.value += 1
        }
    }

    fun prevQuestion() {
        if (_currentIndex.value > 0) {
            _currentIndex.value -= 1
        }
    }

    fun finishExam() {
        _isFinished.value = true
    }
}

class MockExamViewModelFactory(private val dao: AcademicDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockExamViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MockExamViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
