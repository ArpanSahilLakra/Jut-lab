package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class ChatMessage(val sender: String, val text: String)

@Composable
fun AiTutorScreen(onBack: () -> Unit) {
  val scope = rememberCoroutineScope()
  var inputQuery by remember { mutableStateOf("") }
  var messages by remember {
    mutableStateOf(
      listOf(
        ChatMessage("Gemini AI", "Hello! I am your JUT ECE Lab Assistant powered by Gemini. Ask me about circuit design, amplifier gain, logic gates, or 8085 assembly.")
      )
    )
  }
  var isLoading by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NeoHeader(
      title = "Gemini AI Lab Tutor",
      subtitle = "Expert ECE Assistant & Circuit Debugger"
    )

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
              val aiReply = callGeminiApi(userText)
              messages = messages + ChatMessage("Gemini AI", aiReply)
              isLoading = false
            }
          }
        },
        enabled = !isLoading
      )
    }

      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
  }
}

suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
  try {
    val apiKey = BuildConfig.GEMINI_API_KEY

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext getMockAiResponse(prompt)
    }

    val client = OkHttpClient()
    val jsonBody = JSONObject().apply {
      put("contents", JSONArray().put(
        JSONObject().put("parts", JSONArray().put(
          JSONObject().put("text", "You are an expert ECE Professor at JUT ECE Virtual Lab. Answer this question concisely and accurately: $prompt")
        ))
      ))
    }

    val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
      .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        val errorBody = response.body?.string() ?: "Unknown error"
        return@withContext "API Error (${response.code}): $errorBody"
      }
      val responseString = response.body?.string() ?: return@withContext "Empty response."
      val json = JSONObject(responseString)
      val candidates = json.getJSONArray("candidates")
      val content = candidates.getJSONObject(0).getJSONObject("content")
      val parts = content.getJSONArray("parts")
      parts.getJSONObject(0).getString("text")
    }
  } catch (e: Exception) {
    "Offline: Unable to reach AI Server. ${e.localizedMessage}"
  }
}

fun getMockAiResponse(query: String): String {
  val q = query.lowercase()
  return when {
    q.contains("amplifier") || q.contains("op-amp") -> "An Operational Amplifier (Op-Amp) is a high-gain electronic voltage amplifier with differential inputs. In inverting configuration, Gain A = -Rf / Rin."
    q.contains("oscillator") -> "An oscillator generates continuous periodic waveforms. The Barkhausen criteria require loop gain magnitude |Aβ| = 1 and total phase shift 0° or 360°."
    q.contains("diode") -> "A PN junction diode permits current flow in forward bias while blocking it in reverse bias until the breakdown voltage is reached."
    else -> "That is a fascinating ECE topic! In practical lab conditions at JUT ECE Virtual Lab, ensure proper biasing, verify ground connections, and use an oscilloscope to observe waveform distortions."
  }
}
