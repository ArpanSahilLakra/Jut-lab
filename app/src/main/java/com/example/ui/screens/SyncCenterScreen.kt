package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.network.SyncManager
import com.example.network.SyncState
import com.example.repository.SyncRepository
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SyncCenterScreen(labDao: LabDao, onBack: () -> Unit) {
    val syncState by SyncManager.syncState.collectAsState()
    val pendingChanges by SyncManager.pendingChanges.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NeoHeader(
            title = "Sync Center",
            subtitle = "Connection & Data Status"
        )
        
        NeoCard(backgroundColor = Color.White) {
            Text(text = "CONNECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            val (color, text) = when (syncState) {
                SyncState.IDLE -> Triple(Color.Gray, "IDLE", false)
                SyncState.SYNCING -> Triple(TechBlue, "SYNCING", true)
                SyncState.SUCCESS -> Triple(SafeGreen, "ONLINE", false)
                SyncState.FAILED -> Triple(DangerRed, "SYNC ERROR", false)
                SyncState.OFFLINE -> Triple(DangerRed, "OFFLINE", false)
                SyncState.PENDING -> Triple(AmberAccent, "CHANGES PENDING", false)
            }
            Text(text = "● $text", color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
            
            if (syncState == SyncState.OFFLINE) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$pendingChanges CHANGES WAITING\nYour changes are saved locally and will sync automatically when online.",
                    fontSize = 14.sp,
                    color = DangerRed
                )
            }
        }
        
        NeoCard(backgroundColor = Color.White) {
            Text(text = "DATA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Experiments", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "✓", color = SafeGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "✓", color = SafeGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        NeoButton(
            text = "SYNC NOW",
            onClick = {
                scope.launch {
                    val repo = SyncRepository(labDao)
                    repo.processPendingQueue()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = TechBlue,
            textColor = Color.White
        )
        
        NeoButton(
            text = "BACK",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
