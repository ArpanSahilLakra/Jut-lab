package com.example.ui.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.example.network.SyncManager
import com.example.network.SyncState

@Composable
fun rememberSyncState(): State<SyncState> {
    return SyncManager.syncState.collectAsState()
}
