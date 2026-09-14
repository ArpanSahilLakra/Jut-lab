package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AcademicViewModel(private val dao: AcademicDao) : ViewModel() {

    private val _allProgress = MutableStateFlow<List<StudyProgressEntity>>(emptyList())
    val allProgress: StateFlow<List<StudyProgressEntity>> = _allProgress.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllStudyProgress().collect {
                _allProgress.value = it
            }
        }
    }

    private val _semesters = MutableStateFlow<List<SemesterEntity>>(emptyList())
    val semesters: StateFlow<List<SemesterEntity>> = _semesters.asStateFlow()

    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects.asStateFlow()

    private val _units = MutableStateFlow<List<UnitEntity>>(emptyList())
    val units: StateFlow<List<UnitEntity>> = _units.asStateFlow()

    private val _topics = MutableStateFlow<List<TopicEntity>>(emptyList())
    val topics: StateFlow<List<TopicEntity>> = _topics.asStateFlow()

    private val _currentSubject = MutableStateFlow<SubjectEntity?>(null)
    val currentSubject: StateFlow<SubjectEntity?> = _currentSubject.asStateFlow()

    fun loadSemesters(programId: String = "jut_ece_dip") {
        viewModelScope.launch {
            dao.getSemestersForProgram(programId).collect {
                _semesters.value = it
            }
        }
    }

    fun loadSubjects(semesterId: String) {
        viewModelScope.launch {
            dao.getSubjectsForSemester(semesterId).collect {
                _subjects.value = it
            }
        }
    }

    fun loadUnits(subjectId: String) {
        viewModelScope.launch {
            dao.getUnitsForSubject(subjectId).collect {
                _units.value = it
            }
        }
    }

    fun loadTopics(unitId: String) {
        viewModelScope.launch {
            dao.getTopicsForUnit(unitId).collect {
                _topics.value = it
            }
        }
    }

    fun loadSubjectDetails(subjectId: String) {
        viewModelScope.launch {
            _currentSubject.value = dao.getSubjectById(subjectId)
        }
    }

    private val _topicProgress = MutableStateFlow<StudyProgressEntity?>(null)
    val topicProgress: StateFlow<StudyProgressEntity?> = _topicProgress.asStateFlow()

    fun loadTopicProgress(topicId: String) {
        viewModelScope.launch {
            dao.getTopicProgress(topicId).collect {
                _topicProgress.value = it
            }
        }
    }

    fun markTopicComplete(topicId: String) {
        viewModelScope.launch {
            val existing = _topicProgress.value
            val progress = existing?.copy(
                completed = true,
                completedAt = System.currentTimeMillis(),
                lastAccessedAt = System.currentTimeMillis()
            ) ?: StudyProgressEntity(
                id = topicId,
                topicId = topicId,
                completed = true,
                completedAt = System.currentTimeMillis()
            )
            dao.insertStudyProgress(progress)
        }
    }
}


class AcademicViewModelFactory(private val dao: AcademicDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AcademicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AcademicViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
