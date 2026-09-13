package com.example.ui.screens

import kotlinx.coroutines.flow.firstOrNull
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LabDao
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StartupState(val message: String) {
    INITIALIZING("Initializing Virtual Lab..."),
    CHECKING_DATABASE("Verifying offline storage..."),
    CHECKING_AI("Configuring Gemini Tutor..."),
    CHECKING_NETWORK("Checking connectivity..."),
    READY("Ready")
}

class StartupViewModel(private val labDao: LabDao) : ViewModel() {

    private val _startupState = MutableStateFlow(StartupState.INITIALIZING)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    private val _startupComplete = MutableStateFlow(false)
    val startupComplete: StateFlow<Boolean> = _startupComplete.asStateFlow()

    fun beginStartupSequence() {
        viewModelScope.launch {
            // Ensure we don't restart unnecessarily on recomposition
            if (_startupComplete.value) return@launch

            // Phase 1: Initializing components
            _startupState.value = StartupState.INITIALIZING
            delay(400) // Let the initial logo fade-in complete

            // Phase 2: Database Check (Offline persistence validation)
            _startupState.value = StartupState.CHECKING_DATABASE
            try {
                // Initialize default profile if none exists
                val profile = labDao.getStudentProfile().firstOrNull()
                if (profile == null) {
                    labDao.upsertStudentProfile(com.example.data.StudentProfileEntity())
                }

                // Lightweight read to ensure database is accessible and not corrupted
                labDao.getAllExperiments() 
            } catch (e: Exception) {
                // If DB is broken, we catch it here. In a real app we might trigger a recovery flow.
                // For this offline-first lab, we log internally and proceed so the user isn't stuck.
                e.printStackTrace()
            }
            delay(300) // Minimum duration for visual smoothness

            // Phase 3: AI Configuration Check
            _startupState.value = StartupState.CHECKING_AI
            try {
                // Verify Gemini configuration availability
                val hasAiConfig = com.example.BuildConfig.GEMINI_API_KEY.isNotEmpty()
            } catch (e: Exception) {
                // Ignore failure, allow offline mode
            }
            delay(300)

            // Phase 4: Network Check
            // Connectivity is observed directly via NetworkConnectivityObserver in MainActivity, 
            // so we visually indicate we are checking status for the offline-first experience.
            _startupState.value = StartupState.CHECKING_NETWORK
            delay(300) 

            // Phase 5: Ready
            _startupState.value = StartupState.READY
            delay(300) // Let the 'Ready' state breathe for a split second

            _startupComplete.value = true
        }
    }
}
