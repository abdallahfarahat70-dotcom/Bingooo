package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TournamentEngine
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.DoublesEntryMode
import com.example.model.MatchRule
import com.example.model.Participant
import com.example.model.Tournament
import com.example.model.TournamentFormat
import com.example.model.TournamentType
import com.example.ui.components.BingBongHeaderLogo
import com.example.ui.components.SegmentOption
import com.example.ui.components.TactileSegmentedControl
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.NetTurquoise
import com.example.ui.theme.PingPongOrange
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    onThemeToggle: () -> Unit,
    onOpenHistory: () -> Unit,
    onTournamentCreated: (Tournament) -> Unit
) {
    var tournamentType by remember { mutableStateOf(TournamentType.SINGLES) }
    var tournamentFormat by remember { mutableStateOf(TournamentFormat.KNOCKOUT) }
    var tournamentName by remember { mutableStateOf("") }

    // Match Rules
    var bestOfSets by remember { mutableIntStateOf(3) }
    var pointsToWin by remember { mutableIntStateOf(11) }
    var pointsMargin by remember { mutableIntStateOf(2) }

    // Singles Players
    val singlesPlayers = remember { mutableStateListOf<String>() }
    var currentInputName by remember { mutableStateOf("") }

    // Doubles state
    var doublesMode by remember { mutableStateOf(DoublesEntryMode.RANDOM_DRAW) }
    val doublesIndividualPlayers = remember { mutableStateListOf<String>() }
    val doublesManualTeams = remember { mutableStateListOf<Pair<String, String>>() }
    var teamPlayer1Input by remember { mutableStateOf("") }
    var teamPlayer2Input by remember { mutableStateOf("") }

    // Dialogs
    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteTextContent by remember { mutableStateOf("") }
    var oddPlayerAlert by remember { mutableStateOf<String?>(null) }

    fun addPlayerFromInput() {
        val trimmed = currentInputName.trim()
        if (trimmed.isNotBlank()) {
            if (tournamentType == TournamentType.SINGLES) {
                if (!singlesPlayers.contains(trimmed)) singlesPlayers.add(trimmed)
            } else {
                if (!doublesIndividualPlayers.contains(trimmed)) doublesIndividualPlayers.add(trimmed)
            }
            currentInputName = ""
        }
    }

    fun addSamplePlayers() {
        val sampleNames = listOf(
            "محمد صلاح", "عمر خالد", "أحمد علي", "كريم عادل",
            "يوسف هاني", "طارق فتحي", "سامي إبراهيم", "زياد وليد"
        )
        if (tournamentType == TournamentType.SINGLES) {
            singlesPlayers.clear()
            singlesPlayers.addAll(sampleNames)
        } else {
            doublesIndividualPlayers.clear()
            doublesIndividualPlayers.addAll(sampleNames)
        }
    }

    val totalParticipantCount = when (tournamentType) {
        TournamentType.SINGLES -> singlesPlayers.size
        TournamentType.DOUBLES -> {
            if (doublesMode == DoublesEntryMode.MANUAL_PAIRS) doublesManualTeams.size
            else doublesIndividualPlayers.size / 2
        }
    }

    val canStart = totalParticipantCount >= 2

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BingBongHeaderLogo(
                        showTagline = true,
                        taglineText = AppStrings.get("app_tagline", language)
                    )
                },
                actions = {
                    // History button
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = AppStrings.get("history_title", language),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Language toggle chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onLanguageToggle() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Text(
                            text = AppStrings.get("switch_lang", language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = PingPongOrange
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Tournament Type: Singles vs Doubles
            TactileSegmentedControl(
                options = listOf(
                    SegmentOption(
                        value = TournamentType.SINGLES,
                        title = AppStrings.get("singles", language),
                        icon = Icons.Default.Person,
                        testTag = "type_singles_button"
                    ),
                    SegmentOption(
                        value = TournamentType.DOUBLES,
                        title = AppStrings.get("doubles", language),
                        icon = Icons.Default.Groups,
                        testTag = "type_doubles_button"
                    )
                ),
                selectedValue = tournamentType,
                onOptionSelected = { tournamentType = it }
            )

            // 2. Tournament Format: Knockout vs Round Robin
            TactileSegmentedControl(
                options = listOf(
                    SegmentOption(
                        value = TournamentFormat.KNOCKOUT,
                        title = AppStrings.get("knockout", language),
                        icon = Icons.Default.SportsTennis,
                        testTag = "format_knockout_button"
                    ),
                    SegmentOption(
                        value = TournamentFormat.ROUND_ROBIN,
                        title = AppStrings.get("round_robin", language),
                        icon = Icons.Default.Refresh,
                        testTag = "format_roundrobin_button"
                    )
                ),
                selectedValue = tournamentFormat,
                onOptionSelected = { tournamentFormat = it }
            )

            // 3. Tournament Name Input (Optional)
            OutlinedTextField(
                value = tournamentName,
                onValueChange = { tournamentName = it },
                label = {
                    Text(
                        text = AppStrings.get("tournament_name", language),
                        fontFamily = CairoFontFamily
                    )
                },
                placeholder = {
                    Text(
                        text = AppStrings.get("tournament_name_placeholder", language),
                        fontFamily = CairoFontFamily
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tournament_name_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            // 4. Match Rules Configuration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppStrings.get("match_system", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Best of sets selector (1, 3, 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 3, 5).forEach { sets ->
                            val isSelected = bestOfSets == sets
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PingPongOrange else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) PingPongOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { bestOfSets = sets }
                                    .testTag("best_of_${sets}_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = AppStrings.get("best_of_format", language, sets),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Points per set & margin info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = AppStrings.get("points_per_set", language, pointsToWin),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        // +/- buttons for points
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { if (pointsToWin > 5) pointsToWin-- },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            Text(
                                text = "$pointsToWin",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { if (pointsToWin < 30) pointsToWin++ },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }

                    Text(
                        text = "• " + AppStrings.get("margin_points", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = CairoFontFamily
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Doubles Mode Sub-options (if Doubles selected)
            if (tournamentType == TournamentType.DOUBLES) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppStrings.get("doubles_mode", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    TactileSegmentedControl(
                        options = listOf(
                            SegmentOption(
                                value = DoublesEntryMode.RANDOM_DRAW,
                                title = AppStrings.get("doubles_random", language),
                                icon = Icons.Default.Shuffle,
                                testTag = "doubles_random_button"
                            ),
                            SegmentOption(
                                value = DoublesEntryMode.MANUAL_PAIRS,
                                title = AppStrings.get("doubles_manual", language),
                                icon = Icons.Default.Groups,
                                testTag = "doubles_manual_button"
                            )
                        ),
                        selectedValue = doublesMode,
                        onOptionSelected = { doublesMode = it }
                    )
                }
            }

            // 6. Players Entry Section
            if (tournamentType == TournamentType.SINGLES || doublesMode == DoublesEntryMode.RANDOM_DRAW) {
                val currentList = if (tournamentType == TournamentType.SINGLES) singlesPlayers else doublesIndividualPlayers

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header with count & utility buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (tournamentType == TournamentType.SINGLES) {
                                    AppStrings.get("players_count", language, currentList.size)
                                } else {
                                    AppStrings.get("players_count", language, currentList.size) + " (" +
                                            AppStrings.get("teams_count", language, currentList.size / 2) + ")"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = PingPongOrange
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Paste list button
                            FilledTonalButton(
                                onClick = { showPasteDialog = true },
                                modifier = Modifier.testTag("paste_names_button")
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.get("paste_list", language), fontFamily = CairoFontFamily, fontSize = 12.sp)
                            }

                            // Shuffle button
                            if (currentList.isNotEmpty()) {
                                IconButton(
                                    onClick = { currentList.shuffle() },
                                    modifier = Modifier.testTag("shuffle_players_button")
                                ) {
                                    Icon(Icons.Default.Shuffle, contentDescription = AppStrings.get("shuffle_button", language))
                                }
                            }
                        }
                    }

                    // Input Text Field + Add Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentInputName,
                            onValueChange = { currentInputName = it },
                            placeholder = {
                                Text(
                                    text = AppStrings.get("add_player_hint", language),
                                    fontFamily = CairoFontFamily,
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("player_name_input"),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { addPlayerFromInput() })
                        )

                        Button(
                            onClick = { addPlayerFromInput() },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("add_player_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PingPongOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_button", language))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.get("add_button", language),
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Sample Players Button
                    if (currentList.isEmpty()) {
                        OutlinedButton(
                            onClick = { addSamplePlayers() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_sample_players_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NetTurquoise, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (tournamentType == TournamentType.SINGLES) AppStrings.get("sample_players", language) else AppStrings.get("sample_doubles", language),
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = NetTurquoise
                            )
                        }
                    }

                    // Players Chips Flow
                    if (currentList.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            currentList.forEachIndexed { idx, name ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                                        .testTag("player_chip_$idx")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "${idx + 1}. $name",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = CairoFontFamily,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                        IconButton(
                                            onClick = { currentList.removeAt(idx) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove $name",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Odd Doubles Warning Banner
                    if (tournamentType == TournamentType.DOUBLES && currentList.size % 2 != 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFEF3C7) // Amber warning
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppStrings.get("odd_doubles_warning", language, currentList.size),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            currentList.add("احتياطي ${currentList.size + 1}")
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                                    ) {
                                        Text(AppStrings.get("add_reserve", language), fontFamily = CairoFontFamily, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (currentList.isNotEmpty()) currentList.removeLast()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(AppStrings.get("remove_last", language), fontFamily = CairoFontFamily, fontSize = 12.sp, color = Color(0xFF92400E))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Manual Pairs Input for Doubles
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = AppStrings.get("teams_count", language, doublesManualTeams.size),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = PingPongOrange
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = teamPlayer1Input,
                            onValueChange = { teamPlayer1Input = it },
                            placeholder = { Text(AppStrings.get("team_player1", language), fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = teamPlayer2Input,
                            onValueChange = { teamPlayer2Input = it },
                            placeholder = { Text(AppStrings.get("team_player2", language), fontFamily = CairoFontFamily) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            val p1 = teamPlayer1Input.trim()
                            val p2 = teamPlayer2Input.trim()
                            if (p1.isNotBlank() && p2.isNotBlank()) {
                                doublesManualTeams.add(Pair(p1, p2))
                                teamPlayer1Input = ""
                                teamPlayer2Input = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PingPongOrange)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.get("add_team", language), fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }

                    // Teams List
                    doublesManualTeams.forEachIndexed { index, pair ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. ${pair.first} & ${pair.second}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                IconButton(
                                    onClick = { doublesManualTeams.removeAt(index) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Start Tournament Button
            Button(
                onClick = {
                    val rules = MatchRule(
                        bestOf = bestOfSets,
                        pointsToWin = pointsToWin,
                        pointsMargin = pointsMargin
                    )
                    val name = tournamentName.trim().ifEmpty {
                        AppStrings.get("tournament_name_placeholder", language)
                    }

                    val finalParticipants = when (tournamentType) {
                        TournamentType.SINGLES -> {
                            singlesPlayers.map {
                                Participant(id = UUID.randomUUID().toString(), name = it)
                            }
                        }
                        TournamentType.DOUBLES -> {
                            if (doublesMode == DoublesEntryMode.MANUAL_PAIRS) {
                                doublesManualTeams.map {
                                    Participant(id = UUID.randomUUID().toString(), name = it.first, partnerName = it.second)
                                }
                            } else {
                                val (pairs, _) = TournamentEngine.pairDoublesRandomly(doublesIndividualPlayers)
                                pairs
                            }
                        }
                    }

                    val tournament = if (tournamentFormat == TournamentFormat.KNOCKOUT) {
                        TournamentEngine.generateKnockoutTournament(
                            name = name,
                            participants = finalParticipants,
                            rules = rules,
                            type = tournamentType
                        )
                    } else {
                        TournamentEngine.generateRoundRobinTournament(
                            name = name,
                            participants = finalParticipants,
                            rules = rules,
                            type = tournamentType
                        )
                    }
                    onTournamentCreated(tournament)
                },
                enabled = canStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("start_tournament_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PingPongOrange,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.get("start_tournament", language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }

            if (!canStart) {
                Text(
                    text = AppStrings.get("min_players_warning", language),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Paste Names List Dialog
    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = {
                Text(
                    text = AppStrings.get("paste_list", language),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = AppStrings.get("paste_hint", language),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = CairoFontFamily),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = pasteTextContent,
                        onValueChange = { pasteTextContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val names = pasteTextContent
                            .split("\n", ",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        if (tournamentType == TournamentType.SINGLES) {
                            for (n in names) {
                                if (!singlesPlayers.contains(n)) singlesPlayers.add(n)
                            }
                        } else {
                            for (n in names) {
                                if (!doublesIndividualPlayers.contains(n)) doublesIndividualPlayers.add(n)
                            }
                        }
                        pasteTextContent = ""
                        showPasteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PingPongOrange)
                ) {
                    Text(AppStrings.get("paste_confirm", language), fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text(AppStrings.get("close", language), fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
