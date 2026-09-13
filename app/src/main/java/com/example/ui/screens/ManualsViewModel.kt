package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExperimentEntity
import com.example.repository.ExperimentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ManualsViewModel(private val repository: ExperimentRepository) : ViewModel() {

    private val _experiments = MutableStateFlow<List<ExperimentEntity>>(emptyList())
    val experiments: StateFlow<List<ExperimentEntity>> = _experiments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadExperiments()
    }

    private fun loadExperiments() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Collect from the repository (which emits cached data)
            repository.getExperiments()
                .catch { e ->
                    // Handle error if needed
                    _isLoading.value = false
                }
                .collect { data ->
                    _experiments.value = data
                }
        }
        
        // Trigger a background sync
        viewModelScope.launch {
            repository.syncExperiments()
            _isLoading.value = false
        }
    }
}

class ManualsViewModelFactory(
    private val repository: ExperimentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ManualsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ManualsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
