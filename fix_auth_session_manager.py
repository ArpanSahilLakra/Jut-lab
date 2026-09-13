import re

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'r') as f:
    text = f.read()

new_text = '''package com.example.repository

import android.util.Log
import com.example.data.LabDao
import com.example.network.SupabaseClient
import com.example.network.SupabaseProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface AuthState {
    data object CheckingSession : AuthState
    data class Authenticated(
        val userId: String,
        val email: String?,
        val role: String
    ) : AuthState
    data object Unauthenticated : AuthState
    data class Error(val message: String) : AuthState
}

object AuthSessionManager {
    private val _authState = MutableStateFlow<AuthState>(AuthState.CheckingSession)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val auth = SupabaseClient.client.auth
    private val postgrest = SupabaseClient.client.postgrest
    
    private var initializationJob: Job? = null
    
    fun initialize(labDao: LabDao, scope: CoroutineScope) {
        if (initializationJob?.isActive == true) return
        
        initializationJob = scope.launch(Dispatchers.IO) {
            try {
                // Wait for the session to be restored from local storage
                auth.awaitInitialization()
                
                // Observe session changes
                auth.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            val user = auth.currentUserOrNull()
                            if (user != null) {
                                // Fetch role from remote or local
                                try {
                                    val profile = postgrest["profiles"]
                                        .select { filter { eq("id", user.id) } }
                                        .decodeSingleOrNull<SupabaseProfile>()
                                    
                                    val role = profile?.role?.uppercase()
                                    if (role == "STUDENT" || role == "TEACHER") {
                                        _authState.value = AuthState.Authenticated(
                                            userId = user.id,
                                            email = user.email,
                                            role = role
                                        )
                                    } else {
                                        _authState.value = AuthState.Error("Account role is not configured or unknown")
                                    }
                                } catch (e: Exception) {
                                    Log.e("AuthSessionManager", "Error fetching profile", e)
                                    // Fallback to local DB if offline
                                    val localProfile = labDao.getStudentProfile().firstOrNull()
                                    if (localProfile != null && localProfile.id == user.id) {
                                        _authState.value = AuthState.Authenticated(
                                            userId = user.id,
                                            email = user.email,
                                            role = localProfile.role.uppercase()
                                        )
                                    } else {
                                        _authState.value = AuthState.Error("Offline profile missing for this user")
                                    }
                                }
                            } else {
                                _authState.value = AuthState.Unauthenticated
                            }
                        }
                        is SessionStatus.NotAuthenticated -> {
                            _authState.value = AuthState.Unauthenticated
                        }
                        is SessionStatus.RefreshFailure -> {
                            Log.e("AuthSessionManager", "Refresh failed: ${status.cause}")
                            if (status.cause is RefreshFailureCause.NetworkError) {
                                val user = auth.currentUserOrNull()
                                if (user != null) {
                                    val localProfile = labDao.getStudentProfile().firstOrNull()
                                    if (localProfile != null && localProfile.id == user.id) {
                                        _authState.value = AuthState.Authenticated(
                                            userId = user.id,
                                            email = user.email,
                                            role = localProfile.role.uppercase()
                                        )
                                    } else {
                                        _authState.value = AuthState.Error("Offline profile missing")
                                    }
                                } else {
                                    _authState.value = AuthState.Unauthenticated
                                }
                            } else {
                                auth.signOut()
                                _authState.value = AuthState.Unauthenticated
                            }
                        }
                        is SessionStatus.Initializing -> {
                            // Already handled by initial CheckingSession state
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthSessionManager", "Init error", e)
                _authState.value = AuthState.Error(e.message ?: "Failed to initialize auth")
            }
        }
    }
    
    fun explicitLogout(scope: CoroutineScope, labDao: LabDao, onComplete: () -> Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // Stop realtime subscriptions
                SupabaseClient.client.realtime.disconnect()
                auth.signOut()
                // Clear active user context from DB 
                // labDao.clearAllData() // If possible, but we don't have it.
            } catch (e: Exception) {
                Log.e("AuthSessionManager", "Logout error", e)
            } finally {
                _authState.value = AuthState.Unauthenticated
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
}
'''

with open('app/src/main/java/com/example/repository/AuthSessionManager.kt', 'w') as f:
    f.write(new_text)

