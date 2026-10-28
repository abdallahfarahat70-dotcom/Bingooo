package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Tournament
import com.example.model.TournamentFormat
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.MedalGold
import com.example.ui.theme.PingPongOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDialog(
    tournaments: List<Tournament>,
    language: AppLanguage,
    onSelectTournament: (Tournament) -> Unit,
    onDeleteTournament: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(480.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.get("history_title", language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = AppStrings.get("close", language))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (tournaments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.get("history_empty", language),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CairoFontFamily),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(tournaments, key = { it.id }) { item ->
                            val dateStr = try {
                                val sdf = SimpleDateFormat("d MMMM - HH:mm", if (language == AppLanguage.ARABIC) Locale("ar") else Locale.ENGLISH)
                                sdf.format(Date(item.createdAt))
                            } catch (e: Exception) { "" }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectTournament(item)
                                        onDismiss()
                                    }
                                    .testTag("history_item_${item.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = (if (item.format == TournamentFormat.KNOCKOUT) AppStrings.get("knockout", language) else AppStrings.get("round_robin", language)) +
                                                    " • " + AppStrings.get("players_count", language, item.participants.size) +
                                                    if (dateStr.isNotBlank()) " • $dateStr" else "",
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = CairoFontFamily),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (item.winner != null) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                            ) {
                                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MedalGold, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = item.winner.displayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = CairoFontFamily,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = MedalGold
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { onDeleteTournament(item.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = AppStrings.get("delete", language),
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
