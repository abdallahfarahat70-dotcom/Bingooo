package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.ui.theme.MedalGold
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import com.example.ui.theme.SuccessGreen
import kotlin.random.Random

data class ConfettiParticle(
    val initialXRatio: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float,
    val horizontalWobble: Float
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 75
) {
    val progress = remember { Animatable(0f) }

    val colors = listOf(
        MedalGold,
        PingPongOrange,
        NetTurquoise,
        SuccessGreen,
        Color(0xFFFF8533),
        Color.White
    )

    val particles = remember {
        val rand = Random(42)
        (0 until particleCount).map {
            ConfettiParticle(
                initialXRatio = rand.nextFloat(),
                speed = 0.6f + rand.nextFloat() * 0.7f,
                size = 12f + rand.nextFloat() * 16f,
                color = colors[rand.nextInt(colors.size)],
                rotationSpeed = (rand.nextFloat() - 0.5f) * 720f,
                horizontalWobble = (rand.nextFloat() - 0.5f) * 60f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = progress.value

        for (p in particles) {
            val y = (t * p.speed * h * 1.3f) - (p.size * 2)
            val wobble = kotlin.math.sin(t * 10f + p.initialXRatio * 20f) * p.horizontalWobble
            val x = (p.initialXRatio * w) + wobble

            if (y in -50f..h + 50f) {
                rotate(degrees = t * p.rotationSpeed, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(x, y),
                        size = Size(p.size, p.size * 0.55f)
                    )
                }
            }
        }
    }
}
