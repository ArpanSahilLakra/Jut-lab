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
import com.example.network.SupabaseLabReport
import com.example.network.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SubmissionDetailScreen(reportId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var report by remember { mutableStateOf<SupabaseLabReport?>(null) }
    var grade by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        try {
            report = SupabaseClient.client.postgrest["lab_reports"]
                .select { filter { eq("id", reportId) } }
                .decodeSingleOrNull<SupabaseLabReport>()
            grade = report?.grade ?: ""
            feedback = report?.feedback ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NeoHeader(
            title = "Grade Submission",
            subtitle = report?.title ?: "Loading..."
        )

        if (report == null) {
            CircularProgressIndicator(color = Ink)
        } else {
            NeoCard(backgroundColor = Color.White) {
                Text(text = "STUDENT RESULT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report!!.result, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(text = "CONCLUSION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = report!!.conclusion, fontSize = 14.sp)
            }

            NeoCard(backgroundColor = Color.White) {
                Text(text = "GRADING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TechBlue)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = grade,
                    onValueChange = { grade = it },
                    label = { Text("Grade (e.g., A+, 95/100)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = report?.status != "REVIEWED"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text("Teacher Feedback") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = report?.status != "REVIEWED"
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (report?.status != "REVIEWED") {
                NeoButton(
                    text = if (isSaving) "SAVING..." else "SUBMIT GRADE",
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                val updated = report!!.copy(
                                    status = "REVIEWED",
                                    grade = grade,
                                    feedback = feedback,
                                    reviewedAt = System.currentTimeMillis()
                                )
                                SupabaseClient.client.postgrest["lab_reports"].upsert(updated)
                                onBack()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = TechBlue,
                    textColor = Color.White
                )
            }

            NeoButton(
                text = "BACK",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
