package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Match
import com.example.model.MatchRule
import com.example.model.SetScore
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import com.example.ui.theme.SuccessGreen

@Composable
fun ScoreDialog(
    match: Match,
    rules: MatchRule,
    language: AppLanguage,
    onSaveScore: (List<SetScore>) -> Unit,
    onResetScore: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val p1 = match.participant1
    val p2 = match.participant2

    // Initialize with existing sets or empty sets up to bestOf
    var setScores by remember {
        val list = ArrayList<SetScore>()
        if (match.sets.isNotEmpty()) {
            list.addAll(match.sets)
        }
        while (list.size < rules.bestOf) {
            list.add(SetScore(0, 0))
        }
        mutableStateOf(list.toList())
    }

    fun vibrateShort() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    // Determine current score and winner
    val p1SetsWon = setScores.count { it.winner(rules) == 1 }
    val p2SetsWon = setScores.count { it.winner(rules) == 2 }
    val matchWinner = when {
        p1SetsWon >= rules.setsToWin -> p1
        p2SetsWon >= rules.setsToWin -> p2
        else -> null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppStrings.get("score_match_title", language),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = AppStrings.get("best_of_format", language, rules.bestOf) + " • " +
                                    AppStrings.get("points_per_set", language, rules.pointsToWin),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = CairoFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_score_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = AppStrings.get("close", language))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scoreboard Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Player 1
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p1?.displayName ?: "-",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$p1SetsWon",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (p1SetsWon >= rules.setsToWin) SuccessGreen else PingPongOrange
                            )
                        }

                        // VS separator
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "VS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = AppStrings.get("sets", language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = CairoFontFamily
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Player 2
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = p2?.displayName ?: "-",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$p2SetsWon",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black
                                ),
                                color = if (p2SetsWon >= rules.setsToWin) SuccessGreen else PingPongOrange
                            )
                        }
                    }

                    // Winner banner if decided
                    if (matchWinner != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${AppStrings.get("winner_label", language)} ${matchWinner.displayName}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sets Steppers
                setScores.forEachIndexed { index, setScore ->
                    val isSetNeeded = index == 0 || (p1SetsWon < rules.setsToWin && p2SetsWon < rules.setsToWin) || setScore.isFinished(rules)
                    val setWinner = setScore.winner(rules)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (setWinner != null) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppStrings.get("set_label", language, index + 1),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (setWinner != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SuccessGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (setWinner == 1) (p1?.displayName ?: "") else (p2?.displayName ?: ""),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // P1 Stepper
                                PointStepper(
                                    score = setScore.player1Score,
                                    isWinner = setWinner == 1,
                                    onMinus = {
                                        if (setScore.player1Score > 0) {
                                            vibrateShort()
                                            val newList = setScores.toMutableList()
                                            newList[index] = setScore.copy(player1Score = setScore.player1Score - 1)
                                            setScores = newList
                                        }
                                    },
                                    onPlus = {
                                        vibrateShort()
                                        val newList = setScores.toMutableList()
                                        newList[index] = setScore.copy(player1Score = setScore.player1Score + 1)
                                        setScores = newList
                                    },
                                    testTagPrefix = "set_${index}_p1"
                                )

                                Text(
                                    text = ":",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )

                                // P2 Stepper
                                PointStepper(
                                    score = setScore.player2Score,
                                    isWinner = setWinner == 2,
                                    onMinus = {
                                        if (setScore.player2Score > 0) {
                                            vibrateShort()
                                            val newList = setScores.toMutableList()
                                            newList[index] = setScore.copy(player2Score = setScore.player2Score - 1)
                                            setScores = newList
                                        }
                                    },
                                    onPlus = {
                                        vibrateShort()
                                        val newList = setScores.toMutableList()
                                        newList[index] = setScore.copy(player2Score = setScore.player2Score + 1)
                                        setScores = newList
                                    },
                                    testTagPrefix = "set_${index}_p2"
                                )
                            }

                            // Quick score chips for fast entry
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(Pair(11, 8), Pair(11, 9), Pair(12, 10), Pair(8, 11), Pair(9, 11)).forEach { (s1, s2) ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("preset_${index}_${s1}_$s2")
                                    ) {
                                        Text(
                                            text = "$s1-$s2",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            modifier = Modifier.padding(2.dp),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (match.isCompleted) {
                        OutlinedButton(
                            onClick = {
                                onResetScore()
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("undo_match_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("undo_result", language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val activeSets = setScores.filter { it.player1Score > 0 || it.player2Score > 0 }
                            onSaveScore(activeSets.ifEmpty { setScores.take(1) })
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("save_score_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PingPongOrange
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.get("save_result", language),
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
private fun PointStepper(
    score: Int,
    isWinner: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    testTagPrefix: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Minus Button
        IconButton(
            onClick = onMinus,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                .testTag("${testTagPrefix}_minus")
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.onSurface)
        }

        // Score display
        Box(
            modifier = Modifier
                .size(54.dp, 44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isWinner) SuccessGreen.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    1.dp,
                    if (isWinner) SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                color = if (isWinner) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )
        }

        // Plus Button
        IconButton(
            onClick = onPlus,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(PingPongOrange.copy(alpha = 0.15f))
                .border(1.dp, PingPongOrange.copy(alpha = 0.5f), CircleShape)
                .testTag("${testTagPrefix}_plus")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = PingPongOrange)
        }
    }
}
