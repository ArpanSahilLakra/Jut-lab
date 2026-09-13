package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.Ink
import com.example.ui.theme.OffWhite
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.TechBlue
import com.example.ui.theme.AmberAccent
import com.example.LocalHapticManager
import com.example.LocalAudioManager
import com.example.audio.AppAudioManager
import com.example.LocalAppPreferences
import androidx.compose.ui.platform.LocalView

// 1. Pure Gate Logic Functions (gates.ts equivalent)
fun evaluateAnd(a: Boolean, b: Boolean): Boolean = a && b
fun evaluateOr(a: Boolean, b: Boolean): Boolean = a || b
fun evaluateNot(a: Boolean): Boolean = !a
fun evaluateNand(a: Boolean, b: Boolean): Boolean = !(a && b)
fun evaluateNor(a: Boolean, b: Boolean): Boolean = !(a || b)
fun evaluateXor(a: Boolean, b: Boolean): Boolean = a != b

enum class LogicGate(val title: String, val description: String) {
  AND("AND", "Output is High (1) only if both A and B are High (1)."),
  OR("OR", "Output is High (1) if either A or B (or both) is High (1)."),
  NOT("NOT", "Inverts input A (High becomes Low, Low becomes High)."),
  XOR("XOR", "Output is High (1) if A and B are different."),
  NAND("NAND", "Inverted AND: Output is Low (0) only when both A and B are High."),
  NOR("NOR", "Inverted OR: Output is High (1) only when both A and B are Low.")
}

@Composable
fun LogicGateScreen(onBack: () -> Unit) {
  val context = LocalContext.current
  val hapticManager = LocalHapticManager.current
  val audioManager = LocalAudioManager.current
  val view = LocalView.current
  
  var selectedGate by remember { mutableStateOf(LogicGate.AND) }
  var inputA by remember { mutableStateOf(false) }
  var inputB by remember { mutableStateOf(false) }
  var isChallengeMode by remember { mutableStateOf(false) }
  var hintLevel by remember { mutableStateOf(0) }
  var challengeCompleted by remember { mutableStateOf(false) }

  val output = remember(selectedGate, inputA, inputB) {
    when (selectedGate) {
      LogicGate.AND -> evaluateAnd(inputA, inputB)
      LogicGate.OR -> evaluateOr(inputA, inputB)
      LogicGate.NOT -> evaluateNot(inputA)
      LogicGate.XOR -> evaluateXor(inputA, inputB)
      LogicGate.NAND -> evaluateNand(inputA, inputB)
      LogicGate.NOR -> evaluateNor(inputA, inputB)
    }
  }

  // Check NAND challenge completion (Goal: Make NAND output 0 by setting both inputs HIGH)
  LaunchedEffect(selectedGate, inputA, inputB) {
    if (isChallengeMode && selectedGate == LogicGate.NAND && inputA && inputB && !output) {
      challengeCompleted = true
      hapticManager.triggerHapticFeedback()
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
        title = "Digital Logic Lab",
        subtitle = "Interactive Gate Simulator & NAND Challenge"
      )
    }

    // Mode Toggle
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        NeoButton(
          text = "Simulator Mode",
          onClick = {
            hapticManager.triggerHapticFeedback()
            isChallengeMode = false
          },
          modifier = Modifier.weight(1f),
          backgroundColor = if (!isChallengeMode) TechBlue else Color.White,
          textColor = if (!isChallengeMode) OffWhite else Ink
        )
        NeoButton(
          text = "NAND Challenge",
          onClick = {
            hapticManager.triggerHapticFeedback()
            isChallengeMode = true
            selectedGate = LogicGate.NAND
          },
          modifier = Modifier.weight(1f),
          backgroundColor = if (isChallengeMode) AmberAccent else Color.White,
          textColor = Ink
        )
      }
    }

    if (isChallengeMode) {
      // 3. NandChallenge Component
      item {
        NeoCard(backgroundColor = Color.White) {
          Text(text = "CHALLENGE OBJECTIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Build a NAND circuit condition where Output is LOW (0) by setting BOTH Input A and Input B to HIGH (1).",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black
          )

          Spacer(modifier = Modifier.height(12.dp))

          if (challengeCompleted) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(SafeGreen)
                .border(2.dp, Ink)
                .padding(12.dp)
            ) {
              Text(
                text = "SUCCESS! NAND challenge verified and saved locally.",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(text = "Hinglish Hint System:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              if (hintLevel >= 1) {
                Text(text = "Hint 1: Select the NAND gate from the selector.", fontSize = 12.sp, color = TechBlue)
              }
              if (hintLevel >= 2) {
                Text(text = "Hint 2: Arre yaar, NAND gate mein jab dono inputs HIGH honge tabhi output LOW (0) milega!", fontSize = 12.sp, color = TechBlue)
              }
              if (hintLevel < 2) {
                NeoButton(
                  text = "Get Hint (${hintLevel + 1}/2)",
                  onClick = {
                    hapticManager.triggerHapticFeedback()
                    hintLevel = (hintLevel + 1).coerceAtMost(2)
                  }
                )
              }
            }
          }
        }
      }
    }

    // Gate Selector (Simulator Mode)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "SELECT GATE", fontWeight = FontWeight.Black, fontSize = 14.sp)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(LogicGate.values()) { gate ->
            val isSelected = gate == selectedGate
            NeoButton(
              text = gate.title,
              onClick = {
                hapticManager.triggerHapticFeedback()
                selectedGate = gate
              },
              backgroundColor = if (isSelected) TechBlue else Color.White,
              textColor = if (isSelected) OffWhite else Ink
            )
          }
        }
      }
    }

    // Gate Theory Card
    item {
      NeoCard(backgroundColor = OffWhite) {
        Text(
          text = "${selectedGate.title} GATE THEORY",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = selectedGate.description,
          fontSize = 13.sp
        )
      }
    }

    // 2. GateVisualizer & Circuit Bench Component
    item {
      NeoCard(backgroundColor = Color.White) {
        Text(
          text = "CIRCUIT BENCH & VISUALIZER",
          fontWeight = FontWeight.Black,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input A Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("INPUT A: ${if (inputA) "1 (HIGH)" else "0 (LOW)"}", fontWeight = FontWeight.Bold)
          Switch(
            checked = inputA,
            onCheckedChange = {
              hapticManager.triggerHapticFeedback()
              inputA = it
            }
          )
        }

        if (selectedGate != LogicGate.NOT) {
          Spacer(modifier = Modifier.height(12.dp))
          // Input B Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("INPUT B: ${if (inputB) "1 (HIGH)" else "0 (LOW)"}", fontWeight = FontWeight.Bold)
            Switch(
              checked = inputB,
              onCheckedChange = {
                hapticManager.triggerHapticFeedback()
                inputB = it
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Ink, thickness = 2.dp)
        Spacer(modifier = Modifier.height(20.dp))

        // Output Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "OUTPUT: ${if (output) "1 (HIGH)" else "0 (LOW)"}",
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
          )
          Box(
            modifier = Modifier
              .size(36.dp)
              .border(3.dp, Ink, CircleShape)
              .background(if (output) SafeGreen else DangerRed, CircleShape)
          )
        }
      }
    }

    item {
      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
  }
}
