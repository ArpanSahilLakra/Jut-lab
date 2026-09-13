package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite

@Composable
fun TeacherScreen(onBack: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NeoHeader(
      title = "Teacher Faculty Portal",
      subtitle = com.example.data.AppConfig.INSTITUTION_NAME + " • " + com.example.data.AppConfig.DEPARTMENT_NAME + " Evaluation"
    )

    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        NeoCard(backgroundColor = OffWhite) {
          Text(text = "PENDING ASSIGNMENT SUBMISSIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "1. Arpan Lakra - RC Phase Shift Oscillator", fontSize = 15.sp, fontWeight = FontWeight.Black)
          Text(text = "Status: Submitted (Offline Queued)", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          val context = androidx.compose.ui.platform.LocalContext.current
          NeoButton(text = "Verify & Grade", onClick = {
            android.widget.Toast.makeText(context, "Verification Submitted!", android.widget.Toast.LENGTH_SHORT).show()
          })
        }
      }

      item {
        NeoCard(backgroundColor = OffWhite) {
          Text(text = "STUDENT VIVA SCORES", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Average Class Score: 3.5 / 4.0", fontSize = 15.sp, fontWeight = FontWeight.Black)
          Text(text = "Total Active Students: 48", fontSize = 12.sp)
        }
      }
    }

      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
  }
}
