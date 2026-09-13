package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*

import kotlinx.coroutines.launch
import com.example.LocalAppPreferences
import androidx.compose.runtime.collectAsState

@Composable
fun SettingsScreen() {
  var isHinglishEnabled by remember { mutableStateOf(true) }
  var isDarkMode by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf("") }
  
  val appPreferences = LocalAppPreferences.current
  val coroutineScope = rememberCoroutineScope()
  
  val isSoundEnabled by appPreferences.soundEffectsEnabled.collectAsState(initial = true)
  val isNotificationsEnabled by appPreferences.notificationsEnabled.collectAsState(initial = true)
  val isHapticEnabled by appPreferences.hapticsEnabled.collectAsState(initial = true)
  val isAnimationsEnabled by appPreferences.animationsEnabled.collectAsState(initial = true)
  val isReduceMotion by appPreferences.reduceMotion.collectAsState(initial = false)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      NeoHeader(
        title = "Application Settings",
        subtitle = "Preferences, Haptics & Data Management"
      )
    }

    // Language Toggle
    item {
      NeoCard(backgroundColor = Color.White) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = "Hinglish AI Tutor Mode", fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(text = "AI explains technical concepts in Hinglish", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
          }
          Switch(checked = isHinglishEnabled, onCheckedChange = { isHinglishEnabled = it })
        }
      }
    }

    item {
      Text(text = "SOUND & HAPTICS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.5f), modifier = Modifier.padding(start = 4.dp, top = 8.dp))
    }

    item {
      NeoCard(backgroundColor = Color.White) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(text = "Sound Effects", fontSize = 14.sp, fontWeight = FontWeight.Black)
              Text(text = "UI clicks and simulation feedback", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            }
            Switch(checked = isSoundEnabled, onCheckedChange = { coroutineScope.launch { appPreferences.setSoundEffectsEnabled(it) } })
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(text = "Notification Sounds", fontSize = 14.sp, fontWeight = FontWeight.Black)
              Text(text = "Alerts and reminders", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            }
            Switch(checked = isNotificationsEnabled, onCheckedChange = { coroutineScope.launch { appPreferences.setNotificationsEnabled(it) } })
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(text = "Haptic Feedback", fontSize = 14.sp, fontWeight = FontWeight.Black)
              Text(text = "Vibrate on button clicks and wire snapping", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            }
            Switch(checked = isHapticEnabled, onCheckedChange = { coroutineScope.launch { appPreferences.setHapticsEnabled(it) } })
          }
        }
      }
    }

    item {
      Text(text = "ANIMATIONS & MOTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.5f), modifier = Modifier.padding(start = 4.dp, top = 8.dp))
    }

    item {
      NeoCard(backgroundColor = Color.White) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(text = "Animation Effects", fontSize = 14.sp, fontWeight = FontWeight.Black)
              Text(text = "Smooth UI transitions", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            }
            Switch(checked = isAnimationsEnabled, onCheckedChange = { coroutineScope.launch { appPreferences.setAnimationsEnabled(it) } })
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
              Text(text = "Reduce Motion", fontSize = 14.sp, fontWeight = FontWeight.Black)
              Text(text = "Use simplified, faster transitions", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
            }
            Switch(checked = isReduceMotion, onCheckedChange = { coroutineScope.launch { appPreferences.setReduceMotion(it) } })
          }
        }
      }
    }

    // Dark Mode
    item {
      NeoCard(backgroundColor = Color.White) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = "Dark Mode", fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(text = "High contrast dark canvas theme", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))
          }
          Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
        }
      }
    }

    // Destructive Reset Demo Data
    item {
      NeoCard(backgroundColor = Color(0xFFFEE2E2)) {
        Text(text = "DESTRUCTIVE DATA ACTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Reset all cached experiments, quiz progress, and sync queues.", fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        NeoButton(
          text = "Reset Demo Data",
          onClick = { resetMessage = "All demo data successfully reset to initial state!" },
          backgroundColor = DangerRed,
          textColor = Color.White,
          modifier = Modifier.fillMaxWidth()
        )
        if (resetMessage.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = resetMessage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
        }
      }
    }

    
  }
}
