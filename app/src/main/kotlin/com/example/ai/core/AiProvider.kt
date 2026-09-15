package com.example.ai.core

import kotlinx.coroutines.flow.Flow

/**
 * Common abstraction for all AI inference backends (Cloud and Local).
 */
interface AiProvider {
    /** The provider type identifier (LOCAL or CLOUD). */
    val providerType: AiProviderType

    /** Whether the provider is currently ready to serve requests. */
    fun isAvailable(): Boolean

    /** Detailed availability status. */
    fun getAvailabilityStatus(): ModelAvailabilityStatus

    /** Whether this provider supports multimodal inputs (e.g. images). */
    fun supportsMultimodal(): Boolean

    /** Generates a complete response synchronously within a coroutine. */
    suspend fun generate(request: AiRequest): AiResult<AiResponse>

    /** Generates a streaming token flow. */
    fun generateStream(request: AiRequest): Flow<AiStreamToken>
}
