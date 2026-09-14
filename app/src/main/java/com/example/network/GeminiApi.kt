package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
) {
    val text: String?
        get() = candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
}

@Serializable
data class GeminiCandidate(
    val content: GeminiContent
)

class GeminiApi(private val modelName: String, private val apiKey: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateContent(text: String, bitmap: Bitmap? = null): String {
        return withContext(Dispatchers.IO) {
            val parts = mutableListOf<GeminiPart>()
            
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", base64String)))
            }
            
            parts.add(GeminiPart(text = text))

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(parts = parts))
            )

            val jsonBody = json.encodeToString(requestBody)
            
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful) {
                throw Exception("API Error: ${response.code} $responseBody")
            }
            
            if (responseBody != null) {
                val parsed = json.decodeFromString<GeminiResponse>(responseBody)
                parsed.text ?: throw Exception("Empty response from AI")
            } else {
                throw Exception("Empty response body")
            }
        }
    }
}
