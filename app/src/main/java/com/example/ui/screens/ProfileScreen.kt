package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.repository.AuthSessionManager
import androidx.compose.ui.platform.LocalContext
import com.example.data.LabDatabase
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DangerRed
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite

@Composable
fun ProfileScreen(
  userRole: String,
  onRoleChange: (String) -> Unit,
  onBack: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NeoHeader(
      title = "User Profile & Demo Mode",
      subtitle = "JUT ECE Virtual Lab Authentication & Role Manager"
    )

    NeoCard(backgroundColor = OffWhite) {
      Text(text = "CURRENT ROLE: ${userRole.uppercase()}", fontSize = 16.sp, fontWeight = FontWeight.Black)
      Spacer(modifier = Modifier.height(8.dp))
      Text(text = "Name: " + if (userRole == "student") "Arpan Lakra" else "Dr. B. Sarma", fontSize = 14.sp)
      Text(text = "Department: Electronics and Communication Engineering", fontSize = 14.sp)
      Text(text = "Institution: " + com.example.data.AppConfig.INSTITUTION_NAME, fontSize = 14.sp)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(text = "SWITCH DEMO ROLE", fontWeight = FontWeight.Bold, fontSize = 14.sp)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      NeoButton(
        text = "Student Mode",
        onClick = { onRoleChange("student") },
        modifier = Modifier.weight(1f),
        backgroundColor = if (userRole == "student") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        textColor = if (userRole == "student") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
      )
      NeoButton(
        text = "Teacher Mode",
        onClick = { onRoleChange("teacher") },
        modifier = Modifier.weight(1f),
        backgroundColor = if (userRole == "teacher") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        textColor = if (userRole == "teacher") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
      )
    }

    Spacer(modifier = Modifier.weight(1f))

      
      val context = LocalContext.current
      val scope = rememberCoroutineScope()
      val labDao = remember { LabDatabase.getDatabase(context).labDao() }
      
      NeoButton(
          text = "LOGOUT",
          onClick = {
              AuthSessionManager.explicitLogout(scope, labDao, onComplete = {})
          },
          backgroundColor = DangerRed,
          textColor = Color.White,
          modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(12.dp))
      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())

  }
}
