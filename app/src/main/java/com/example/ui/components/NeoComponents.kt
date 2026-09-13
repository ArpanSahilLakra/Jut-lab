package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.LocalAppPreferences
import com.example.LocalAudioManager
import com.example.LocalHapticManager
import com.example.audio.AppAudioManager
import com.example.ui.animations.bounceClick
import com.example.ui.theme.Ink
import com.example.ui.theme.OffWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NeoCard(
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color.White,
  borderColor: Color = Ink,
  borderWidth: Dp = 4.dp,
  shadowOffsetX: Dp = 4.dp,
  shadowOffsetY: Dp = 4.dp,
  shadowColor: Color = Ink,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  val audioManager = LocalAudioManager.current
  val hapticManager = LocalHapticManager.current
  val appPreferences = LocalAppPreferences.current
  val reduceMotion by appPreferences.reduceMotion.collectAsState(initial = false)
  val interactionSource = remember { MutableInteractionSource() }
  val view = LocalView.current
  val isPressed by interactionSource.collectIsPressedAsState()

  val translation by animateDpAsState(
      targetValue = if (isPressed && onClick != null && !reduceMotion) 2.dp else 0.dp,
      animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f), label = "NeoCardPress"
  )
  val currentShadowX = shadowOffsetX - translation
  val currentShadowY = shadowOffsetY - translation

  val clickModifier = if (onClick != null) {
    Modifier
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = {
            hapticManager.triggerHapticFeedback()
            audioManager.playSound(AppAudioManager.SoundType.CLICK, view)
            onClick()
        }
      )
  } else {
    Modifier
  }

  Box(
    modifier = modifier
      .graphicsLayer {
          translationX = translation.toPx()
          translationY = translation.toPx()
      }
      .drawBehind {
        drawRect(
          color = shadowColor,
          topLeft = Offset(currentShadowX.toPx(), currentShadowY.toPx()),
          size = size
        )
      }
      .border(borderWidth, borderColor, RoundedCornerShape(4.dp))
      .background(backgroundColor, RoundedCornerShape(4.dp))
      .then(clickModifier)
      .padding(16.dp)
  ) {
    Column(content = content)
  }
}

@Composable
fun NeoButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color.White,
  textColor: Color = Ink,
  borderColor: Color = Ink,
  enabled: Boolean = true,
  buttonType: NeoButtonType = NeoButtonType.DEFAULT
) {
  val audioManager = LocalAudioManager.current
  val hapticManager = LocalHapticManager.current
  val appPreferences = LocalAppPreferences.current
  val reduceMotion by appPreferences.reduceMotion.collectAsState(initial = false)
  val interactionSource = remember { MutableInteractionSource() }
  val view = LocalView.current
  val isPressed by interactionSource.collectIsPressedAsState()
  val scope = rememberCoroutineScope()

  var isSuccessState by remember { mutableStateOf(false) }

  // Tactile Press Translation
  val pressTranslation by animateDpAsState(
      targetValue = if (isPressed && enabled && !reduceMotion) 3.dp else 0.dp,
      animationSpec = spring(dampingRatio = 0.7f, stiffness = 600f), label = "NeoButtonPress"
  )
  val currentShadow = 4.dp - pressTranslation

  // Specific animations based on type
  val iconRotation by animateFloatAsState(
      targetValue = if (isPressed && buttonType == NeoButtonType.RESET && !reduceMotion) 180f else 0f,
      animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f), label = "NeoButtonRotate"
  )
  val iconTranslationX by animateDpAsState(
      targetValue = if (isPressed && buttonType == NeoButtonType.BACK && !reduceMotion) (-4).dp else 0.dp,
      animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "NeoButtonTranslateX"
  )
  val iconScale by animateFloatAsState(
      targetValue = if (isPressed && buttonType == NeoButtonType.ADD && !reduceMotion) 1.2f else if (isPressed && buttonType == NeoButtonType.ZOOM_OUT && !reduceMotion) 0.8f else 1f,
      animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f), label = "NeoButtonScale"
  )

  Box(
    modifier = modifier
      .heightIn(min = 48.dp)
      .graphicsLayer {
          translationX = pressTranslation.toPx()
          translationY = pressTranslation.toPx()
      }
      .drawBehind {
        drawRect(
          color = if (enabled) borderColor else Color.Gray,
          topLeft = Offset(currentShadow.toPx(), currentShadow.toPx()),
          size = size
        )
      }
      .border(4.dp, if (enabled) borderColor else Color.Gray, RoundedCornerShape(4.dp))
      .background(if (enabled) backgroundColor else Color.LightGray, RoundedCornerShape(4.dp))
      .clickable(
          enabled = enabled,
          interactionSource = interactionSource,
          indication = null,
          onClick = {
            hapticManager.triggerHapticFeedback()
            audioManager.playSound(AppAudioManager.SoundType.CLICK, view)
            
            if (buttonType == NeoButtonType.VALIDATE || buttonType == NeoButtonType.SAVE) {
                scope.launch {
                    isSuccessState = true
                    delay(1500)
                    isSuccessState = false
                }
            }
            onClick()
          }
      )
      .padding(horizontal = 16.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        if (isSuccessState) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Success", tint = textColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (buttonType == NeoButtonType.SAVE) "SAVED" else "SUCCESS",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = textColor
            )
        } else {
            if (buttonType == NeoButtonType.RESET) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = textColor, modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = iconRotation })
                Spacer(modifier = Modifier.width(8.dp))
            } else if (buttonType == NeoButtonType.BACK) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor, modifier = Modifier.size(18.dp).graphicsLayer { translationX = iconTranslationX.toPx() })
                Spacer(modifier = Modifier.width(8.dp))
            } else if (buttonType == NeoButtonType.ADD) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = textColor, modifier = Modifier.size(18.dp).graphicsLayer { scaleX = iconScale; scaleY = iconScale })
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
              text = text.uppercase(),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = if (enabled) textColor else Color.DarkGray
            )
        }
    }
  }
}

@Composable
fun NeoHeader(
  title: String,
  subtitle: String? = null,
  modifier: Modifier = Modifier
) {
  NeoCard(
    modifier = modifier.fillMaxWidth(),
    backgroundColor = OffWhite
  ) {
    Text(
      text = title.uppercase(),
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      color = Ink,
      letterSpacing = 1.5.sp
    )
    if (subtitle != null) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Ink.copy(alpha = 0.8f)
      )
    }
  }
}
