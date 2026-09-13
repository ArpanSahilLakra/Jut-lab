package com.example.repository

import com.example.data.LabDao
import com.example.data.LabReportEntity
import com.example.data.SyncQueueEntity
import com.example.network.SupabaseLabReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ReportRepository(
    private val labDao: LabDao,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) {
    suspend fun saveReport(report: LabReportEntity) = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext
        val reportWithUser = report.copy(userId = userId)
        
        labDao.upsertLabReport(reportWithUser)
        
        val supabaseReport = SupabaseLabReport(
            id = report.id,
            userId = userId,
            experimentId = report.experimentId,
            title = report.title,
            aim = report.aim,
            apparatus = report.apparatus,
            theory = report.theory,
            circuitDescription = report.circuitDescription,
            observations = report.observations,
            calculations = report.calculations,
            result = report.result,
            conclusion = report.conclusion,
            vivaScore = report.vivaScore,
            status = report.status,
            grade = report.grade,
            feedback = report.feedback,
            reviewedAt = report.reviewedAt,
            createdAt = report.createdAt,
            updatedAt = report.updatedAt
        )
        val payload = Json.encodeToString(supabaseReport)
        
        val syncOp = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            entityType = "lab_report",
            entityId = report.id,
            operation = "UPDATE",
            payload = payload
        )
        syncRepository.enqueueSyncOperation(syncOp)
    }
}
