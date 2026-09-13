package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getPendingSyncOperations(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncOperation(operation: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, attemptCount = attemptCount + 1, lastAttemptAt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String, timestamp: Long, error: String?)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteSyncOperation(id: String)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentActivities(userId: String, limit: Int = 10): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)
}
