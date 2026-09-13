package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LabDao
import com.example.ui.components.NeoCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun BookmarksScreen(
  labDao: LabDao,
  onBack: () -> Unit
) {
  val bookmarks by labDao.getBookmarksForUser(com.example.repository.AuthRepository().getCurrentUserId() ?: "").collectAsState(initial = emptyList())
  val scope = rememberCoroutineScope()

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
        .border(BorderStroke(2.dp, Ink))
        .padding(horizontal = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Ink)
      }
      Spacer(modifier = Modifier.width(8.dp))
      Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Bookmarks", tint = TechBlue)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "SAVED BOOKMARKS",
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = Ink
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (bookmarks.isEmpty()) {
        item {
          Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
             Text("No bookmarks saved.", color = Ink.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
          }
        }
      } else {
        items(bookmarks) { bookmark ->
          NeoCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color.White
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .border(1.dp, Ink)
                    .background(TechBlue.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = bookmark.type.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TechBlue
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = bookmark.title,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = bookmark.description,
                  fontSize = 13.sp,
                  color = Ink.copy(alpha = 0.8f)
                )
              }
              IconButton(onClick = {
                  scope.launch {
                      labDao.deleteBookmark(bookmark)
                  }
              }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
              }
            }
          }
        }
      }
    }
  }
}
