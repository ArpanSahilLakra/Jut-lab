package com.example.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class SyncState {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED,
    OFFLINE,
    PENDING
}

object SyncManager {
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState

    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime

    private val _pendingChanges = MutableStateFlow(0)
    val pendingChanges: StateFlow<Int> = _pendingChanges

    fun updateState(state: SyncState) {
        _syncState.value = state
        if (state == SyncState.SUCCESS) {
            _lastSyncTime.value = System.currentTimeMillis()
            _pendingChanges.value = 0
        }
    }

    fun addPendingChange() {
        _pendingChanges.value += 1
        if (_syncState.value != SyncState.OFFLINE) {
            _syncState.value = SyncState.PENDING
        }
    }
}
