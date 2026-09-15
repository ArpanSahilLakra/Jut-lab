package com.example.ai.repository

import com.example.ai.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Unified AI Repository orchestrating local llama.cpp and cloud Gemini providers.
 * Enforces strict routing policies, privacy guarantees, and error typing.
 */
class AiRepository(
    private val localProvider: AiProvider,
    private val cloudProvider: AiProvider,
    private var defaultRoutingMode: AiRoutingMode = AiRoutingMode.AUTO
) {

    fun setRoutingMode(mode: AiRoutingMode) {
        defaultRoutingMode = mode
    }

    fun getRoutingMode(): AiRoutingMode = defaultRoutingMode

    /**
     * Resolves the target provider according to strict policy.
     * Guaranteed: OFFLINE_ONLY never returns cloudProvider.
     */
    fun resolveProvider(request: AiRequest, mode: AiRoutingMode = defaultRoutingMode): ProviderResolution {
        return when (mode) {
            AiRoutingMode.OFFLINE_ONLY -> {
                if (request.isMultimodal) {
                    ProviderResolution.Rejected(
                        AiResult.Failure.CapabilityUnsupported(
                            capability = "multimodal_image",
                            providerType = AiProviderType.LOCAL,
                            message = "Multimodal image queries require Cloud AI. In Offline-Only mode, network access is prohibited."
                        )
                    )
                } else if (!localProvider.isAvailable()) {
                    ProviderResolution.Rejected(
                        AiResult.Failure.ProviderUnavailable(
                            message = "Offline Mode: Local Llama model is not ready (${localProvider.getAvailabilityStatus()}). Cloud access is strictly prohibited.",
                            providerType = AiProviderType.LOCAL
                        )
                    )
                } else {
                    ProviderResolution.Selected(localProvider, AiRoutingMode.OFFLINE_ONLY)
                }
            }

            AiRoutingMode.ONLINE_ONLY -> {
                if (!cloudProvider.isAvailable()) {
                    ProviderResolution.Rejected(
                        AiResult.Failure.ProviderUnavailable(
                            message = "Online Mode: Cloud Gemini is unavailable or API key not configured.",
                            providerType = AiProviderType.CLOUD
                        )
                    )
                } else {
                    ProviderResolution.Selected(cloudProvider, AiRoutingMode.ONLINE_ONLY)
                }
            }

            AiRoutingMode.AUTO -> {
                if (request.isMultimodal) {
                    // Local does not support vision; route to Cloud
                    if (cloudProvider.isAvailable()) {
                        ProviderResolution.Selected(cloudProvider, AiRoutingMode.AUTO)
                    } else {
                        ProviderResolution.Rejected(
                            AiResult.Failure.CapabilityUnsupported(
                                capability = "multimodal_image",
                                providerType = AiProviderType.LOCAL,
                                message = "Multimodal image queries require Cloud AI, but Cloud AI is unavailable."
                            )
                        )
                    }
                } else if (localProvider.isAvailable()) {
                    ProviderResolution.Selected(localProvider, AiRoutingMode.AUTO)
                } else if (cloudProvider.isAvailable()) {
                    ProviderResolution.Selected(cloudProvider, AiRoutingMode.AUTO)
                } else {
                    ProviderResolution.Rejected(
                        AiResult.Failure.ProviderUnavailable(
                            message = "No AI provider is available. Local model status: ${localProvider.getAvailabilityStatus()}, Cloud status: ${cloudProvider.getAvailabilityStatus()}",
                            providerType = AiProviderType.LOCAL
                        )
                    )
                }
            }
        }
    }

    sealed class ProviderResolution {
        data class Selected(val provider: AiProvider, val effectiveMode: AiRoutingMode) : ProviderResolution()
        data class Rejected(val failure: AiResult.Failure) : ProviderResolution()
    }

    /**
     * Executes non-streaming generation with strict routing and typed errors.
     */
    suspend fun generate(request: AiRequest, mode: AiRoutingMode = defaultRoutingMode): AiResult<AiResponse> {
        return when (val resolution = resolveProvider(request, mode)) {
            is ProviderResolution.Rejected -> resolution.failure
            is ProviderResolution.Selected -> {
                when (val result = resolution.provider.generate(request)) {
                    is AiResult.Success -> {
                        // Attach accurate routing mode metadata
                        AiResult.Success(
                            data = result.data.copy(routingMode = resolution.effectiveMode),
                            providerType = result.providerType,
                            isHeuristicFallback = result.isHeuristicFallback
                        )
                    }
                    is AiResult.Failure -> {
                        // If in AUTO mode, local failed during inference, attempt cloud fallback
                        if (mode == AiRoutingMode.AUTO && resolution.provider.providerType == AiProviderType.LOCAL && cloudProvider.isAvailable()) {
                            val cloudResult = cloudProvider.generate(request)
                            if (cloudResult is AiResult.Success) {
                                return AiResult.Success(
                                    data = cloudResult.data.copy(routingMode = AiRoutingMode.AUTO),
                                    providerType = AiProviderType.CLOUD,
                                    isHeuristicFallback = false
                                )
                            }
                        }
                        result
                    }
                }
            }
        }
    }

    /**
     * Executes streaming generation with strict routing.
     */
    fun generateStream(request: AiRequest, mode: AiRoutingMode = defaultRoutingMode): Flow<AiStreamToken> {
        return when (val resolution = resolveProvider(request, mode)) {
            is ProviderResolution.Rejected -> flow {
                emit(AiStreamToken.Error(resolution.failure))
            }
            is ProviderResolution.Selected -> resolution.provider.generateStream(request)
        }
    }
}
