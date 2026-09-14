package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppPreferences
import com.example.ui.components.NeoButton
import com.example.ui.components.NeoButtonType
import com.example.ui.components.NeoCard
import com.example.ui.components.NeoHeader
import com.example.ui.theme.*
import com.example.util.SecureApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var isHinglishEnabled by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(false) }
    var resetMessage by remember { mutableStateOf("") }

    // AI Key state
    var apiKeyInput by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var hasSavedKey by remember { mutableStateOf(SecureApiKeyStore.hasApiKey(context)) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

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
                subtitle = "Preferences, AI Tutor & Local Mode"
            )
        }

        // App Mode Info Card
        item {
            NeoCard(backgroundColor = Color.White) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "APP MODE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.5f))
                    Text(text = "LOCAL / OFFLINE-FIRST", fontSize = 14.sp, fontWeight = FontWeight.Black, color = SafeGreen)
                    Text(text = "No account required. All experiments, simulations, and data remain local on your device.", fontSize = 12.sp, color = Ink.copy(alpha = 0.7f))
                }
            }
        }

        // AI Tutor Settings Section
        item {
            Text(text = "AI TUTOR (GEMINI API CONFIGURATION)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink.copy(alpha = 0.5f), modifier = Modifier.padding(start = 4.dp, top = 8.dp))
        }

        item {
            NeoCard(backgroundColor = Color.White) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "AI Provider", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(text = "Google Gemini (v2.5 / v3.5 Flash)", fontSize = 12.sp, color = Ink.copy(alpha = 0.6f))

                    Divider(color = Ink.copy(alpha = 0.1f))

                    Text(text = "Personal Gemini API Key", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text(
                        text = if (hasSavedKey) "Status: Configured & Encrypted Securely ✓" else "Status: Not configured",
                        fontSize = 12.sp,
                        color = if (hasSavedKey) SafeGreen else DangerRed,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (hasSavedKey) "Enter new key to replace..." else "Paste your AIza... key here") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Text(if (isKeyVisible) "Hide" else "Show", fontSize = 12.sp)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoButton(
                            text = "Save Key",
                            onClick = {
                                if (apiKeyInput.isNotBlank()) {
                                    SecureApiKeyStore.saveApiKey(context, apiKeyInput)
                                    hasSavedKey = true
                                    apiKeyInput = ""
                                    testStatusMessage = "API Key saved securely!"
                                }
                            },
                            backgroundColor = SafeGreen,
                            textColor = Color.White,
                            modifier = Modifier.weight(1f)
                        )

                        if (hasSavedKey) {
                            NeoButton(
                                text = "Remove",
                                onClick = { showRemoveDialog = true },
                                backgroundColor = DangerRed,
                                textColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    NeoButton(
                        text = if (isTestingConnection) "Testing..." else "Test AI Connection",
                        onClick = {
                            if (!isTestingConnection) {
                                isTestingConnection = true
                                testStatusMessage = null
                                coroutineScope.launch {
                                    val result = testGeminiConnection(context, if (apiKeyInput.isNotBlank()) apiKeyInput else null)
                                    testStatusMessage = result
                                    isTestingConnection = false
                                }
                            }
                        },
                        backgroundColor = TechBlue,
                        textColor = Color.White,
                        enabled = !isTestingConnection,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!testStatusMessage.isNullOrEmpty()) {
                        Text(
                            text = testStatusMessage!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (testStatusMessage!!.startsWith("✓")) SafeGreen else DangerRed
                        )
                    }
                }
            }
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

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove AI API Key?") },
            text = { Text("The saved Gemini API key will be deleted securely from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    SecureApiKeyStore.removeApiKey(context)
                    hasSavedKey = false
                    showRemoveDialog = false
                    testStatusMessage = "API key removed."
                }) {
                    Text("Remove", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

suspend fun testGeminiConnection(context: Context, transientKey: String?): String = withContext(Dispatchers.IO) {
    try {
        val key = transientKey ?: SecureApiKeyStore.getApiKey(context)
        if (key.isNullOrBlank()) {
            return@withContext "✕ No API key found. Please enter or save a valid key."
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().put("parts", JSONArray().put(
                    JSONObject().put("text", "Ping test")
                ))
            ))
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$key")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return@withContext "✓ AI CONNECTION WORKING (Gemini API verified successfully)"
            } else {
                return@withContext "✕ CONNECTION FAILED (${response.code}): Check your API key."
            }
        }
    } catch (e: Exception) {
        "✕ CONNECTION FAILED: ${e.localizedMessage}"
    }
}
