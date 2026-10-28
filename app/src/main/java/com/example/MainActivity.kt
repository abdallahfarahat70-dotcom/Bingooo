package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.localization.AppLanguage
import com.example.model.Tournament
import com.example.repository.TournamentRepository
import com.example.ui.screens.HistoryDialog
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.TournamentScreen
import com.example.ui.theme.BingBongTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = TournamentRepository(this)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemDark) }
            var currentLanguage by remember { mutableStateOf(AppLanguage.ARABIC) }

            // Active tournament state
            var currentTournament by remember { mutableStateOf<Tournament?>(repository.getActiveTournament()) }
            var showHistoryDialog by remember { mutableStateOf(false) }
            var savedTournaments by remember { mutableStateOf(repository.getAllTournaments()) }

            val layoutDirection = if (currentLanguage == AppLanguage.ARABIC) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                BingBongTheme(darkTheme = isDarkMode) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (currentTournament == null) {
                            SetupScreen(
                                language = currentLanguage,
                                onLanguageToggle = {
                                    currentLanguage = if (currentLanguage == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                                },
                                onThemeToggle = { isDarkMode = !isDarkMode },
                                onOpenHistory = {
                                    savedTournaments = repository.getAllTournaments()
                                    showHistoryDialog = true
                                },
                                onTournamentCreated = { newTournament ->
                                    repository.saveTournament(newTournament)
                                    currentTournament = newTournament
                                    savedTournaments = repository.getAllTournaments()
                                }
                            )
                        } else {
                            // Back handler to return to setup
                            BackHandler {
                                currentTournament = null
                            }

                            TournamentScreen(
                                tournament = currentTournament!!,
                                language = currentLanguage,
                                onLanguageToggle = {
                                    currentLanguage = if (currentLanguage == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                                },
                                onOpenHistory = {
                                    savedTournaments = repository.getAllTournaments()
                                    showHistoryDialog = true
                                },
                                onNewTournament = {
                                    currentTournament = null
                                },
                                onTournamentUpdated = { updated ->
                                    repository.saveTournament(updated)
                                    currentTournament = updated
                                    savedTournaments = repository.getAllTournaments()
                                }
                            )
                        }

                        // History modal dialog
                        if (showHistoryDialog) {
                            HistoryDialog(
                                tournaments = savedTournaments,
                                language = currentLanguage,
                                onSelectTournament = { selected ->
                                    currentTournament = selected
                                    repository.setActiveTournament(selected.id)
                                },
                                onDeleteTournament = { idToDelete ->
                                    repository.deleteTournament(idToDelete)
                                    savedTournaments = repository.getAllTournaments()
                                    if (currentTournament?.id == idToDelete) {
                                        currentTournament = null
                                    }
                                },
                                onDismiss = { showHistoryDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
