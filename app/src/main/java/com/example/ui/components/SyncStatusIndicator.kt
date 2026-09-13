package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.SyncManager
import com.example.network.SyncState
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TechBlue
import com.example.ui.theme.AmberAccent

@Composable
fun SyncStatusIndicator(modifier: Modifier = Modifier) {
    val syncState by SyncManager.syncState.collectAsState()

    val (color, text, showSpinner) = when (syncState) {
        SyncState.IDLE -> Triple(Color.Gray, "Idle", false)
        SyncState.SYNCING -> Triple(TechBlue, "Syncing", true)
        SyncState.SUCCESS -> Triple(SafeGreen, "Synced", false)
        SyncState.FAILED -> Triple(DangerRed, "Sync Failed", false)
        SyncState.OFFLINE -> Triple(DangerRed, "Offline", false)
        SyncState.PENDING -> Triple(AmberAccent, "Changes Pending", false)
    }

    Box(
        modifier = modifier
            .background(color = color, shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showSpinner) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(12.dp).padding(end = 4.dp)
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
