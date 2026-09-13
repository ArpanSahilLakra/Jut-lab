package com.example.repository

import android.util.Log
import com.example.data.*
import com.example.network.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TeacherRepository(private val labDao: LabDao) {
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun syncTeacherData() = withContext(Dispatchers.IO) {
        try {
            // Fetch students
            val remoteStudents = postgrest["profiles"]
                .select { filter { eq("role", "student") } }
                .decodeList<SupabaseProfile>()
            
            val studentEntities = remoteStudents.map {
                StudentSummaryEntity(
                    id = it.id,
                    name = it.email, // using email as name for now if name is absent
                    email = it.email,
                    rollNumber = null,
                    xp = it.xp,
                    level = it.level,
                    lastActivityAt = 0L, // To be implemented with activity table
                    pendingSubmissions = 0, // To be implemented
                    completedExperiments = it.experimentsCompleted
                )
            }
            labDao.insertStudentSummaries(studentEntities)

            // Fetch submissions
            val remoteSubmissions = postgrest["lab_reports"]
                .select { filter { eq("status", "SUBMITTED") } }
                .decodeList<SupabaseLabReport>()
            
            val submissionEntities = remoteSubmissions.map {
                SubmissionEntity(
                    id = it.id,
                    studentId = it.userId,
                    studentName = "Student", // Would normally join or map
                    experimentId = it.experimentId,
                    experimentTitle = it.title,
                    submittedAt = it.updatedAt,
                    status = it.status
                )
            }
            labDao.insertTeacherSubmissions(submissionEntities)

            Log.d("TeacherRepository", "Successfully synced teacher data")
            SyncManager.updateState(SyncState.SUCCESS)
        } catch (e: Exception) {
            Log.e("TeacherRepository", "Failed to sync teacher data: ${e.message}")
            SyncManager.updateState(SyncState.FAILED)
        }
    }
}
