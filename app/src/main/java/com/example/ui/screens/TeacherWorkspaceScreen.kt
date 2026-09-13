package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.repository.TeacherRepository
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*

@Composable
fun TeacherWorkspaceScreen(labDao: LabDao, onNavigateToSubmission: (String) -> Unit = {}) {
    val students by labDao.getAllStudentSummaries().collectAsState(initial = emptyList())
    val submissions by labDao.getAllTeacherSubmissions().collectAsState(initial = emptyList())
    val allQuizResults by labDao.getAllQuizResults().collectAsState(initial = emptyList()) // Needs fixing later for all students
    
    LaunchedEffect(Unit) {
        val teacherRepo = TeacherRepository(labDao)
        teacherRepo.syncTeacherData()
    }

    val totalStudents = students.size
    val activeStudents = students.count { it.xp > 0 }
    val pendingSubmissions = submissions.size
    val completedExperiments = students.sumOf { it.completedExperiments }
    val avgScore = if (allQuizResults.isNotEmpty()) {
        allQuizResults.map { if (it.totalQuestions > 0) (it.score.toFloat() / it.totalQuestions) * 100 else 0f }.average()
    } else 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeoHeader(
                title = "Teacher Workspace",
                subtitle = "Aggregated Analytics & Submissions"
            )
        }

        item {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "AGGREGATED CLASS ANALYTICS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "TOTAL STUDENTS", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        Text(text = "$totalStudents", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Column {
                        Text(text = "ACTIVE STUDENTS", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        Text(text = "$activeStudents", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TechBlue)
                    }
                    Column {
                        Text(text = "PENDING SUBMISSIONS", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        Text(text = "$pendingSubmissions", fontSize = 18.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = "COMPLETED EXPERIMENTS", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        Text(text = "$completedExperiments", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                    Column {
                        Text(text = "AVERAGE QUIZ SCORE", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                        Text(text = "${avgScore.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (avgScore > 75) SafeGreen else DangerRed)
                    }
                }
            }
        }

        item {
            Text(text = "STUDENTS", fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 16.dp))
        }

        if (students.isEmpty()) {
            item {
                Text(text = "NO DATA YET", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f))
            }
        } else {
            items(students) { student ->
                NeoCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Text(text = student.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "XP: ${student.xp} • Level: ${student.level}", fontSize = 12.sp, color = TechBlue)
                    Text(text = "Completed: ${student.completedExperiments}", fontSize = 12.sp, color = SafeGreen)
                }
            }
        }

        item {
            Text(text = "RECENT SUBMISSIONS", fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 16.dp))
        }

        if (submissions.isEmpty()) {
            item {
                Text(text = "NO DATA YET", fontSize = 13.sp, color = Ink.copy(alpha = 0.6f))
            }
        } else {
            items(submissions) { sub ->
                NeoCard(backgroundColor = Color.White, modifier = Modifier.fillMaxWidth().clickable { onNavigateToSubmission(sub.id) }) {
                    Text(text = sub.experimentTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = "By ${sub.studentName}", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "STATUS: ${sub.status}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = AmberAccent)
                }
            }
        }
    }
}
