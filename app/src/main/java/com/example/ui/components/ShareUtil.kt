package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.localization.AppLanguage
import com.example.localization.AppStrings
import com.example.model.Tournament
import com.example.model.TournamentFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareUtil {

    fun generateReportText(tournament: Tournament, language: AppLanguage): String {
        // Date formatting without year to comply strictly with year-omission rule
        val dateStr = try {
            val sdf = SimpleDateFormat("d MMMM - HH:mm", if (language == AppLanguage.ARABIC) Locale("ar") else Locale.ENGLISH)
            sdf.format(Date(tournament.createdAt))
        } catch (e: Exception) {
            ""
        }

        val sb = StringBuilder()
        sb.append("🏓 ").append(tournament.name).append("\n")
        if (dateStr.isNotBlank()) {
            sb.append("📅 ").append(dateStr).append("\n")
        }
        sb.append("⚡ ").append(
            if (tournament.format == TournamentFormat.KNOCKOUT) AppStrings.get("knockout", language)
            else AppStrings.get("round_robin", language)
        ).append(" (").append(
            if (tournament.type == com.example.model.TournamentType.SINGLES) AppStrings.get("singles", language)
            else AppStrings.get("doubles", language)
        ).append(")\n\n")

        if (tournament.winner != null) {
            sb.append("🏆 ").append(AppStrings.get("champion_title", language)).append("\n")
            sb.append("🥇 1st: ").append(tournament.winner.displayName).append("\n")
            if (tournament.runnerUp != null) {
                sb.append("🥈 2nd: ").append(tournament.runnerUp.displayName).append("\n")
            }
            if (tournament.thirdPlace != null) {
                sb.append("🥉 3rd: ").append(tournament.thirdPlace.displayName).append("\n")
            }
            sb.append("\n")
        }

        sb.append("📋 ").append(AppStrings.get("rounds_tab", language)).append(":\n")
        for (round in tournament.rounds) {
            val roundTitle = when (round.titleKey) {
                "round_n" -> AppStrings.get("round_n", language, round.roundNumber)
                else -> AppStrings.get(round.titleKey, language)
            }
            sb.append("\n▶ ").append(roundTitle).append(":\n")
            for (match in round.matches) {
                val p1Name = match.participant1?.displayName ?: "—"
                val p2Name = if (match.isBye) AppStrings.get("status_bye", language) else (match.participant2?.displayName ?: "—")
                val p1Sets = match.player1SetsWon(tournament.rules)
                val p2Sets = match.player2SetsWon(tournament.rules)

                if (match.isCompleted) {
                    val winnerMarker = if (match.winner?.id == match.participant1?.id) "★" else ""
                    val winnerMarker2 = if (match.winner?.id == match.participant2?.id) "★" else ""
                    sb.append("  • $p1Name $winnerMarker ($p1Sets) vs ($p2Sets) $winnerMarker2 $p2Name\n")
                } else {
                    sb.append("  • $p1Name vs $p2Name\n")
                }
            }
        }

        sb.append("\n-- Bing bong --")
        return sb.toString()
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(context: Context, text: String, toastMsg: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bing bong Tournament", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }
}
