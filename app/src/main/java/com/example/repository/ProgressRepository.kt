package com.example.repository

import com.example.data.ExperimentProgressEntity
import com.example.data.LabDao
import com.example.data.SyncQueueEntity
import com.example.network.SupabaseExperimentProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ProgressRepository(
    private val labDao: LabDao,
    private val syncRepository: SyncRepository,
    private val authRepository: AuthRepository
) {
    suspend fun saveProgress(progress: ExperimentProgressEntity) = withContext(Dispatchers.IO) {
        val userId = authRepository.getCurrentUserId() ?: return@withContext
        val progressWithUser = progress.copy(userId = userId)
        
        // 1. Save locally to Room
        labDao.upsertProgress(progressWithUser)
        
        // 2. Prepare payload for Sync Queue
        val supabaseProgress = SupabaseExperimentProgress(
            userId = userId,
            experimentId = progress.experimentId,
            theoryCompleted = progress.theoryCompleted,
            circuitCompleted = progress.circuitCompleted,
            simulationCompleted = progress.simulationCompleted,
            observationCompleted = progress.observationCompleted,
            calculationCompleted = progress.calculationCompleted,
            vivaCompleted = progress.vivaCompleted,
            reportCompleted = progress.reportCompleted,
            overallProgress = progress.overallProgress
        )
        val payload = Json.encodeToString(supabaseProgress)
        
        // 3. Enqueue
        val syncOp = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            entityType = "experiment_progress",
            entityId = "${userId}_${progress.experimentId}",
            operation = "UPDATE",
            payload = payload
        )
        syncRepository.enqueueSyncOperation(syncOp)
    }
}
