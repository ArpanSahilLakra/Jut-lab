package com.example.network

import android.util.Log
import com.example.data.LabDao
import com.example.data.StudentProfileEntity
import com.example.repository.AuthRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RealtimeManager(private val labDao: LabDao) {
    private val client = SupabaseClient.client
    private val authRepo = AuthRepository()
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var studentChannel: RealtimeChannel? = null
    private var teacherChannel: RealtimeChannel? = null

    fun connectAndSubscribe() {
        scope.launch {
            try {
                client.realtime.connect()
                Log.d("RealtimeManager", "Realtime connected")
                val profile = labDao.getStudentProfile().firstOrNull()
                val role = profile?.role
                if (role == "teacher") {
                    subscribeToTeacherData()
                } else {
                    subscribeToStudentData()
                }
            } catch (e: Exception) {
                Log.e("RealtimeManager", "Realtime connection failed: ${e.message}")
            }
        }
    }

    private suspend fun subscribeToTeacherData() {
        teacherChannel = client.channel("teacher-room")
        
        val submissionChanges = teacherChannel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "lab_reports"
        }

        submissionChanges.onEach { action ->
            Log.d("RealtimeManager", "Teacher received submission update")
            val teacherRepo = com.example.repository.TeacherRepository(labDao)
            teacherRepo.syncTeacherData()
        }.launchIn(scope)

        teacherChannel!!.subscribe()
        Log.d("RealtimeManager", "Subscribed to teacher channel")
    }

    private suspend fun subscribeToStudentData() {
        val userId = authRepo.getCurrentUserId() ?: return
        
        studentChannel = client.channel("student-room-$userId")
        
        val profileChanges = studentChannel!!.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
            table = "profiles"
        }

        profileChanges.onEach { action ->
            try {
                val profile = action.decodeRecord<SupabaseProfile>()
                if (profile.id == userId) {
                    labDao.upsertStudentProfile(
                        StudentProfileEntity(
                            id = profile.id,
                            email = profile.email,
                            role = profile.role,
                            xp = profile.xp,
                            level = profile.level,
                            currentStreak = profile.currentStreak,
                            longestStreak = profile.longestStreak,
                            experimentsCompleted = profile.experimentsCompleted,
                            totalStudyTime = profile.totalStudyTime
                        )
                    )
                    Log.d("RealtimeManager", "Profile updated via realtime")
                }
            } catch(e: Exception) {
                Log.e("RealtimeManager", "Failed to decode record: ${e.message}")
            }
        }.launchIn(scope)

        studentChannel!!.subscribe()
        Log.d("RealtimeManager", "Subscribed to student channel")
    }

    fun disconnect() {
        scope.launch {
            studentChannel?.unsubscribe()
            teacherChannel?.unsubscribe()
            client.realtime.disconnect()
            Log.d("RealtimeManager", "Realtime disconnected")
        }
    }
}
