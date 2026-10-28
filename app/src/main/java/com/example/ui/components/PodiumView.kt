package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Participant
import com.example.model.Tournament
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.MedalBronze
import com.example.ui.theme.MedalGold
import com.example.ui.theme.MedalSilver
import com.example.ui.theme.PingPongOrange

@Composable
fun PodiumDialog(
    tournament: Tournament,
    language: AppLanguage,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    val winner = tournament.winner
    val runnerUp = tournament.runnerUp
    val third = tournament.thirdPlace

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, MedalGold.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            // Confetti falling inside dialog
            ConfettiOverlay(modifier = Modifier.matchParentSize())

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MedalGold, Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Champion Trophy",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.get("champion_title", language),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Black
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = tournament.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = CairoFontFamily
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Podium steps: 2nd, 1st, 3rd
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 2nd Place (Silver)
                    PodiumStep(
                        rank = 2,
                        participant = runnerUp,
                        medal = "🥈",
                        medalColor = MedalSilver,
                        pedestalHeight = 110.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 1st Place (Gold)
                    PodiumStep(
                        rank = 1,
                        participant = winner,
                        medal = "🥇",
                        medalColor = MedalGold,
                        pedestalHeight = 150.dp,
                        modifier = Modifier.weight(1.1f),
                        isFirst = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 3rd Place (Bronze)
                    PodiumStep(
                        rank = 3,
                        participant = third,
                        medal = "🥉",
                        medalColor = MedalBronze,
                        pedestalHeight = 85.dp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = AppStrings.get("close", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = onShare,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp)
                            .testTag("share_podium_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PingPongOrange)
                    ) {
                        Text(
                            text = AppStrings.get("share_results", language),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumStep(
    rank: Int,
    participant: Participant?,
    medal: String,
    medalColor: Color,
    pedestalHeight: Dp,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Medal icon / emoji
        Text(
            text = medal,
            fontSize = if (isFirst) 28.sp else 22.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Name
        Text(
            text = participant?.displayName ?: "—",
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = CairoFontFamily,
                fontWeight = if (isFirst) FontWeight.Bold else FontWeight.Medium,
                fontSize = if (isFirst) 13.sp else 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            medalColor.copy(alpha = if (isFirst) 0.85f else 0.65f),
                            medalColor.copy(alpha = 0.35f)
                        )
                    )
                )
                .border(
                    1.dp,
                    medalColor,
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = if (isFirst) 36.sp else 28.sp
                ),
                color = Color.White
            )
        }
    }
}
