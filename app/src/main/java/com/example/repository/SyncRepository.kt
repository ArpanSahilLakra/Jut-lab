package com.example.repository

import android.util.Log
import com.example.data.*
import com.example.network.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class SyncRepository(private val labDao: LabDao) {
    private val postgrest = SupabaseClient.client.postgrest
    private val authRepo = AuthRepository()

    suspend fun syncProfile() = withContext(Dispatchers.IO) {
        val userId = authRepo.getCurrentUserId() ?: return@withContext
        try {
            val remoteProfile = postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<SupabaseProfile>()
            
            if (remoteProfile != null) {
                val localProfile = StudentProfileEntity(
                    id = remoteProfile.id,
                    email = remoteProfile.email,
                    role = remoteProfile.role,
                    xp = remoteProfile.xp,
                    level = remoteProfile.level,
                    currentStreak = remoteProfile.currentStreak,
                    longestStreak = remoteProfile.longestStreak,
                    experimentsCompleted = remoteProfile.experimentsCompleted,
                    totalStudyTime = remoteProfile.totalStudyTime
                )
                labDao.upsertStudentProfile(localProfile)
            }
            SyncManager.updateState(SyncState.SUCCESS)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Sync failed: ${e.message}")
            SyncManager.updateState(SyncState.FAILED)
        }
    }

    suspend fun processPendingQueue() = withContext(Dispatchers.IO) {
        val pendingOps = labDao.getPendingSyncOperations()
        if (pendingOps.isEmpty()) return@withContext

        SyncManager.updateState(SyncState.SYNCING)

        for (op in pendingOps) {
            try {
                when (op.entityType) {
                    "experiment_progress" -> {
                        val progress = Json.decodeFromString<SupabaseExperimentProgress>(op.payload)
                        when (op.operation) {
                            "UPDATE", "INSERT" -> {
                                postgrest["experiment_progress"].upsert(progress)
                            }
                        }
                    }
                    "lab_report" -> {
                        val report = Json.decodeFromString<SupabaseLabReport>(op.payload)
                        when (op.operation) {
                            "UPDATE", "INSERT" -> {
                                postgrest["lab_reports"].upsert(report)
                            }
                        }
                    }
                    // Add other entity types here
                }
                labDao.deleteSyncOperation(op.id)
            } catch (e: Exception) {
                Log.e("SyncRepository", "Sync operation failed: ${e.message}")
                labDao.updateSyncStatus(op.id, "FAILED", System.currentTimeMillis(), e.message)
            }
        }
        
        val remainingOps = labDao.getPendingSyncOperations()
        if (remainingOps.isEmpty()) {
            SyncManager.updateState(SyncState.SUCCESS)
        } else {
            SyncManager.updateState(SyncState.FAILED)
        }
    }

    suspend fun enqueueSyncOperation(operation: SyncQueueEntity) = withContext(Dispatchers.IO) {
        labDao.insertSyncOperation(operation)
        SyncManager.addPendingChange()
        processPendingQueue()
    }
}
