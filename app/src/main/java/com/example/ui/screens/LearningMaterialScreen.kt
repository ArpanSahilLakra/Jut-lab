package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import com.example.ui.theme.TechBlue
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Ink

@Composable
fun LearningMaterialScreen(
  onBack: () -> Unit,
  viewModel: GeminiPdfTutorViewModel = viewModel()
) {
  val context = LocalContext.current
  val isLoading by viewModel.isLoading.collectAsState()
  val aiResponse by viewModel.aiResponse.collectAsState()
  val fileName by viewModel.fileName.collectAsState()

  // PDF file picker launcher using Storage Access Framework
  val pdfPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      // Extract file name or default
      val name = uri.lastPathSegment ?: "document.pdf"
      viewModel.analyzePdf(context, uri, name)
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      NeoHeader(
        title = "JUT PDF Lab Tutor",
        subtitle = "Upload Lab Manuals & Get Hinglish AI Explanations"
      )
    }

    // Upload Action Card
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(
          text = "UPLOAD LEARNING MATERIAL",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = TechBlue
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Select any ECE lecture notes, lab manual, or datasheet (PDF) from your device to get an instant breakdown in Hinglish.",
          fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        NeoButton(
          text = if (fileName != null) "Selected: $fileName" else "UPLOAD PDF / MATERIAL",
          onClick = { pdfPickerLauncher.launch("application/pdf") },
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Ink,
          textColor = OffWhite
        )
      }
    }

    // Loading State Card
    if (isLoading) {
      item {
        NeoCard(backgroundColor = Color(0xFFFEF3C7)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Ink, strokeWidth = 3.dp)
            Column {
              Text(
                text = "Analyzing document...",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
              )
              Text(
                text = "PDF padh raha hoon... Thoda sabra kijiye!",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.8f)
              )
            }
          }
        }
      }
    }

    // AI Hinglish Response Card
    if (aiResponse != null) {
      item {
        NeoCard(backgroundColor = Color.White) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "PROFESSOR'S HINGLISH EXPLANATION",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = SafeGreen
            )
            Box(
              modifier = Modifier
                .background(SafeGreen)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(text = "AI VERIFIED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = aiResponse ?: "",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    item {
      NeoButton(text = "Back", onClick = onBack, modifier = Modifier.fillMaxWidth(), buttonType = NeoButtonType.BACK)
    }
  }
}
