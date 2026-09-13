package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.data.LabDao
import com.example.network.RealtimeManager
import com.example.network.SyncManager
import com.example.network.SyncState
import com.example.repository.ExperimentRepository
import com.example.repository.SyncRepository
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SyncTrigger(labDao: LabDao) {
    val context = LocalContext.current
    val networkObserver = NetworkConnectivityObserver(context)
    val networkStatus by networkObserver.observe().collectAsState(initial = NetworkStatus.Available)
    
    LaunchedEffect(Unit) {
        val syncRepo = SyncRepository(labDao)
        val expRepo = ExperimentRepository(labDao)
        
        syncRepo.syncProfile()
        expRepo.syncExperiments()
        syncRepo.processPendingQueue()
        
        val realtimeManager = RealtimeManager(labDao)
        realtimeManager.connectAndSubscribe()
    }
    
    LaunchedEffect(networkStatus) {
        if (networkStatus == NetworkStatus.Available) {
            val syncRepo = SyncRepository(labDao)
            syncRepo.processPendingQueue()
        } else {
            SyncManager.updateState(SyncState.OFFLINE)
        }
    }
}
