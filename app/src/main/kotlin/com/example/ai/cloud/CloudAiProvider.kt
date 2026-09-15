package com.example.ai.cloud

import com.example.ai.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Production Cloud AI Provider connecting to Google Gemini.
 * Strictly distinguishes network/API errors from successful text tokens.
 */
class CloudAiProvider(
    private val apiKeyProvider: () -> String?,
    private val modelName: String = "gemini-1.5-flash"
) : AiProvider {

    override val providerType: AiProviderType = AiProviderType.CLOUD

    override fun supportsMultimodal(): Boolean = true

    override fun isAvailable(): Boolean {
        val key = apiKeyProvider()?.trim()
        return !key.isNullOrBlank() && key != "YOUR_GEMINI_API_KEY"
    }

    override fun getAvailabilityStatus(): ModelAvailabilityStatus {
        val key = apiKeyProvider()?.trim()
        return if (!key.isNullOrBlank() && key != "YOUR_GEMINI_API_KEY") {
            ModelAvailabilityStatus.MODEL_READY
        } else {
            ModelAvailabilityStatus.MODEL_NOT_CONFIGURED
        }
    }

    override suspend fun generate(request: AiRequest): AiResult<AiResponse> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val apiKey = apiKeyProvider()?.trim()

        if (apiKey.isNullOrBlank() || apiKey == "YOUR_GEMINI_API_KEY") {
            return@withContext AiResult.Failure.ProviderUnavailable(
                "Gemini API key is not configured. Add your API key in Secrets panel.",
                providerType
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true

            val payload = buildJsonPayload(request)
            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { os ->
                os.write(payload.toString())
                os.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                val errStream = conn.errorStream ?: conn.inputStream
                val errText = errStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                return@withContext AiResult.Failure.NetworkError(
                    null,
                    "Cloud API responded with HTTP $responseCode: $errText"
                )
            }

            val responseBody = conn.inputStream.bufferedReader().use { it.readText() }
            val extractedText = parseGeminiResponse(responseBody)

            val latency = System.currentTimeMillis() - startTime
            val tokens = extractedText.split("\\s+".toRegex()).size

            AiResult.Success(
                data = AiResponse(
                    text = extractedText,
                    providerType = providerType,
                    routingMode = AiRoutingMode.ONLINE_ONLY,
                    latencyMs = latency,
                    tokensCount = tokens,
                    isHeuristicFallback = false
                ),
                providerType = providerType
            )
        } catch (e: Exception) {
            AiResult.Failure.NetworkError(e, "Failed to connect to Cloud AI: ${e.localizedMessage}")
        }
    }

    override fun generateStream(request: AiRequest): Flow<AiStreamToken> = flow {
        // Stream wrapper around generate call or SSE
        when (val result = generate(request)) {
            is AiResult.Success -> {
                // Emit words as simulated streaming tokens for smooth UI transitions
                val words = result.data.text.split(" ")
                for (word in words) {
                    emit(AiStreamToken.Token("$word "))
                }
                emit(AiStreamToken.Completion(result.data.text, providerType))
            }
            is AiResult.Failure -> {
                emit(AiStreamToken.Error(result))
            }
        }
    }

    private fun buildJsonPayload(request: AiRequest): JSONObject {
        val root = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()

        if (request.systemPrompt.isNotBlank()) {
            val sysPart = JSONObject().put("text", "System: ${request.systemPrompt}\n\n")
            parts.put(sysPart)
        }

        // Add text prompt
        val textPart = JSONObject().put("text", request.prompt)
        parts.put(textPart)

        // Add multimodal images if present
        for (base64Img in request.imageBase64List) {
            val inlineData = JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", base64Img)
            val imgPart = JSONObject().put("inline_data", inlineData)
            parts.put(imgPart)
        }

        contentObj.put("parts", parts)
        contents.put(contentObj)
        root.put("contents", contents)

        val genConfig = JSONObject()
            .put("temperature", request.temperature.toDouble())
            .put("maxOutputTokens", request.maxTokens)
        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseGeminiResponse(json: String): String {
        val root = JSONObject(json)
        val candidates = root.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val candidate = candidates.getJSONObject(0)
        val content = candidate.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            sb.append(part.optString("text", ""))
        }
        return sb.toString()
    }
}
