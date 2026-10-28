package com.example.model

enum class TournamentType {
    SINGLES,
    DOUBLES
}

enum class TournamentFormat {
    KNOCKOUT,
    ROUND_ROBIN
}

enum class DoublesEntryMode {
    MANUAL_PAIRS,
    RANDOM_DRAW
}

data class MatchRule(
    val bestOf: Int = 3,           // Best of 1, 3, or 5 sets
    val pointsToWin: Int = 11,     // Typically 11 points
    val pointsMargin: Int = 2      // Typically must lead by 2 points
) {
    val setsToWin: Int get() = (bestOf / 2) + 1
}

data class Participant(
    val id: String,
    val name: String,
    val partnerName: String? = null
) {
    val displayName: String
        get() = if (partnerName.isNullOrBlank()) name else "$name & $partnerName"
}

data class SetScore(
    val player1Score: Int = 0,
    val player2Score: Int = 0
) {
    fun isFinished(rule: MatchRule): Boolean {
        val maxScore = maxOf(player1Score, player2Score)
        val diff = kotlin.math.abs(player1Score - player2Score)
        return maxScore >= rule.pointsToWin && diff >= rule.pointsMargin
    }

    fun winner(rule: MatchRule): Int? {
        if (!isFinished(rule)) return null
        return if (player1Score > player2Score) 1 else 2
    }
}

data class Match(
    val id: String,
    val roundIndex: Int,
    val matchIndexInRound: Int,
    val participant1: Participant? = null,
    val participant2: Participant? = null,
    val sets: List<SetScore> = emptyList(),
    val winner: Participant? = null,
    val isBye: Boolean = false,
    val nextMatchId: String? = null,
    val nextMatchSlot: Int = 1, // 1 for participant1 slot, 2 for participant2 slot
    val isCompleted: Boolean = false
) {
    fun player1SetsWon(rule: MatchRule): Int =
        sets.count { it.winner(rule) == 1 }

    fun player2SetsWon(rule: MatchRule): Int =
        sets.count { it.winner(rule) == 2 }

    fun determineWinner(rule: MatchRule): Participant? {
        if (isBye) return participant1
        val p1Sets = player1SetsWon(rule)
        val p2Sets = player2SetsWon(rule)
        val setsNeeded = rule.setsToWin
        return when {
            p1Sets >= setsNeeded -> participant1
            p2Sets >= setsNeeded -> participant2
            else -> null
        }
    }
}

data class Round(
    val roundIndex: Int,
    val titleKey: String, // e.g., "final", "semi", "quarter", "round_of_16", "preliminary", "round_n"
    val roundNumber: Int = roundIndex + 1,
    val matches: List<Match>
)

data class Standing(
    val participant: Participant,
    val played: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val setsWon: Int = 0,
    val setsLost: Int = 0,
    val pointsWon: Int = 0,
    val pointsLost: Int = 0,
    val points: Int = 0
) {
    val setDiff: Int get() = setsWon - setsLost
    val pointDiff: Int get() = pointsWon - pointsLost
}

data class Tournament(
    val id: String,
    val name: String,
    val type: TournamentType = TournamentType.SINGLES,
    val format: TournamentFormat = TournamentFormat.KNOCKOUT,
    val rules: MatchRule = MatchRule(),
    val participants: List<Participant> = emptyList(),
    val rounds: List<Round> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val isFinished: Boolean = false,
    val winner: Participant? = null,
    val runnerUp: Participant? = null,
    val thirdPlace: Participant? = null
)
