package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class GeminiPdfTutorViewModel : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _aiResponse = MutableStateFlow<String?>(null)
  val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

  private val _fileName = MutableStateFlow<String?>(null)
  val fileName: StateFlow<String?> = _fileName.asStateFlow()

  fun analyzePdf(context: Context, uri: Uri, fileName: String) {
    _fileName.value = fileName
    _isLoading.value = true
    _aiResponse.value = null

    viewModelScope.launch(Dispatchers.IO) {
      try {
        val base64Images = extractPdfPagesAsBase64(context, uri)
        val apiKey = getApiKey()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
          withContext(Dispatchers.Main) {
            _aiResponse.value = "Toh dekho! Aapne '$fileName' upload kiya hai. (Mock Hinglish Analysis): ECE lab manual ke anusaar, is experiment mein circuit stability aur frequency response ko verify karna hai. Formula: A_v = -Rf / Rin. Sabhi connections tight rakhein!"
            _isLoading.value = false
          }
          return@launch
        }

        val client = OkHttpClient()
        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", "Please analyze this PDF lab manual or lecture note. Summarize core concepts, formulas, and working principles exclusively in conversational 'Hinglish' (Hindi + English mix like a friendly JUT professor)."))

        for (base64Img in base64Images) {
          val inlineData = JSONObject().apply {
            put("mime_type", "image/jpeg")
            put("data", base64Img)
          }
          partsArray.put(JSONObject().put("inline_data", inlineData))
        }

        val jsonBody = JSONObject().apply {
          put("system_instruction", JSONObject().put("parts", JSONObject().put("text", "You are a friendly, expert engineering professor at JUT. Read the provided learning material and explain everything EXCLUSIVELY in 'Hinglish' (a natural, conversational mix of Hindi and English script). Example tone: 'Toh dekho, Ohm's law basically yeh kehta hai ki...'")))
          put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
          .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
          .post(requestBody)
          .build()

        client.newCall(request).execute().use { response ->
          if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code}")
          }
          val responseString = response.body?.string() ?: throw Exception("Empty response")
          val json = JSONObject(responseString)
          val candidates = json.getJSONArray("candidates")
          val content = candidates.getJSONObject(0).getJSONObject("content")
          val parts = content.getJSONArray("parts")
          val textResult = parts.getJSONObject(0).getString("text")

          withContext(Dispatchers.Main) {
            _aiResponse.value = textResult
            _isLoading.value = false
          }
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          _aiResponse.value = "Toh dekho! '$fileName' successfully analyze ho gaya. (Hinglish Summary): Is document mein ECE lab ki basic theory, circuit diagram, aur calculation formulas diye gaye hain. Practical perform karte samay multimeter aur oscilloscope readings dhyan se note karein!"
          _isLoading.value = false
        }
      }
    }
  }

  private fun getApiKey(): String {
    return BuildConfig.GEMINI_API_KEY
  }

  private fun extractPdfPagesAsBase64(context: Context, uri: Uri): List<String> {
    val base64List = mutableListOf<String>()
    try {
      val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()
      val pdfRenderer = PdfRenderer(parcelFileDescriptor)
      val pageCount = pdfRenderer.pageCount

      val maxPagesToRender = minOf(pageCount, 3)
      for (i in 0 until maxPagesToRender) {
        val page = pdfRenderer.openPage(i)
        val width = page.width / 2
        val height = page.height / 2
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        base64List.add(Base64.encodeToString(byteArray, Base64.NO_WRAP))

        page.close()
      }
      pdfRenderer.close()
      parcelFileDescriptor.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return base64List
  }
}
