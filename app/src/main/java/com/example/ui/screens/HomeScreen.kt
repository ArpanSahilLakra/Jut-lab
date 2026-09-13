package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.TechBlue
import com.example.ui.theme.DangerRed
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.OffWhite
import com.example.ui.theme.Ink
import com.example.LocalAppPreferences
import com.example.ui.animations.AppAnimations
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun HomeScreen(
  onNavigateToExperiment: (String) -> Unit, 
  labDao: com.example.data.LabDao, 
  onNavigate: (String) -> Unit
) {
  val appPreferences = LocalAppPreferences.current
  val experimentsProgress by labDao.getProgressForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())
  val recentExperiment = experimentsProgress.maxByOrNull { it.lastOpenedAt }
  val profile by labDao.getStudentProfileById(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = null)
  SyncTrigger(labDao = labDao)
  val reduceMotion by appPreferences.reduceMotion.collectAsState(initial = false)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
  ) {
    // Bento Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .background(Color.White)
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "ENGINEERING LAB",
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = (-0.5).sp,
          color = Ink.copy(alpha = 0.6f)
        )
        Text(
          text = "JUT ECE VIRTUAL",
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onNavigate("profile") }) {
          Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Ink)
        }
        IconButton(onClick = { onNavigate("settings") }) {
          Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Ink)
        }
        val context = androidx.compose.ui.platform.LocalContext.current
        Box(
          modifier = Modifier
            .clickable { android.widget.Toast.makeText(context, "Syncing...", android.widget.Toast.LENGTH_SHORT).show() }
            .border(2.dp, Ink, RoundedCornerShape(0.dp))
            .background(SafeGreen)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "SYNC",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    // Main Bento Scroll Content
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Active Session Bento Box
      item {
        AppAnimations.EnterFadeInSlideUp(reduceMotion) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "ACTIVE SESSION",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp,
              color = Ink.copy(alpha = 0.8f)
            )
            NeoCard(
              modifier = Modifier.fillMaxWidth(),
              backgroundColor = Color.White,
              shadowOffsetX = 6.dp,
              shadowOffsetY = 6.dp
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
              ) {
                Text(
                  text = "DIGITAL SIGNAL PROCESSING",
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Black,
                  modifier = Modifier.weight(2f),
                  lineHeight = 22.sp
                )
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                  Text(text = "LAB 04", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  Text(text = "In Progress", fontSize = 10.sp, color = Ink.copy(alpha = 0.6f))
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              NeoButton(
                text = "Resume Breadboard",
                onClick = { onNavigate("breadboard") },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Ink,
                textColor = OffWhite
              )
            }
          }
        }
      }

      // Dashboards quick access
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("student_dashboard") }
          ) {
            Text(text = "STUDENT\nDASHBOARD", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "XP & Streak", fontSize = 10.sp, color = SafeGreen, fontWeight = FontWeight.Bold)
          }

          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("teacher_workspace") }
          ) {
            Text(text = "TEACHER\nWORKSPACE", fontSize = 12.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Analytics", fontSize = 10.sp, color = TechBlue, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Bento Grid Section
      item {
        Text(
          text = "BENTO LAB MODULES",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp,
          color = Ink.copy(alpha = 0.8f)
        )
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Bento Tile 1: Logic Gates
          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("logic_gate") }
          ) {
            Text(
              text = "LOGIC\nGATES",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "COMPLETED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = SafeGreen
            )
          }

          // Bento Tile 2: Oscilloscope
          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("oscilloscope") }
          ) {
            Text(
              text = "SIGNAL\nSTUDIO",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "ACTIVE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TechBlue
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Bento Tile 3: Manuals
          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("manuals") }
          ) {
            Text(
              text = "LAB\nMANUALS",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "3 GUIDES",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Ink
            )
          }

          // Bento Tile 4: Viva Quiz
          NeoCard(
            modifier = Modifier.weight(1f),
            backgroundColor = Color.White,
            onClick = { onNavigate("quiz") }
          ) {
            Text(
              text = "VIVA\nQUIZ",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "ASSESS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = DangerRed
            )
          }
        }
      }

      // Bento Tile 5: AI Lab Tutor (Full width)
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("calculator") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ECE CALCULATOR & TOOLKIT",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Resistor Color Codes, Ohm's Law, & RC Filters",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.Default.Calculate, contentDescription = "Calculator", tint = TechBlue)
          }
        }
      }

      // Bento Tile: IC Pinout & Datasheet Reference
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("ic_pinout") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "IC PINOUT & DATASHEETS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "NE555, LM741, 74LS00, & Regulator Pinouts",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.Default.Memory, contentDescription = "IC", tint = TechBlue)
          }
        }
      }

      // Bento Tile: Lab Report & Observation Log
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("lab_report") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "LAB REPORT & OBSERVATION LOG",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Record observations, aims, and export PDF reports",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.Assignment, contentDescription = "Report", tint = TechBlue)
          }
        }
      }

      // Bento Tile: Bookmarks & Saved Experiments
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("bookmarks") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "SAVED BOOKMARKS",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Quick access to saved formulas and experiments",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Bookmarks", tint = TechBlue)
          }
        }
      }

      // Bento Tile: RF & ECE Unit Converter
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("engineering_converter") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "RF & ECE UNIT CONVERTER",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Convert dBm to Watts, Freq to Wavelength, & Reactance",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Converter", tint = TechBlue)
          }
        }
      }

      // Bento Tile: Oscilloscope & Signal Simulator
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("oscilloscope") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "OSCILLOSCOPE & SIGNAL SIMULATOR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Simulate Sine, Square, & Triangle wave signals in real-time",
                fontSize = 11.sp,
                color = Ink.copy(alpha = 0.7f)
              )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Oscilloscope", tint = TechBlue)
          }
        }
      }

      // Bento Tile 6: AI Lab Tutor (Full width)
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White,
          onClick = { onNavigate("ai_tutor") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "GEMINI AI LAB TUTOR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Instant circuit analysis & theoretical derivations",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      // Bento Tile 6: PDF Lab Tutor (Hinglish)
      item {
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color(0xFFFEF3C7),
          onClick = { onNavigate("learning_material") }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "PDF LAB TUTOR (HINGLISH)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Ink
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Upload PDF manuals & get friendly Hinglish explanations",
                fontSize = 12.sp,
                color = Ink.copy(alpha = 0.8f)
              )
            }
          }
        }
      }
    }  }
}
