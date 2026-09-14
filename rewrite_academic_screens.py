import os

content = """package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterListScreen(
    viewModel: AcademicViewModel,
    onNavigateToSubjects: (String) -> Unit,
    onBack: () -> Unit
) {
    // We skip this for Phase 2, usually jumping straight to SubjectListScreen
    // but keep it just in case it's requested.
    val semesters by viewModel.semesters.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadSemesters()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JUT ECE Semesters", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(semesters) { semester ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToSubjects(semester.id) }) {
                    Text(semester.title, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectListScreen(
    semesterId: String,
    viewModel: AcademicViewModel,
    onNavigateToSubjectDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    LaunchedEffect(semesterId) {
        viewModel.loadSubjects(semesterId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SEMESTER 1",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink
                    )
                    Text(
                        text = "Foundation Year",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TechBlue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            
            if (subjects.isEmpty()) {
                item {
                    Text("Loading subjects...", color = Ink.copy(alpha = 0.6f))
                }
            } else {
                items(subjects) { subject ->
                    val code = subject.title.substringBefore(" - ")
                    val name = subject.title.substringAfter(" - ")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSubjectDetail(subject.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Ink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = code,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TechBlue
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Ink,
                                        lineHeight = 22.sp
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open",
                                    tint = Ink,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Stats Row Placeholder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(label = "Questions", value = "10+")
                                StatItem(label = "Viva", value = "0%")
                                StatItem(label = "Bookmarks", value = "0")
                                StatItem(label = "Mastery", value = "0%")
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Ink.copy(alpha = 0.6f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    viewModel: AcademicViewModel,
    onNavigateToTopic: (String) -> Unit,
    onBack: () -> Unit
) {
    val subject by viewModel.currentSubject.collectAsState()
    
    LaunchedEffect(subjectId) {
        viewModel.loadSubjectDetails(subjectId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.title?.substringBefore(" - ") ?: "Subject", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        if (subject == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = subject!!.title.substringAfter(" - "),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Ink,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = subject!!.description, fontSize = 14.sp, color = Ink.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                val categories = listOf(
                    "Overview" to Icons.Default.Info,
                    "Units" to Icons.Default.List,
                    "VVI" to Icons.Default.LocalFireDepartment,
                    "Practice" to Icons.Default.Assignment,
                    "Numericals" to Icons.Default.Calculate,
                    "Viva" to Icons.Default.RecordVoiceOver,
                    "Previous Papers" to Icons.Default.HistoryEdu,
                    "Bookmarks" to Icons.Default.BookmarkBorder,
                    "Weak Topics" to Icons.Default.WarningAmber
                )
                
                items(categories) { (title, icon) ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { 
                            if (title == "Units") {
                                // For now, pass subjectId as unitId to TopicListScreen to bypass an extra screen
                                onNavigateToTopic(subject!!.id)
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Ink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = TechBlue)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Ink.copy(alpha = 0.5f))
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicListScreen(
    unitId: String, // Actually using subjectId here for simplification in Phase 2
    viewModel: AcademicViewModel,
    onNavigateToTopicDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    // Using subjectId to load all units and topics
    val units by viewModel.units.collectAsState()
    val topics by viewModel.topics.collectAsState()
    
    LaunchedEffect(unitId) {
        viewModel.loadUnits(unitId)
        // Wait, normally we load topics per unit. To show all, let's just load for the first unit or create a flow.
        // For Phase 2, if we just want to see the questions, let's navigate straight to "Questions" or display the units.
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Units & Topics", fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (units.isEmpty()) {
                item { Text("No Units Available", color = Ink.copy(alpha = 0.6f)) }
            } else {
                items(units) { unit ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToTopicDetail(unit.id) },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Ink)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Unit ${unit.unitNumber}: ${unit.title}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = TechBlue)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(unit.description, fontSize = 14.sp, color = Ink.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: String,
    viewModel: AcademicViewModel,
    onNavigateToAI: (String) -> Unit,
    onBookmark: (TopicEntity) -> Unit,
    onBack: () -> Unit
) {
    // In our simplified flow, topicId here is actually the unitId, and we should show Questions for that unit.
    // Let's implement a quick view to show the questions.
    // This requires a new method in AcademicViewModel to fetch questions by unitId, 
    // but for now, we'll just show a "Questions" UI placeholder that could wire up to it.
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Questions", fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        containerColor = OffWhite
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Questions for this unit will appear here.", color = Ink)
            // We can populate this deeply later in Phase 2
        }
    }
}
"""

with open('app/src/main/java/com/example/ui/screens/AcademicScreens.kt', 'w') as f:
    f.write(content)

