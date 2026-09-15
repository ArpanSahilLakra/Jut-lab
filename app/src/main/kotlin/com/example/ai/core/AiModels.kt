package com.example.ai.core

/**
 * AI Provider identity.
 */
enum class AiProviderType {
    LOCAL,
    CLOUD
}

/**
 * Strict privacy & routing modes for AI inference.
 */
enum class AiRoutingMode {
    /**
     * Attempts Local Llama first if capable and available;
     * falls back to Cloud Gemini; returns typed failure if both unavailable.
     */
    AUTO,

    /**
     * Exclusively routes to Cloud Gemini. Never uses local inference.
     */
    ONLINE_ONLY,

    /**
     * Strict privacy mode. Exclusively routes to Local Llama.
     * NEVER initiates any network or cloud API requests.
     */
    OFFLINE_ONLY
}

/**
 * Local model availability states.
 */
enum class ModelAvailabilityStatus {
    /** No model path has been configured. */
    MODEL_NOT_CONFIGURED,

    /** Model path exists but file is missing, empty, unreadable, invalid header, or exceeds RAM. */
    MODEL_CONFIGURED_BUT_INVALID,

    /** Model file is valid GGUF, readable, fits memory, and native library is loaded. */
    MODEL_READY
}

/**
 * AI Request specification.
 */
data class AiRequest(
    val prompt: String,
    val systemPrompt: String = "",
    val imageBase64List: List<String> = emptyList(),
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512
) {
    val isMultimodal: Boolean
        get() = imageBase64List.isNotEmpty()
}

/**
 * AI Response metadata and payload.
 */
data class AiResponse(
    val text: String,
    val providerType: AiProviderType,
    val routingMode: AiRoutingMode,
    val latencyMs: Long = 0L,
    val tokensCount: Int = 0,
    val isHeuristicFallback: Boolean = false
)

/**
 * Typed Result hierarchy for all AI generation calls.
 * Errors NEVER masquerade as successful AI response strings.
 */
sealed class AiResult<out T> {
    data class Success<T>(
        val data: T,
        val providerType: AiProviderType,
        val isHeuristicFallback: Boolean = false
    ) : AiResult<T>()

    sealed class Failure : AiResult<Nothing>() {
        abstract val message: String

        data class ProviderUnavailable(
            override val message: String,
            val providerType: AiProviderType
        ) : Failure()

        data class CapabilityUnsupported(
            val capability: String,
            val providerType: AiProviderType,
            override val message: String = "Capability '$capability' is not supported by $providerType"
        ) : Failure()

        data class ModelNotFound(
            val path: String,
            override val message: String = "Model file not found at: $path"
        ) : Failure()

        data class ModelLoadError(
            override val message: String
        ) : Failure()

        data class InsufficientMemory(
            val requiredMb: Long,
            val availableMb: Long,
            override val message: String = "Insufficient RAM to load model: requires ${requiredMb}MB, available ${availableMb}MB"
        ) : Failure()

        data class ModelTooLarge(
            val modelBytes: Long,
            val maxAllowedBytes: Long,
            override val message: String = "Model file size (${modelBytes / (1024 * 1024)}MB) exceeds limit (${maxAllowedBytes / (1024 * 1024)}MB)"
        ) : Failure()

        data class NetworkError(
            val cause: Throwable? = null,
            override val message: String = cause?.localizedMessage ?: "Network error connecting to cloud AI"
        ) : Failure()

        data class InferenceError(
            val cause: Throwable? = null,
            override val message: String = cause?.localizedMessage ?: "Inference error during token generation"
        ) : Failure()

        data class Cancelled(
            override val message: String = "Generation cancelled by user"
        ) : Failure()
    }
}

/**
 * Streaming token events for real-time inference displays.
 */
sealed class AiStreamToken {
    data class Token(val text: String) : AiStreamToken()
    data class Completion(val fullText: String, val providerType: AiProviderType) : AiStreamToken()
    data class Error(val failure: AiResult.Failure) : AiStreamToken()
}
