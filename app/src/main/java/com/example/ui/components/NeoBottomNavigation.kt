package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Ink
import com.example.LocalHapticManager
import com.example.LocalAudioManager

@Composable
fun NeoBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind { 
                drawLine(color = Ink, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 8.dp.toPx())
            }
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NeoNavItem(
            label = "LABS",
            isSelected = currentRoute == "home",
            onClick = { onNavigate("home") }
        )
        NeoNavItem(
            label = "STATS",
            isSelected = currentRoute == "student_dashboard",
            onClick = { onNavigate("student_dashboard") }
        )
        NeoNavItem(
            label = "TEACH",
            isSelected = currentRoute == "teacher_workspace",
            onClick = { onNavigate("teacher_workspace") }
        )
        NeoNavItem(
            label = "SET",
            isSelected = currentRoute == "settings",
            onClick = { onNavigate("settings") }
        )
    }
}

@Composable
fun NeoNavItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hapticManager = LocalHapticManager.current
    val audioManager = LocalAudioManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val translationY by animateFloatAsState(targetValue = if (isPressed) 4f else 0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticManager.triggerHapticFeedback()
                    audioManager.playSound(com.example.audio.AppAudioManager.SoundType.CLICK)
                    onClick()
                }
            )
            .graphicsLayer { this.translationY = translationY }
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, Ink, if (label == "SET") RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp))
                .background(if (isSelected) Ink else Color.Transparent)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Ink else Ink.copy(alpha = 0.5f)
        )
    }
}
