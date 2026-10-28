package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TournamentEngine
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Match
import com.example.model.SetScore
import com.example.model.Tournament
import com.example.model.TournamentFormat
import com.example.ui.components.BingBongIcon
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.MatchCard
import com.example.ui.components.PodiumDialog
import com.example.ui.components.ScoreDialog
import com.example.ui.components.ShareUtil
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.MedalBronze
import com.example.ui.theme.MedalGold
import com.example.ui.theme.MedalSilver
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    tournament: Tournament,
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    onOpenHistory: () -> Unit,
    onNewTournament: () -> Unit,
    onTournamentUpdated: (Tournament) -> Unit
) {
    val context = LocalContext.current
    var selectedMatchForScoring by remember { mutableStateOf<Match?>(null) }
    var showPodium by remember { mutableStateOf(tournament.isFinished) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Bracket/Rounds, 1: Standings

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNewTournament,
                        modifier = Modifier.testTag("back_to_setup_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppStrings.get("new_tournament", language))
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BingBongIcon(size = 36.dp, animated = false)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tournament.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (tournament.format == TournamentFormat.KNOCKOUT) AppStrings.get("knockout", language)
                                else AppStrings.get("round_robin", language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = CairoFontFamily
                                ),
                                color = PingPongOrange
                            )
                        }
                    }
                },
                actions = {
                    // Trophy button to show champion podium
                    if (tournament.isFinished) {
                        IconButton(
                            onClick = { showPodium = true },
                            modifier = Modifier.testTag("show_podium_topbar_button")
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = "Podium", tint = MedalGold)
                        }
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val text = ShareUtil.generateReportText(tournament, language)
                            ShareUtil.shareText(context, text, tournament.name)
                        },
                        modifier = Modifier.testTag("share_tournament_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = AppStrings.get("share_results", language))
                    }

                    // History button
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(Icons.Default.History, contentDescription = AppStrings.get("history_title", language))
                    }

                    // Language toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLanguageToggle() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Text(
                            text = AppStrings.get("switch_lang", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PingPongOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs for Round Robin (Rounds vs Standings)
            if (tournament.format == TournamentFormat.ROUND_ROBIN) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = PingPongOrange
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                AppStrings.get("rounds_tab", language),
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                AppStrings.get("standings_tab", language),
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            // Main Content Area
            if (tournament.format == TournamentFormat.KNOCKOUT) {
                KnockoutBracketView(
                    tournament = tournament,
                    language = language,
                    onMatchClick = { selectedMatchForScoring = it }
                )
            } else {
                if (selectedTab == 0) {
                    RoundRobinMatchesView(
                        tournament = tournament,
                        language = language,
                        onMatchClick = { selectedMatchForScoring = it }
                    )
                } else {
                    StandingsTableView(
                        tournament = tournament,
                        language = language
                    )
                }
            }
        }
    }

    // Score Dialog
    selectedMatchForScoring?.let { match ->
        ScoreDialog(
            match = match,
            rules = tournament.rules,
            language = language,
            onSaveScore = { sets ->
                val updated = TournamentEngine.updateMatchScore(tournament, match.id, sets)
                onTournamentUpdated(updated)
                if (updated.isFinished && !tournament.isFinished) {
                    showPodium = true
                }
            },
            onResetScore = {
                val updated = TournamentEngine.resetMatchScore(tournament, match.id)
                onTournamentUpdated(updated)
            },
            onDismiss = { selectedMatchForScoring = null }
        )
    }

    // Podium Celebration Dialog
    if (showPodium && tournament.isFinished) {
        PodiumDialog(
            tournament = tournament,
            language = language,
            onShare = {
                val text = ShareUtil.generateReportText(tournament, language)
                ShareUtil.shareText(context, text, tournament.name)
            },
            onDismiss = { showPodium = false }
        )
    }
}

@Composable
private fun KnockoutBracketView(
    tournament: Tournament,
    language: AppLanguage,
    onMatchClick: (Match) -> Unit
) {
    // Horizontally scrollable tree bracket view
    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        tournament.rounds.forEachIndexed { roundIndex, round ->
            val roundTitle = when (round.titleKey) {
                "final" -> AppStrings.get("final", language)
                "semi" -> AppStrings.get("semi", language)
                "quarter" -> AppStrings.get("quarter", language)
                "round_of_16" -> AppStrings.get("round_of_16", language)
                "round_of_32" -> AppStrings.get("round_of_32", language)
                else -> AppStrings.get("preliminary", language)
            }

            Column(
                modifier = Modifier
                    .width(260.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Round Header Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (round.titleKey == "final") MedalGold.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            1.dp,
                            if (round.titleKey == "final") MedalGold
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (round.titleKey == "final") {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MedalGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = roundTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = if (round.titleKey == "final") MedalGold else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Matches in this round
                round.matches.forEachIndexed { matchIndex, match ->
                    MatchCard(
                        match = match,
                        rules = tournament.rules,
                        language = language,
                        onClick = { onMatchClick(match) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun RoundRobinMatchesView(
    tournament: Tournament,
    language: AppLanguage,
    onMatchClick: (Match) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tournament.rounds, key = { it.roundIndex }) { round ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppStrings.get("round_n", language, round.roundNumber),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PingPongOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    round.matches.forEach { match ->
                        MatchCard(
                            match = match,
                            rules = tournament.rules,
                            language = language,
                            onClick = { onMatchClick(match) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsTableView(
    tournament: Tournament,
    language: AppLanguage
) {
    val standings = remember(tournament) {
        TournamentEngine.calculateStandings(tournament)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Table Container Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "#", modifier = Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text(text = "اللاعب / الفريق", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold))
                    Text(text = "ل", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text(text = "ف", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text(text = "خ", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text(text = "أشواط", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                    Text(text = "نقاط", modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall.copy(fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold), textAlign = TextAlign.Center)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                // Rows
                standings.forEachIndexed { index, standing ->
                    val rankMedal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "${index + 1}"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (index == 0 && tournament.isFinished) SuccessGreen.copy(alpha = 0.12f)
                                else if (index % 2 == 0) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                else Color.Transparent
                            )
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rankMedal,
                            modifier = Modifier.width(28.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = standing.participant.displayName,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "${standing.played}", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text(text = "${standing.won}", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SuccessGreen), textAlign = TextAlign.Center)
                        Text(text = "${standing.lost}", modifier = Modifier.width(26.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text(text = "${standing.setsWon}-${standing.setsLost}", modifier = Modifier.width(42.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text(text = "${standing.points}", modifier = Modifier.width(36.dp), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black, color = PingPongOrange), textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}
