package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Match
import com.example.model.MatchRule
import com.example.model.Participant
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import com.example.ui.theme.SuccessGreen

@Composable
fun MatchCard(
    match: Match,
    rules: MatchRule,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val p1 = match.participant1
    val p2 = match.participant2
    val p1Sets = match.player1SetsWon(rules)
    val p2Sets = match.player2SetsWon(rules)
    val hasPlayers = p1 != null && (p2 != null || match.isBye)

    val cardBorderColor = when {
        match.isCompleted -> SuccessGreen.copy(alpha = 0.6f)
        hasPlayers -> PingPongOrange.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, cardBorderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = hasPlayers && !match.isBye) { onClick() }
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: status badge + sets preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                StatusBadge(match = match, language = language)

                // Detailed set scores chips (e.g. 11-9, 11-8)
                if (match.sets.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        match.sets.take(3).forEach { s ->
                            Text(
                                text = "${s.player1Score}-${s.player2Score}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player 1 Row
            ParticipantRow(
                participant = p1,
                setsWon = p1Sets,
                isWinner = match.winner?.id == p1?.id && p1 != null,
                placeholder = if (p1 == null) "—" else null
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Divider or VS line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Player 2 Row (or Bye)
            if (match.isBye) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "— ${AppStrings.get("status_bye", language)} —",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = NetTurquoise
                    )
                }
            } else {
                ParticipantRow(
                    participant = p2,
                    setsWon = p2Sets,
                    isWinner = match.winner?.id == p2?.id && p2 != null,
                    placeholder = if (p2 == null) "—" else null
                )
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant?,
    setsWon: Int,
    isWinner: Boolean,
    placeholder: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isWinner) SuccessGreen.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isWinner) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Winner",
                    tint = SuccessGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = participant?.displayName ?: (placeholder ?: ""),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = CairoFontFamily,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium
                ),
                color = when {
                    isWinner -> SuccessGreen
                    participant != null -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Sets count badge
        Box(
            modifier = Modifier
                .size(28.dp, 22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isWinner) SuccessGreen
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$setsWon",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isWinner) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatusBadge(match: Match, language: AppLanguage) {
    val (label, bg, fg) = when {
        match.isBye -> Triple(
            AppStrings.get("status_bye", language),
            NetTurquoise.copy(alpha = 0.15f),
            NetTurquoise
        )
        match.isCompleted -> Triple(
            AppStrings.get("status_completed", language),
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen
        )
        match.participant1 != null && match.participant2 != null -> Triple(
            AppStrings.get("status_in_progress", language),
            PingPongOrange.copy(alpha = 0.15f),
            PingPongOrange
        )
        else -> Triple(
            AppStrings.get("status_upcoming", language),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = fg
        )
    }
}
