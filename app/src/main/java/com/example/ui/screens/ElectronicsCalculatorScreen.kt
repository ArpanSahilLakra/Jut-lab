package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.theme.*

@Composable
fun ElectronicsCalculatorScreen(
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Resistor, 1: Ohm's Law, 2: RC Filter

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
          text = "ECE CALCULATOR & TOOLKIT",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculator", tint = TechBlue)
    }

    // Tabs
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
        .padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      TabButton(title = "Resistor", selected = selectedTab == 0) { selectedTab = 0 }
      TabButton(title = "Ohm's Law", selected = selectedTab == 1) { selectedTab = 1 }
      TabButton(title = "RC Filter", selected = selectedTab == 2) { selectedTab = 2 }
      TabButton(title = "V-Divider", selected = selectedTab == 3) { selectedTab = 3 }
    }

    // Content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      when (selectedTab) {
        0 -> ResistorColorCodeSection()
        1 -> OhmsLawCalculatorSection()
        2 -> RcFilterCalculatorSection()
        3 -> VoltageDividerSection()
      }
    }
  }
}

@Composable
fun RowScope.TabButton(title: String, selected: Boolean, onClick: () -> Unit) {
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
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = if (selected) Color.White else Ink
    )
  }
}

@Composable
fun ResistorColorCodeSection() {
  val colors = listOf(
    "Black" to Color.Black,
    "Brown" to Color(0xFF8B4513),
    "Red" to Color.Red,
    "Orange" to Color(0xFFFFA500),
    "Yellow" to Color.Yellow,
    "Green" to Color.Green,
    "Blue" to Color.Blue,
    "Violet" to Color(0xFF8A2BE2),
    "Grey" to Color.Gray,
    "White" to Color.White
  )

  var band1 by remember { mutableStateOf(2) } // Red = 2
  var band2 by remember { mutableStateOf(0) } // Black = 0
  var multiplier by remember { mutableStateOf(1) } // Brown = 1 (10^1)

  val digit1 = band1
  val digit2 = band2
  val multVal = Math.pow(10.0, multiplier.toDouble()).toLong()
  val resistance = (digit1 * 10 + digit2) * multVal

  val resistanceString = if (resistance >= 1_000_000) {
    "${resistance / 1_000_000.0} MΩ"
  } else if (resistance >= 1_000) {
    "${resistance / 1_000.0} kΩ"
  } else {
    "$resistance Ω"
  }

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "4-BAND RESISTOR CALCULATOR", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Calculated Value:", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
        Text(text = resistanceString, fontSize = 28.sp, fontWeight = FontWeight.Black, color = TechBlue)
      }
    }

    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "Band 1: ${colors[band1].first}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          colors.forEachIndexed { index, pair ->
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(pair.second)
                .border(2.dp, if (band1 == index) Ink else Color.Transparent)
                .clickable { band1 = index }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Band 2: ${colors[band2].first}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          colors.forEachIndexed { index, pair ->
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(pair.second)
                .border(2.dp, if (band2 == index) Ink else Color.Transparent)
                .clickable { band2 = index }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Multiplier (10^x): 10^$multiplier", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          (0..6).forEach { m ->
            Box(
              modifier = Modifier
                .size(36.dp)
                .border(2.dp, if (multiplier == m) Ink else Color.LightGray)
                .background(if (multiplier == m) AmberAccent else Color.White)
                .clickable { multiplier = m },
              contentAlignment = Alignment.Center
            ) {
              Text(text = "$m", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun OhmsLawCalculatorSection() {
  var voltageInput by remember { mutableStateOf("12") }
  var currentInput by remember { mutableStateOf("0.5") }
  var resistanceInput by remember { mutableStateOf("") }

  val v = voltageInput.toDoubleOrNull()
  val i = currentInput.toDoubleOrNull()
  val r = resistanceInput.toDoubleOrNull()

  // Compute missing
  val computedResult = remember(v, i, r) {
    when {
      v != null && i != null && i > 0 -> "Resistance R = ${v / i} Ω (P = ${v * i} W)"
      v != null && r != null && r > 0 -> "Current I = ${v / r} A (P = ${v * v / r} W)"
      i != null && r != null -> "Voltage V = ${i * r} V (P = ${i * i * r} W)"
      else -> "Enter any two values to compute the third."
    }
  }

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "OHM'S LAW & POWER CALCULATOR", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = computedResult, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
      }
    }

    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = voltageInput,
          onValueChange = { voltageInput = it },
          label = { Text("Voltage V (Volts)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = currentInput,
          onValueChange = { currentInput = it },
          label = { Text("Current I (Amps)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = resistanceInput,
          onValueChange = { resistanceInput = it },
          label = { Text("Resistance R (Ohms)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun RcFilterCalculatorSection() {
  var resInput by remember { mutableStateOf("1000") } // 1k ohm
  var capInput by remember { mutableStateOf("100") } // 100 nF

  val r = resInput.toDoubleOrNull() ?: 1000.0
  val cNano = capInput.toDoubleOrNull() ?: 100.0
  val cFarads = cNano * 1e-9

  val fc = if (r > 0 && cFarads > 0) 1.0 / (2 * Math.PI * r * cFarads) else 0.0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "RC LOW-PASS FILTER CUTOFF", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = String.format("Cutoff Frequency (fc): %.2f Hz", fc),
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = TechBlue
        )
      }
    }

    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = resInput,
          onValueChange = { resInput = it },
          label = { Text("Resistance R (Ω)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = capInput,
          onValueChange = { capInput = it },
          label = { Text("Capacitance C (nF)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun VoltageDividerSection() {
  var vinInput by remember { mutableStateOf("5") }
  var r1Input by remember { mutableStateOf("1000") }
  var r2Input by remember { mutableStateOf("2000") }

  val vin = vinInput.toDoubleOrNull() ?: 5.0
  val r1 = r1Input.toDoubleOrNull() ?: 1000.0
  val r2 = r2Input.toDoubleOrNull() ?: 2000.0

  val vout = if (r1 + r2 > 0) vin * (r2 / (r1 + r2)) else 0.0

  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(text = "VOLTAGE DIVIDER", fontSize = 14.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = String.format("Output Voltage (Vout): %.2f V", vout),
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = TechBlue
        )
      }
    }
    item {
      NeoCard(backgroundColor = Color.White) {
        OutlinedTextField(
          value = vinInput,
          onValueChange = { vinInput = it },
          label = { Text("Input Voltage (Vin)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = r1Input,
          onValueChange = { r1Input = it },
          label = { Text("Resistor 1 (R1 in Ω)") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = r2Input,
          onValueChange = { r2Input = it },
          label = { Text("Resistor 2 (R2 in Ω)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}
