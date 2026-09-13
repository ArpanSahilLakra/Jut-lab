package com.example.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

object AppAnimations {
    @Composable
    fun EnterFadeInSlideUp(
        reduceMotion: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        val enterTransition = if (reduceMotion) {
            fadeIn(animationSpec = tween(300))
        } else {
            fadeIn(animationSpec = tween(300)) + slideInVertically(
                initialOffsetY = { 50 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        
        AnimatedVisibility(
            visible = true,
            enter = enterTransition,
            content = content
        )
    }

    @Composable
    fun StandardAnimatedVisibility(
        visible: Boolean,
        reduceMotion: Boolean,
        content: @Composable AnimatedVisibilityScope.() -> Unit
    ) {
        val enterTransition = if (reduceMotion) fadeIn() else fadeIn() + expandVertically()
        val exitTransition = if (reduceMotion) fadeOut() else fadeOut() + shrinkVertically()
        
        AnimatedVisibility(
            visible = visible,
            enter = enterTransition,
            exit = exitTransition,
            content = content
        )
    }
}

// Keep bounceClick for backward compatibility where needed, but we will mostly use custom neoPress in components.
fun Modifier.bounceClick(
    reduceMotion: Boolean,
    interactionSource: MutableInteractionSource
) = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    if (reduceMotion) {
        this // Do not animate scale if reduce motion is enabled
    } else {
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.97f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "BounceClick"
        )
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    }
}
