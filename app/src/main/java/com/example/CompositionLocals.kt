package com.example

import androidx.compose.runtime.compositionLocalOf
import com.example.audio.AppAudioManager
import com.example.audio.AppHapticManager
import com.example.data.AppPreferences

import com.example.audio.AppNotificationManager

val LocalAppPreferences = compositionLocalOf<AppPreferences> { error("No AppPreferences provided") }
val LocalAudioManager = compositionLocalOf<AppAudioManager> { error("No AudioManager provided") }
val LocalHapticManager = compositionLocalOf<AppHapticManager> { error("No HapticManager provided") }
val LocalNotificationManager = compositionLocalOf<AppNotificationManager> { error("No NotificationManager provided") }
