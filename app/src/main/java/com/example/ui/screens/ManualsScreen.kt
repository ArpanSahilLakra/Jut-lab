package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExperimentEntity
import com.example.data.LabDao
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.OffWhite
import com.example.ui.theme.TechBlue
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.Ink
import kotlinx.coroutines.delay

@Composable
fun ManualsScreen(
  viewModel: ManualsViewModel,
  onSelectExperiment: (String) -> Unit,
  onTakeQuiz: (String) -> Unit,
  onBack: () -> Unit
) {
  val experiments by viewModel.experiments.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var rawSearchQuery by remember { mutableStateOf("") }
  var debouncedQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }

  // Debounce search input
  LaunchedEffect(rawSearchQuery) {
    delay(300)
    debouncedQuery = rawSearchQuery
  }

  val categories = listOf("All", "Analog Electronics", "Digital Electronics", "Circuit Theory")

  val filteredExperiments = remember(experiments, debouncedQuery, selectedCategory) {
    experiments.filter { exp ->
      val matchesCategory = selectedCategory == "All" || exp.category.equals(selectedCategory, ignoreCase = true)
      val matchesQuery = debouncedQuery.isBlank() ||
        exp.title.contains(debouncedQuery, ignoreCase = true) ||
        exp.description.contains(debouncedQuery, ignoreCase = true) ||
        exp.apparatusCommaSeparated.contains(debouncedQuery, ignoreCase = true)
      matchesCategory && matchesQuery
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(OffWhite)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    NeoHeader(
      title = "JUT ECE Lab Manuals",
      subtitle = "Search & Filter Practicals & Viva Voce Quizzes"
    )

    // Search Input Field
    OutlinedTextField(
      value = rawSearchQuery,
      onValueChange = { rawSearchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Search by topic, apparatus, or title...") },
      singleLine = true
    )

    // Category Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories) { cat ->
        val isSelected = selectedCategory == cat
        NeoButton(
          text = cat,
          onClick = { selectedCategory = cat },
          backgroundColor = if (isSelected) Ink else Color.White,
          textColor = if (isSelected) OffWhite else Ink
        )
      }
    }

    if (isLoading && experiments.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TechBlue)
      }
    } else if (filteredExperiments.isEmpty()) {
      Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(text = "No experiments found.", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "Try adjusting your search or category filter.", fontSize = 12.sp, color = Color.Gray)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredExperiments) { exp ->
        NeoCard(
          backgroundColor = Color.White,
          onClick = { onSelectExperiment(exp.id) }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = exp.id.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechBlue)
            Text(text = exp.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SafeGreen)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = exp.title, fontSize = 16.sp, fontWeight = FontWeight.Black)
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = exp.description, fontSize = 12.sp, color = Color.DarkGray)

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            NeoButton(
              text = "View Manual",
              onClick = { onSelectExperiment(exp.id) },
              modifier = Modifier.weight(1f)
            )
            NeoButton(
              text = "Viva Quiz",
              onClick = { onTakeQuiz(exp.id) },
              modifier = Modifier.weight(1f),
              backgroundColor = SafeGreen,
              textColor = Color.White
            )
          }
        }
      }
    }
    }

      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack, modifier = Modifier.fillMaxWidth())
  }
}
