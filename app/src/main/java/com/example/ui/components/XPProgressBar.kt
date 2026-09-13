package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Ink
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TechBlue

@Composable
fun XPProgressBar(
  currentXp: Int,
  maxXp: Int = 2000,
  modifier: Modifier = Modifier
) {
  val progress = (currentXp.toFloat() / maxXp.toFloat()).coerceIn(0f, 1f)

  val animatedProgress by animateFloatAsState(
    targetValue = progress,
    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
  )

  NeoCard(
    modifier = modifier,
    backgroundColor = Color.White
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(text = "LEVEL 3 SCHOLAR", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TechBlue)
      Text(text = "$currentXp / $maxXp XP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(16.dp)
        .border(2.dp, Ink, RoundedCornerShape(4.dp))
        .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(animatedProgress)
          .fillMaxHeight()
          .background(SafeGreen, RoundedCornerShape(2.dp))
      )
    }
  }
}
