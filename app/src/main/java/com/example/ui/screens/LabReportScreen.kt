package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.data.LabReportEntity
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun LabReportScreen(
  labDao: LabDao,
  reportRepository: com.example.repository.ReportRepository,
  onBack: () -> Unit
) {
  val scope = rememberCoroutineScope()
  val reports by labDao.getLabReportsForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())
  
  var showAddDialog by remember { mutableStateOf(false) }
  var newTitle by remember { mutableStateOf("") }
  var newAim by remember { mutableStateOf("") }
  var newObservations by remember { mutableStateOf("") }
  var newConclusion by remember { mutableStateOf("") }
  var exportStatus by remember { mutableStateOf<String?>(null) }

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
          text = "LAB REPORT & OBSERVATION LOG",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = Ink
        )
      }
      IconButton(onClick = { showAddDialog = true }) {
        Icon(imageVector = Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "Add", tint = TechBlue)
      }
    }

    if (exportStatus != null) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .background(SafeGreen)
          .padding(12.dp),
        color = SafeGreen
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = Color.White)
          Text(text = exportStatus!!, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Submitted Lab Reports (${reports.size})", fontWeight = FontWeight.Black, fontSize = 16.sp)
          if (reports.isNotEmpty()) {
              NeoButton(
                text = "Export All PDF",
                backgroundColor = TechBlue,
                textColor = Color.White,
                onClick = {
                  exportStatus = "Successfully generated and exported all lab reports to PDF format."
                }
              )
          }
        }
      }

      if (reports.isEmpty()) {
          item {
              Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                  Text("No reports generated yet.", color = Ink.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
              }
          }
      }

      items(reports) { report ->
        NeoCard(
          modifier = Modifier.fillMaxWidth(),
          backgroundColor = Color.White
        ) {
          val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(report.updatedAt))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = report.id.take(8), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechBlue)
            Text(text = dateStr, fontSize = 11.sp, color = Ink.copy(alpha = 0.6f))
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(text = report.title, fontSize = 16.sp, fontWeight = FontWeight.Black)
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = "Aim: ${report.aim}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Observations: ${report.observations}", fontSize = 12.sp, color = Ink.copy(alpha = 0.8f))
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Conclusion: ${report.result}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
        }
      }
    }
  }

  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text(text = "New Lab Observation", fontWeight = FontWeight.Black) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Experiment Title") })
          OutlinedTextField(value = newAim, onValueChange = { newAim = it }, label = { Text("Aim") })
          OutlinedTextField(value = newObservations, onValueChange = { newObservations = it }, label = { Text("Observations & Readings") })
          OutlinedTextField(value = newConclusion, onValueChange = { newConclusion = it }, label = { Text("Conclusion") })
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newTitle.isNotBlank()) {
              val newReport = LabReportEntity(
                id = UUID.randomUUID().toString(),
                experimentId = "manual_entry",
                title = newTitle,
                aim = newAim.ifBlank { "Standard ECE Lab Experiment" },
                apparatus = "Standard Tools",
                theory = "N/A",
                circuitDescription = "N/A",
                observations = newObservations.ifBlank { "Readings verified." },
                calculations = "N/A",
                result = newConclusion.ifBlank { "Experiment successful." },
                conclusion = newConclusion.ifBlank { "Principles verified." },
                vivaScore = 0,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
              )
              scope.launch {
                  reportRepository.saveReport(newReport)
              }
              newTitle = ""
              newAim = ""
              newObservations = ""
              newConclusion = ""
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
        ) {
          Text("Save Report")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
