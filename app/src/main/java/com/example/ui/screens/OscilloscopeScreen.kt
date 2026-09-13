package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoCard
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.sin

@Composable
fun OscilloscopeScreen(
  onBack: () -> Unit
) {
  var frequency by remember { mutableStateOf(2f) } // Hz
  var amplitude by remember { mutableStateOf(50f) } // pixels
  var waveType by remember { mutableStateOf(0) } // 0: Sine, 1: Square, 2: Triangle
  var time by remember { mutableStateOf(0f) }
  var isRunning by remember { mutableStateOf(true) }
  LaunchedEffect(isRunning) {
      while(isRunning) {
          kotlinx.coroutines.delay(16)
          time += 0.05f
      }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
  ) {
    // Header
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
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
        }
        Text(
          text = "OSCILLOSCOPE & SIGNAL SIMULATOR",
          fontSize = 14.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Oscilloscope", tint = TechBlue)
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        NeoCard(backgroundColor = Color.Black) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.dp, SafeGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .background(Color.Black)
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerY = height / 2f

                // Draw grid lines
                val stepX = width / 10f
                val stepY = height / 8f
                for (i in 1..9) {
                  drawLine(
                    color = SafeGreen.copy(alpha = 0.2f),
                    start = Offset(i * stepX, 0f),
                    end = Offset(i * stepX, height),
                    strokeWidth = 1f
                  )
                }
                for (i in 1..7) {
                  drawLine(
                    color = SafeGreen.copy(alpha = 0.2f),
                    start = Offset(0f, i * stepY),
                    end = Offset(width, i * stepY),
                    strokeWidth = 1f
                  )
                }

                // Center crosshairs
                drawLine(
                  color = SafeGreen.copy(alpha = 0.5f),
                  start = Offset(0f, centerY),
                  end = Offset(width, centerY),
                  strokeWidth = 1.5f
                )
                drawLine(
                  color = SafeGreen.copy(alpha = 0.5f),
                  start = Offset(width / 2f, 0f),
                  end = Offset(width / 2f, height),
                  strokeWidth = 1.5f
                )

                // Draw waveform
                val path = Path()
                val points = 200
                val dx = width / points

                for (i in 0..points) {
                  val x = i * dx
                  val t = (i / points.toFloat()) * (2f * Math.PI.toFloat()) * frequency + time
                  val y = when (waveType) {
                    0 -> centerY - (sin(t) * amplitude).toFloat()
                    1 -> centerY - (if (sin(t) >= 0) amplitude else -amplitude).toFloat()
                    else -> {
                      val normalized = (t / (2f * Math.PI.toFloat())) % 1f
                      val triangle = if (normalized < 0.5f) (normalized * 4f - 1f) else ((1f - normalized) * 4f - 1f)
                      centerY - (triangle * amplitude).toFloat()
                    }
                  }

                  if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }

                drawPath(
                  path = path,
                  color = SafeGreen,
                  style = Stroke(width = 3f)
                )
              }
            }
          }
        }
      }

      item {
        NeoCard(backgroundColor = Color.White) {
          Text(text = "Signal Controls", fontSize = 15.sp, fontWeight = FontWeight.Black)
          Spacer(modifier = Modifier.height(12.dp))

          Text(text = "Waveform Type", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { waveType = 0 },
              colors = ButtonDefaults.buttonColors(containerColor = if (waveType == 0) TechBlue else Color.LightGray),
              modifier = Modifier.weight(1f)
            ) {
              Text("Sine", fontSize = 12.sp)
            }
            Button(
              onClick = { waveType = 1 },
              colors = ButtonDefaults.buttonColors(containerColor = if (waveType == 1) TechBlue else Color.LightGray),
              modifier = Modifier.weight(1f)
            ) {
              Text("Square", fontSize = 12.sp)
            }
            Button(
              onClick = { waveType = 2 },
              colors = ButtonDefaults.buttonColors(containerColor = if (waveType == 2) TechBlue else Color.LightGray),
              modifier = Modifier.weight(1f)
            ) {
              Text("Triangle", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
          Text(text = "Frequency: ${String.format(Locale.US, "%.1f", (frequency * 100).toDouble())} Hz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Slider(
            value = frequency,
            onValueChange = { frequency = it },
            valueRange = 0.5f..5f
          )

          Spacer(modifier = Modifier.height(12.dp))
          Text(text = "Amplitude: ${amplitude.toInt()} mV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Slider(
            value = amplitude,
            onValueChange = { amplitude = it },
            valueRange = 20f..90f
          )
        }
      }
    }
  }
}
