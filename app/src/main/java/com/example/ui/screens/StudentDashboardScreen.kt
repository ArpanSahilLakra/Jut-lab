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
    // For the Academic App, we will show generic analytics for the semester.
    val xpPoints = 1250
    val streakCount = 4
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeoHeader(
                title = "Analytics Dashboard",
                subtitle = "Semester 1 Progress Tracking"
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
                    Text(text = "STUDY STREAK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$streakCount Days", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                NeoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color.White
                ) {
                    Text(text = "ACADEMIC XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechBlue)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$xpPoints XP", fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        
        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "SUBJECT READINESS (AI ESTIMATE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(modifier = Modifier.height(16.dp))
                
                SubjectProgressRow("Programming (ESPP1)", 0.8f, TechBlue)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Electrical (ESEE1)", 0.65f, AmberAccent)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Mechanics (ESEM1)", 0.4f, DangerRed)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Physics (BSP01)", 0.9f, SafeGreen)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Math I (BSM01)", 0.75f, TechBlue)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Indian Knowledge (HSM01)", 0.95f, SafeGreen)
                Spacer(modifier = Modifier.height(8.dp))
                SubjectProgressRow("Data Vis (VSC01)", 0.5f, AmberAccent)
            }
        }
        
        item {
            NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
                Text(text = "AI INSIGHTS", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your retention in Physics and Programming is excellent. However, you should focus on Engineering Mechanics (ESEM1) before the mid-semester exams. We recommend taking a Mock Exam.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SubjectProgressRow(name: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.8f))
            Text(text = "${(progress * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
