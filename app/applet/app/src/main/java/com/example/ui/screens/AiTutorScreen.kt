package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Ink
import com.example.ui.theme.TechBlue
import com.example.util.SecureApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(val sender: String, val text: String)

@Composable
fun AiTutorScreen(onBack: () -> Unit, onNavigateToSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Gemini AI", "Hello! I am your JUT ECE Lab Assistant. Ask me about circuit design, amplifier gain, logic gates, or 8085 assembly.")
            )
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var hasKey by remember { mutableStateOf(SecureApiKeyStore.hasApiKey(context)) }

    // Refresh key status on resume
    LaunchedEffect(Unit) {
        hasKey = SecureApiKeyStore.hasApiKey(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NeoHeader(
            title = "Gemini AI Lab Tutor",
            subtitle = "Expert ECE Assistant & Circuit Debugger"
        )

        if (!hasKey) {
            NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "AI TUTOR NOT CONFIGURED", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                    Text(
                        text = "To use the AI Tutor, please configure your personal Gemini API key in Settings. Your key is stored securely on this device using Android Keystore encryption.",
                        fontSize = 13.sp,
                        color = Ink
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    NeoButton(
                        text = "Open AI Settings",
                        onClick = onNavigateToSettings,
                        backgroundColor = TechBlue,
                        textColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                NeoCard(
                    backgroundColor = if (msg.sender == "You") Color.White else OffWhite
                ) {
                    Text(text = msg.sender.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = msg.text, fontSize = 14.sp)
                }
            }
        }

        if (hasKey) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = { inputQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask an ECE question...") },
                    singleLine = true
                )
                NeoButton(
                    text = "Send",
                    onClick = {
                        if (inputQuery.isNotBlank() && !isLoading) {
                            val userText = inputQuery
                            inputQuery = ""
                            messages = messages + ChatMessage("You", userText)
                            isLoading = true
                            scope.launch {
                                val aiReply = callUserGeminiApi(context, userText)
                                messages = messages + ChatMessage("Gemini AI", aiReply)
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                )
            }
        }

        NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

suspend fun callUserGeminiApi(context: Context, prompt: String): String = withContext(Dispatchers.IO) {
    try {
        val apiKey = SecureApiKeyStore.getApiKey(context)
        if (apiKey.isNullOrBlank()) {
            return@withContext "AI Tutor is not configured. Please add your Gemini API key in Settings."
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", "You are an expert ECE Professor at JUT ECE Virtual Lab. Answer this ECE question concisely and accurately: $prompt")
                ))
            ))
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                return@withContext when (response.code) {
                    401, 403 -> "Invalid API key. Please check your Gemini API key in Settings."
                    429 -> "Quota exceeded or Rate limit reached. Please wait a moment and try again."
                    else -> "API Error (${response.code}): $errorBody"
                }
            }
            val responseString = response.body?.string() ?: return@withContext "Empty response."
            val json = JSONObject(responseString)
            val candidates = json.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            parts.getJSONObject(0).getString("text")
        }
    } catch (e: Exception) {
        "Offline or Connection Error: Unable to reach Gemini AI. Please check your Internet connection. (${e.localizedMessage})"
    }
}
