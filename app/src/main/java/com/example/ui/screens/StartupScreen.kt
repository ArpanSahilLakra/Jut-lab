package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.LocalAppPreferences
import com.example.data.LabDao
import com.example.ui.theme.Ink
import com.example.ui.theme.OffWhite
import com.example.ui.theme.TechBlue
import com.example.ui.theme.SafeGreen
import kotlin.math.sin

import com.example.LocalAudioManager
import com.example.audio.AppAudioManager
import androidx.compose.ui.platform.LocalView

@Composable
fun StartupScreen(
    labDao: LabDao,
    onStartupComplete: () -> Unit
) {
    val viewModel: StartupViewModel = viewModel(factory = StartupViewModelFactory(labDao))
    val state by viewModel.startupState.collectAsState()
    val isComplete by viewModel.startupComplete.collectAsState()
    val reduceMotion by LocalAppPreferences.current.reduceMotion.collectAsState(initial = false)
    val audioManager = LocalAudioManager.current
    val view = LocalView.current

    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        viewModel.beginStartupSequence()
    }

    LaunchedEffect(isComplete) {
        if (isComplete) {
            audioManager.playSound(AppAudioManager.SoundType.SUCCESS, view)
            onStartupComplete()
        }
    }

    // Animation values
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else (if (reduceMotion) 1f else 0.92f),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "logo_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Electronic Signal Background Animation
        if (!reduceMotion && startAnimation && !isComplete) {
            SignalWaveform(modifier = Modifier.fillMaxSize())
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(logoAlpha)
        ) {
            // Main Logo/Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
            ) {
                Text(
                    text = "ENGINEERING LAB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = TechBlue.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "JUT ECE",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "VIRTUAL LAB",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Subtitle / Status indicator
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800, delayMillis = 600)),
                exit = fadeOut(tween(300))
            ) {
                Text(
                    text = state.message,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state == StartupState.READY) SafeGreen else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SignalWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "signal")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    val color = TechBlue

    Canvas(modifier = modifier) {
        val path = Path()
        val width = size.width
        val height = size.height
        val centerY = height / 2

        path.moveTo(0f, centerY)
        for (x in 0..width.toInt() step 5) {
            // Create a stylized damped sine wave or pulse trace
            val xNorm = x / width
            // Sine wave with decaying amplitude near edges
            val envelope = kotlin.math.sin(xNorm * Math.PI).toFloat() 
            val yOffset = kotlin.math.sin((x / 50f) - phase).toFloat() * 30f * envelope
            path.lineTo(x.toFloat(), centerY + yOffset)
        }

        drawPath(
            path = path,
            color = color.copy(alpha = 0.15f),
            style = Stroke(
                width = 4f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Occasional digital pulse dots traversing the path
        val dotX = (phase / (2 * Math.PI).toFloat()) * width
        val dotEnvelope = kotlin.math.sin((dotX / width) * Math.PI).toFloat()
        val dotYOffset = kotlin.math.sin((dotX / 50f) - phase).toFloat() * 30f * dotEnvelope
        
        if (dotEnvelope > 0.1f) {
            drawCircle(
                color = color.copy(alpha = 0.8f),
                radius = 6f,
                center = Offset(dotX, centerY + dotYOffset)
            )
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = 12f,
                center = Offset(dotX, centerY + dotYOffset)
            )
        }
    }
}
