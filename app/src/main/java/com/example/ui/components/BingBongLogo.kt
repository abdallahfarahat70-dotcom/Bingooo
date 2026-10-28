package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import com.example.ui.theme.PingPongOrangeLight

@Composable
fun BingBongIcon(
    size: Dp = 44.dp,
    animated: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ball_bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val actualBounce = if (animated) bounceY else 0f

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.82f)) {
            val w = this.size.width
            val h = this.size.height

            // Ping pong paddle blade (circular/oval)
            val paddleRadius = w * 0.28f
            val paddleCenter = Offset(w * 0.42f, h * 0.42f)

            // Rubber face
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PingPongOrangeLight, PingPongOrange),
                    center = paddleCenter,
                    radius = paddleRadius
                ),
                radius = paddleRadius,
                center = paddleCenter
            )

            // Paddle wooden handle
            val handlePath = Path().apply {
                moveTo(paddleCenter.x - paddleRadius * 0.28f, paddleCenter.y + paddleRadius * 0.72f)
                lineTo(w * 0.22f, h * 0.88f)
                lineTo(w * 0.32f, h * 0.94f)
                lineTo(paddleCenter.x + paddleRadius * 0.28f, paddleCenter.y + paddleRadius * 0.72f)
                close()
            }
            drawPath(
                path = handlePath,
                color = Color(0xFFD97706) // Wood warm brown
            )

            // Motion bounce arc trail
            val trailPath = Path().apply {
                moveTo(w * 0.48f, h * 0.58f)
                quadraticBezierTo(
                    w * 0.58f, h * 0.72f,
                    w * 0.78f, h * 0.38f + actualBounce * (h / 40f)
                )
            }
            drawPath(
                path = trailPath,
                color = NetTurquoise.copy(alpha = 0.7f),
                style = Stroke(width = w * 0.06f, cap = StrokeCap.Round)
            )

            // Ping pong ball
            val ballCenter = Offset(w * 0.78f, h * 0.34f + actualBounce * (h / 40f))
            val ballRadius = w * 0.12f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFFFEDD5)),
                    center = Offset(ballCenter.x - ballRadius * 0.3f, ballCenter.y - ballRadius * 0.3f),
                    radius = ballRadius
                ),
                radius = ballRadius,
                center = ballCenter
            )
        }
    }
}

@Composable
fun BingBongHeaderLogo(
    modifier: Modifier = Modifier,
    showTagline: Boolean = false,
    taglineText: String = ""
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BingBongIcon(size = 46.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Bing ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "bong",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    ),
                    color = PingPongOrange
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NetTurquoise)
                )
            }
            if (showTagline && taglineText.isNotBlank()) {
                Text(
                    text = taglineText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
