package com.example.ai.local

import com.example.ai.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Production-hardened Local Llama inference provider.
 * Implements strict memory safety, availability auditing, and cooperative cancellation.
 */
class LocalLlamaProvider(
    private val modelPathProvider: () -> String?
) : AiProvider {

    override val providerType: AiProviderType = AiProviderType.LOCAL

    private val activeModelHandle = AtomicLong(0L)
    private val loadedPath = AtomicReference<String?>(null)

    companion object {
        private const val TAG = "LocalLlamaProvider"
        // GGUF magic bytes: 'G', 'G', 'U', 'F' (0x46554747 in little-endian)
        private val GGUF_MAGIC = byteArrayOf(0x47, 0x47, 0x55, 0x46)
        private const val MIN_MODEL_BYTES = 1024 * 1024L // 1 MB minimal valid test model
        private const val MAX_MODEL_BYTES = 4L * 1024 * 1024 * 1024 // 4 GB safety ceiling for mobile
        private const val MIN_RAM_HEADROOM_MB = 256L // Minimum RAM headroom beyond model size
    }

    override fun supportsMultimodal(): Boolean = false

    override fun isAvailable(): Boolean {
        return getAvailabilityStatus() == ModelAvailabilityStatus.MODEL_READY
    }

    override fun getAvailabilityStatus(): ModelAvailabilityStatus {
        val path = modelPathProvider()?.trim()
        if (path.isNullOrBlank()) {
            return ModelAvailabilityStatus.MODEL_NOT_CONFIGURED
        }

        val file = File(path)
        if (!file.exists() || !file.isFile || !file.canRead() || file.length() < MIN_MODEL_BYTES) {
            return ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID
        }

        if (file.length() > MAX_MODEL_BYTES) {
            return ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID
        }

        // Verify GGUF magic header bytes
        if (!isValidGgufHeader(file)) {
            return ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID
        }

        // Verify available memory headroom
        if (!hasSufficientRamForModel(file.length())) {
            return ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID
        }

        // Verify native library readiness
        if (!LlamaNative.isNativeLibraryLoaded) {
            return ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID
        }

        return ModelAvailabilityStatus.MODEL_READY
    }

    private fun isValidGgufHeader(file: File): Boolean {
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(4)
                val read = fis.read(header)
                read == 4 && header.contentEquals(GGUF_MAGIC)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun hasSufficientRamForModel(modelSizeBytes: Long): Boolean {
        return try {
            val runtime = Runtime.getRuntime()
            val maxJvmMemory = runtime.maxMemory()
            val allocatedJvmMemory = runtime.totalMemory()
            val freeJvmMemory = runtime.freeMemory()
            val availableJvm = maxJvmMemory - allocatedJvmMemory + freeJvmMemory

            // On Android/Linux, check /proc/meminfo for device RAM if available
            val memInfo = File("/proc/meminfo")
            if (memInfo.exists() && memInfo.canRead()) {
                var memAvailableKb: Long = -1
                memInfo.forEachLine { line ->
                    if (line.startsWith("MemAvailable:")) {
                        val parts = line.split("\\s+".toRegex())
                        if (parts.size >= 2) {
                            memAvailableKb = parts[1].toLongOrNull() ?: -1
                        }
                    }
                }
                if (memAvailableKb > 0) {
                    val availableSystemBytes = memAvailableKb * 1024L
                    val requiredBytes = modelSizeBytes + (MIN_RAM_HEADROOM_MB * 1024 * 1024L)
                    return availableSystemBytes >= requiredBytes
                }
            }

            // Fallback: Ensure model file size does not exceed reasonable threshold
            modelSizeBytes <= MAX_MODEL_BYTES
        } catch (e: Exception) {
            true // If /proc/meminfo unreadable, allow verification to proceed to native loader
        }
    }

    @Synchronized
    suspend fun ensureModelLoaded(): Long = withContext(Dispatchers.IO) {
        val currentHandle = activeModelHandle.get()
        val path = modelPathProvider()?.trim()

        if (path.isNullOrBlank()) {
            throw IllegalStateException("Model path is not configured")
        }

        if (currentHandle != 0L && loadedPath.get() == path) {
            return@withContext currentHandle
        }

        // Unload previous model if path changed
        if (currentHandle != 0L) {
            LlamaNative.unloadModel(currentHandle)
            activeModelHandle.set(0L)
            loadedPath.set(null)
        }

        val file = File(path)
        if (!file.exists()) {
            throw IllegalArgumentException("Model file does not exist at $path")
        }
        if (!isValidGgufHeader(file)) {
            throw IllegalArgumentException("File at $path is not a valid GGUF model")
        }

        val handle = LlamaNative.loadModel(path)
        if (handle == 0L) {
            throw IllegalStateException("Native llama.cpp failed to load model from $path (returned null handle)")
        }

        activeModelHandle.set(handle)
        loadedPath.set(path)
        handle
    }

    @Synchronized
    fun unloadCurrentModel() {
        val handle = activeModelHandle.getAndSet(0L)
        if (handle != 0L) {
            LlamaNative.unloadModel(handle)
        }
        loadedPath.set(null)
    }

    override suspend fun generate(request: AiRequest): AiResult<AiResponse> = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        if (request.isMultimodal) {
            return@withContext AiResult.Failure.CapabilityUnsupported(
                capability = "multimodal_image",
                providerType = providerType,
                message = "Local Llama provider does not support image analysis. Use Cloud Gemini for vision tasks."
            )
        }

        val availability = getAvailabilityStatus()
        if (availability != ModelAvailabilityStatus.MODEL_READY) {
            return@withContext when (availability) {
                ModelAvailabilityStatus.MODEL_NOT_CONFIGURED ->
                    AiResult.Failure.ProviderUnavailable("Local model path is not configured", providerType)
                ModelAvailabilityStatus.MODEL_CONFIGURED_BUT_INVALID ->
                    AiResult.Failure.ModelLoadError("Local model file is invalid, missing, or exceeds device memory limits")
                ModelAvailabilityStatus.MODEL_READY ->
                    AiResult.Failure.ProviderUnavailable("Local model unavailable", providerType)
            }
        }

        val handle: Long
        try {
            handle = ensureModelLoaded()
        } catch (e: Exception) {
            return@withContext AiResult.Failure.ModelLoadError("Failed to initialize native model: ${e.message}")
        }

        val outputBuilder = StringBuilder()
        var errorOccurred: String? = null

        val callback = object : LlamaNative.TokenCallback {
            override fun onToken(token: String) {
                outputBuilder.append(token)
            }

            override fun onComplete() {
                // Done
            }

            override fun onError(error: String) {
                errorOccurred = error
            }
        }

        try {
            LlamaNative.generate(
                modelPtr = handle,
                prompt = request.prompt,
                systemPrompt = request.systemPrompt,
                callback = callback
            )
        } catch (e: Exception) {
            return@withContext AiResult.Failure.InferenceError(e, "Inference exception: ${e.message}")
        }

        if (errorOccurred != null) {
            return@withContext AiResult.Failure.InferenceError(null, errorOccurred!!)
        }

        val latency = System.currentTimeMillis() - startTime
        val fullText = outputBuilder.toString()
        val tokens = fullText.split("\\s+".toRegex()).size

        AiResult.Success(
            data = AiResponse(
                text = fullText,
                providerType = providerType,
                routingMode = AiRoutingMode.OFFLINE_ONLY,
                latencyMs = latency,
                tokensCount = tokens,
                isHeuristicFallback = false
            ),
            providerType = providerType
        )
    }

    override fun generateStream(request: AiRequest): Flow<AiStreamToken> = callbackFlow {
        if (request.isMultimodal) {
            trySend(AiStreamToken.Error(
                AiResult.Failure.CapabilityUnsupported(
                    capability = "multimodal_image",
                    providerType = providerType,
                    message = "Local Llama provider does not support image analysis."
                )
            ))
            close()
            return@callbackFlow
        }

        val availability = getAvailabilityStatus()
        if (availability != ModelAvailabilityStatus.MODEL_READY) {
            trySend(AiStreamToken.Error(
                AiResult.Failure.ProviderUnavailable(
                    "Local Llama is not ready: $availability",
                    providerType
                )
            ))
            close()
            return@callbackFlow
        }

        var handle = 0L
        try {
            handle = ensureModelLoaded()
        } catch (e: Exception) {
            trySend(AiStreamToken.Error(AiResult.Failure.ModelLoadError("Model init failed: ${e.message}")))
            close()
            return@callbackFlow
        }

        val stringAccumulator = StringBuilder()

        val callback = object : LlamaNative.TokenCallback {
            override fun onToken(token: String) {
                stringAccumulator.append(token)
                trySend(AiStreamToken.Token(token))
            }

            override fun onComplete() {
                trySend(AiStreamToken.Completion(stringAccumulator.toString(), providerType))
                close()
            }

            override fun onError(error: String) {
                trySend(AiStreamToken.Error(AiResult.Failure.InferenceError(null, error)))
                close()
            }
        }

        // Run inference on background Default dispatcher
        withContext(Dispatchers.Default) {
            try {
                LlamaNative.generate(
                    modelPtr = handle,
                    prompt = request.prompt,
                    systemPrompt = request.systemPrompt,
                    callback = callback
                )
            } catch (e: Exception) {
                trySend(AiStreamToken.Error(AiResult.Failure.InferenceError(e, "Generation error: ${e.message}")))
                close()
            }
        }

        awaitClose {
            // Signal native generation loop to stop if consumer cancels collection
            if (handle != 0L) {
                LlamaNative.cancel(handle)
            }
        }
    }
}
