package com.example.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseProfile(
    val id: String,
    val email: String,
    val role: String,
    val xp: Int,
    val level: Int,
    @SerialName("current_streak") val currentStreak: Int,
    @SerialName("longest_streak") val longestStreak: Int,
    @SerialName("experiments_completed") val experimentsCompleted: Int,
    @SerialName("total_study_time") val totalStudyTime: Long,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SupabaseExperiment(
    val id: String,
    val title: String,
    val description: String,
    val aim: String,
    val theory: String,
    val apparatus: String,
    val formula: String,
    val category: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SupabaseExperimentProgress(
    @SerialName("user_id") val userId: String,
    @SerialName("experiment_id") val experimentId: String,
    @SerialName("theory_completed") val theoryCompleted: Boolean,
    @SerialName("circuit_completed") val circuitCompleted: Boolean,
    @SerialName("simulation_completed") val simulationCompleted: Boolean,
    @SerialName("observation_completed") val observationCompleted: Boolean,
    @SerialName("calculation_completed") val calculationCompleted: Boolean,
    @SerialName("viva_completed") val vivaCompleted: Boolean,
    @SerialName("report_completed") val reportCompleted: Boolean,
    @SerialName("overall_progress") val overallProgress: Int,
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SupabaseLabReport(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("experiment_id") val experimentId: String,
    val title: String,
    val aim: String,
    val apparatus: String,
    val theory: String,
    @SerialName("circuit_description") val circuitDescription: String,
    val observations: String,
    val calculations: String,
    val result: String,
    val conclusion: String,
    @SerialName("viva_score") val vivaScore: Int,
    val status: String,
    val grade: String? = null,
    val feedback: String? = null,
    @SerialName("reviewed_at") val reviewedAt: Long? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
)

@Serializable
data class SupabaseBookmark(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    @SerialName("reference_id") val referenceId: String,
    val title: String,
    val description: String,
    @SerialName("created_at") val createdAt: Long
)
