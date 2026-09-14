package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoCard
import com.example.ui.theme.*
import com.example.network.GeminiApi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val sender: String, 
    val text: String,
    val imageUri: Uri? = null,
    val imageBitmap: Bitmap? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(onBack: () -> Unit, onNavigateToSettings: () -> Unit = {}) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var inputQuery by remember { mutableStateOf("") }
  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
  
  var messages by remember {
    mutableStateOf(
      listOf(
        ChatMessage(
          "Gemini AI", 
          "Hello! I am your JUT ECE Lab Assistant powered by Gemini. Ask me about circuit design, or attach a photo of your breadboard/oscilloscope for analysis!"
        )
      )
    )
  }
  var isLoading by remember { mutableStateOf(false) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
      if (uri != null) {
          selectedImageUri = uri
          // Load bitmap for Gemini
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
              val source = ImageDecoder.createSource(context.contentResolver, uri)
              selectedBitmap = ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
          } else {
              @Suppress("DEPRECATION")
              selectedBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
          }
      }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(Color.White)
        .border(2.dp, Ink)
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
        }
        Text(
          text = "GEMINI LAB TUTOR",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(messages) { msg ->
        NeoCard(
          backgroundColor = if (msg.sender == "You") Color.White else AmberAccent.copy(alpha = 0.1f)
        ) {
          Text(text = msg.sender.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          if (msg.imageUri != null) {
              AsyncImage(
                  model = msg.imageUri,
                  contentDescription = "Attached Image",
                  modifier = Modifier
                      .fillMaxWidth()
                      .height(200.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .border(1.dp, Ink, RoundedCornerShape(8.dp)),
                  contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.height(8.dp))
          }
          Text(text = msg.text, fontSize = 14.sp, lineHeight = 20.sp)
        }
      }
      if (isLoading) {
          item {
             CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TechBlue)
          }
      }
    }

    // Input Area
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .border(2.dp, Ink)
        .padding(16.dp)
    ) {
      if (selectedImageUri != null) {
          Box(modifier = Modifier.padding(bottom = 8.dp)) {
              AsyncImage(
                  model = selectedImageUri,
                  contentDescription = "Selected Image",
                  modifier = Modifier
                      .size(64.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .border(1.dp, Ink)
              )
              IconButton(
                  onClick = { 
                      selectedImageUri = null 
                      selectedBitmap = null
                  },
                  modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Red, RoundedCornerShape(10.dp))
              ) {
                  Text("X", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
          }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
            onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.background(TechBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", tint = TechBlue)
        }
        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          modifier = Modifier.weight(1f),
          placeholder = { Text("Ask about your circuit...") },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = TechBlue,
            unfocusedBorderColor = Ink
          )
        )
        IconButton(
          onClick = {
            if (inputQuery.isNotBlank() || selectedBitmap != null) {
              val query = inputQuery
              val bmp = selectedBitmap
              val uri = selectedImageUri
              
              messages = messages + ChatMessage("You", query, uri, bmp)
              inputQuery = ""
              selectedImageUri = null
              selectedBitmap = null
              isLoading = true

              scope.launch {
                val response = callGeminiApi(query, bmp)
                messages = messages + ChatMessage("Gemini AI", response)
                isLoading = false
              }
            }
          },
          modifier = Modifier.background(SafeGreen, RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
        }
      }
    }
  }
}

suspend fun callGeminiApi(prompt: String, bitmap: Bitmap?): String = withContext(Dispatchers.IO) {
  try {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      kotlinx.coroutines.delay(1000)
      return@withContext "In demo mode (No API Key). If you attached a photo, Gemini 1.5 Pro would analyze this circuit to ensure you haven't shorted the power rail."
    }

        val modelName = if (bitmap != null) "gemini-1.5-pro" else "gemini-1.5-flash"
    val geminiApi = GeminiApi(
        modelName = modelName,
        apiKey = apiKey
    )
    val textPrompt = "You are an expert ECE Professor at JUT ECE Virtual Lab. Answer this concise lab question: $prompt"
    geminiApi.generateContent(textPrompt, bitmap)
  } catch (e: Exception) {
    "Offline: Unable to reach AI Server. ${e.localizedMessage}"
  }
}
