package com.example.repository

import com.example.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepository {
    private val auth = SupabaseClient.client.auth

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUserOrNull()?.id
    }

    fun observeAuthState(): Flow<Boolean> {
        return auth.sessionStatus.map {
            it is io.github.jan.supabase.auth.status.SessionStatus.Authenticated
        }
    }

    suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            auth.signInWith(io.github.jan.supabase.auth.providers.Google)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
