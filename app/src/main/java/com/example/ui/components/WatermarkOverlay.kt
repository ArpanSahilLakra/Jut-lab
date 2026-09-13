package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Ink
import com.example.ui.theme.OffWhite

@Composable
fun WatermarkOverlay() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(bottom = 8.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    Box(
      modifier = Modifier
        .shadow(3.dp, shape = RoundedCornerShape(4.dp))
        .background(Ink, shape = RoundedCornerShape(4.dp))
        .border(2.dp, Ink, shape = RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
      Text(
        text = "This app Was made By ARPAN SAHIL LAKRA",
        color = OffWhite,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }
  }
}
