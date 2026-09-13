package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.components.XPProgressBar
import com.example.ui.theme.*

@Composable
fun StudentDashboardScreen(labDao: LabDao) {
    val profile by labDao.getStudentProfileById(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = null)
    val experimentsProgress by labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())
    val assignments by labDao.getAllAssignments().collectAsState(initial = emptyList())
    
    val streakCount = profile?.currentStreak ?: 0
    val xpPoints = profile?.xp ?: 0
    val completedCount = profile?.experimentsCompleted ?: 0
    val recentExperiment = experimentsProgress.maxByOrNull { it.lastOpenedAt }
    val pendingAssignments = assignments.count { it.status == "UPCOMING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeoHeader(
                title = "Student Dashboard",
                subtitle = "Track your laboratory progress"
            )
        }
        item {
            XPProgressBar(currentXp = xpPoints)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White
                ) {
                    Text(text = "STREAK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$streakCount Days", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                NeoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White
                ) {
                    Text(text = "LAB XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$xpPoints XP", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White
                ) {
                    Text(text = "PENDING TASKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$pendingAssignments Assignments", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                NeoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White
                ) {
                    Text(text = "LABS COMPLETED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$completedCount", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        item {
            NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
                Text(text = "LEARNING INSIGHTS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                Spacer(modifier = Modifier.height(6.dp))
                if (completedCount == 0) {
                    Text(
                        text = "Welcome to the Lab! Start your first experiment to generate insights.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Great job completing $completedCount experiments! Review your recent lab observations to strengthen your knowledge.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "RECENT ACTIVITY", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (recentExperiment != null) {
                    Text(text = recentExperiment.title.ifEmpty { recentExperiment.experimentId }, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(text = "Overall Progress: ${recentExperiment.overallProgress}%", fontSize = 12.sp, color = if (recentExperiment.overallProgress == 100) SafeGreen else TechBlue)
                } else {
                    Text(text = "No activity yet", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text(text = "Status: Not started", fontSize = 12.sp, color = Ink)
                }
            }
        }
    }
}
