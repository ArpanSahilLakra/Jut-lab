package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoCard
import com.example.ui.theme.*
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun EngineeringConverterScreen(
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: dBm <-> Watts, 1: Wavelength <-> Freq, 2: Reactance

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
          text = "RF & ECE UNIT CONVERTER",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Converter", tint = TechBlue)
    }

    // Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      ConverterTab("dBm / Watts", selectedTab == 0) { selectedTab = 0 }
      ConverterTab("Freq / Wave", selectedTab == 1) { selectedTab = 1 }
      ConverterTab("Reactance", selectedTab == 2) { selectedTab = 2 }
    }

    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (selectedTab) {
        0 -> DbmWattsConverterSection()
        1 -> FreqWavelengthConverterSection()
        2 -> ReactanceCalculatorSection()
      }
    }
  }
}

@Composable
fun RowScope.ConverterTab(title: String, selected: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .weight(1f)
      .border(2.dp, Ink, RoundedCornerShape(0.dp))
      .background(if (selected) TechBlue else Color.White)
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (selected) Color.White else Ink
    )
  }
}

@Composable
fun DbmWattsConverterSection() {
  var dbmInput by remember { mutableStateOf("30") } // 30 dBm = 1 Watt
  val dbm = dbmInput.toDoubleOrNull() ?: 0.0
  val watts = 10.0.pow((dbm - 30.0) / 10.0)
  val mw = watts * 1000.0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "dBm to Watts Conversion", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = String.format("Power: %.6f W (%.3f mW)", watts, mw), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TechBlue)
      }
    }
    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = dbmInput,
          onValueChange = { dbmInput = it },
          label = { Text("Power (dBm)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun FreqWavelengthConverterSection() {
  var freqInput by remember { mutableStateOf("2400") } // 2400 MHz (2.4 GHz)
  val freqMhz = freqInput.toDoubleOrNull() ?: 2400.0
  val freqHz = freqMhz * 1e6
  val c = 3.0 * 10.0.pow(8)
  val wavelengthMeters = if (freqHz > 0) c / freqHz else 0.0
  val wavelengthCm = wavelengthMeters * 100.0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "Frequency & Wavelength", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = String.format("Wavelength: %.2f cm (%.4f m)", wavelengthCm, wavelengthMeters), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TechBlue)
      }
    }
    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = freqInput,
          onValueChange = { freqInput = it },
          label = { Text("Frequency (MHz)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun ReactanceCalculatorSection() {
  var freqInput by remember { mutableStateOf("1000") } // 1 kHz
  var capInput by remember { mutableStateOf("100") } // 100 nF

  val f = freqInput.toDoubleOrNull() ?: 1000.0
  val c = (capInput.toDoubleOrNull() ?: 100.0) * 1e-9
  val xc = if (f > 0 && c > 0) 1.0 / (2 * Math.PI * f * c) else 0.0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "Capacitive Reactance (Xc)", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = String.format("Xc = %.2f Ω", xc), fontSize = 18.sp, fontWeight = FontWeight.Black, color = TechBlue)
      }
    }
    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = freqInput,
          onValueChange = { freqInput = it },
          label = { Text("Frequency (Hz)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = capInput,
          onValueChange = { capInput = it },
          label = { Text("Capacitance (nF)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
