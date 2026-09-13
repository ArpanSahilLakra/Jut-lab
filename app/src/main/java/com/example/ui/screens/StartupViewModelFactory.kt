package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.LabDao

class StartupViewModelFactory(private val labDao: LabDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StartupViewModel(labDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
