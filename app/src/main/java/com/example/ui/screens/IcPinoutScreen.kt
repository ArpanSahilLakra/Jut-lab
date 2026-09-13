package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Memory
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

data class IcPinout(
  val name: String,
  val packageType: String,
  val description: String,
  val pins: List<String>
)

@Composable
fun IcPinoutScreen(
  onBack: () -> Unit
) {
  val icList = remember {
    listOf(
      IcPinout(
        name = "NE555 Timer",
        packageType = "8-Pin DIP",
        description = "Precision timing circuit capable of producing accurate time delays or oscillation.",
        pins = listOf(
          "1: GND (Ground)",
          "2: Trigger (Inverts input to flip-flop)",
          "3: Output (Pin output waveform)",
          "4: Reset (Active low reset)",
          "5: Control Voltage (Threshold control)",
          "6: Threshold (Comparator 1 input)",
          "7: Discharge (Open collector output to discharge timing capacitor)",
          "8: VCC (Supply voltage 4.5V - 15V)"
        )
      ),
      IcPinout(
        name = "LM741 Operational Amplifier",
        packageType = "8-Pin DIP",
        description = "General-purpose operational amplifier with high voltage gain and internal compensation.",
        pins = listOf(
          "1: Offset Null",
          "2: Inverting Input (-)",
          "3: Non-Inverting Input (+)",
          "4: V- (Negative Supply / GND)",
          "5: Offset Null",
          "6: Output",
          "7: V+ (Positive Supply)",
          "8: NC (No Connection)"
        )
      ),
      IcPinout(
        name = "74LS00 Quad 2-Input NAND",
        packageType = "14-Pin DIP",
        description = "Standard TTL logic IC containing four independent 2-input NAND gates.",
        pins = listOf(
          "1: 1A Input", "2: 1B Input", "3: 1Y Output",
          "4: 2A Input", "5: 2B Input", "6: 2Y Output",
          "7: GND", "8: 3Y Output", "9: 3B Input",
          "10: 3A Input", "11: 4Y Output", "12: 4B Input",
          "13: 4A Input", "14: VCC (+5V)"
        )
      ),
      IcPinout(
        name = "LM7805 5V Voltage Regulator",
        packageType = "3-Pin TO-220",
        description = "Fixed 5V positive voltage regulator for microcontroller and digital logic circuits.",
        pins = listOf(
          "1: Input (Voltage In, 7V - 35V)",
          "2: Ground (Common GND)",
          "3: Output (Regulated +5V Out)"
        )
      )
    )
  }

  var selectedIc by remember { mutableStateOf<IcPinout?>(icList.first()) }

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
          text = "IC PINOUT & DATASHEET REFERENCE",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      Icon(imageVector = Icons.Default.Memory, contentDescription = "IC", tint = TechBlue)
    }

    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // List of ICs
      LazyColumn(
        modifier = Modifier
          .weight(0.45f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(icList) { ic ->
          val isSelected = selectedIc?.name == ic.name
          NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = if (isSelected) TechBlue else Color.White,
            onClick = { selectedIc = ic }
          ) {
            Text(
              text = ic.name,
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = if (isSelected) Color.White else Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = ic.packageType,
              fontSize = 11.sp,
              color = if (isSelected) Color.White.copy(alpha = 0.8f) else Ink.copy(alpha = 0.6f)
            )
          }
        }
      }

      // Detailed Pinout View
      selectedIc?.let { ic ->
        NeoCard(
          modifier = Modifier
            .weight(0.55f)
            .fillMaxHeight(),
          backgroundColor = Color.White
        ) {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            item {
              Text(text = ic.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TechBlue)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "Package: ${ic.packageType}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = ic.description, fontSize = 12.sp, color = Ink.copy(alpha = 0.8f))
              Spacer(modifier = Modifier.height(16.dp))
              Text(text = "PIN CONFIGURATION", fontSize = 13.sp, fontWeight = FontWeight.Black)
              Spacer(modifier = Modifier.height(8.dp))
            }

            items(ic.pins) { pin ->
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .border(1.dp, Ink, RoundedCornerShape(4.dp)),
                color = OffWhite
              ) {
                Text(
                  text = pin,
                  modifier = Modifier.padding(10.dp),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Ink
                )
              }
            }
          }
        }
      }
    }
  }
}
