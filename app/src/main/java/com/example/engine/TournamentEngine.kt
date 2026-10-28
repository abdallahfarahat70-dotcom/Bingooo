package com.example.engine

import com.example.model.Match
import com.example.model.MatchRule
import com.example.model.Participant
import com.example.model.Round
import com.example.model.SetScore
import com.example.model.Standing
import com.example.model.Tournament
import com.example.model.TournamentFormat
import com.example.model.TournamentType
import java.util.UUID

object TournamentEngine {

    /**
     * Finds the next power of 2 that is >= value, with minimum 2.
     */
    fun nextPowerOfTwo(value: Int): Int {
        if (value <= 2) return 2
        var power = 2
        while (power < value) {
            power *= 2
        }
        return power
    }

    /**
     * Generates a single-elimination knockout tournament bracket for ANY number of participants.
     * Automatically handles byes fairly distributed without Bye vs Bye matches.
     */
    fun generateKnockoutTournament(
        name: String,
        participants: List<Participant>,
        rules: MatchRule = MatchRule(),
        type: TournamentType = TournamentType.SINGLES,
        id: String = UUID.randomUUID().toString()
    ): Tournament {
        require(participants.size >= 2) { "At least 2 participants required" }

        val count = participants.size
        val bracketSize = nextPowerOfTwo(count) // e.g. 2, 4, 8, 16, 32
        val totalRounds = (kotlin.math.ln(bracketSize.toDouble()) / kotlin.math.ln(2.0)).toInt()
        val numByes = bracketSize - count

        // Distribute byes evenly across the first round matches
        val round0MatchCount = bracketSize / 2
        val byeMatchIndices = if (numByes > 0) {
            (0 until numByes).map { i -> (i * round0MatchCount) / numByes }.toSet()
        } else {
            emptySet()
        }

        // Assign participants to round 0
        var participantIndex = 0
        val round0Matches = ArrayList<Match>(round0MatchCount)

        // Pre-create match IDs for all rounds so nextMatchId links can be wired up
        val matchIdsByRound = (0 until totalRounds).map { r ->
            val matchCount = bracketSize / (1 shl (r + 1))
            (0 until matchCount).map { UUID.randomUUID().toString() }
        }

        // Build Round 0
        for (m in 0 until round0MatchCount) {
            val matchId = matchIdsByRound[0][m]
            val nextMatchId = if (totalRounds > 1) matchIdsByRound[1][m / 2] else null
            val nextMatchSlot = (m % 2) + 1

            if (byeMatchIndices.contains(m)) {
                // Single participant + Bye: automatically advances
                val player1 = participants[participantIndex++]
                round0Matches.add(
                    Match(
                        id = matchId,
                        roundIndex = 0,
                        matchIndexInRound = m,
                        participant1 = player1,
                        participant2 = null,
                        winner = player1,
                        isBye = true,
                        nextMatchId = nextMatchId,
                        nextMatchSlot = nextMatchSlot,
                        isCompleted = true
                    )
                )
            } else {
                // Two participants playing
                val player1 = participants[participantIndex++]
                val player2 = participants[participantIndex++]
                round0Matches.add(
                    Match(
                        id = matchId,
                        roundIndex = 0,
                        matchIndexInRound = m,
                        participant1 = player1,
                        participant2 = player2,
                        winner = null,
                        isBye = false,
                        nextMatchId = nextMatchId,
                        nextMatchSlot = nextMatchSlot,
                        isCompleted = false
                    )
                )
            }
        }

        // Build subsequent rounds (Round 1 to totalRounds - 1)
        val allRounds = ArrayList<Round>(totalRounds)
        allRounds.add(
            Round(
                roundIndex = 0,
                titleKey = getRoundTitleKey(0, totalRounds),
                matches = round0Matches
            )
        )

        for (r in 1 until totalRounds) {
            val matchCount = bracketSize / (1 shl (r + 1))
            val matches = ArrayList<Match>(matchCount)
            val nextRoundId = if (r + 1 < totalRounds) matchIdsByRound[r + 1] else null

            for (m in 0 until matchCount) {
                val matchId = matchIdsByRound[r][m]
                val nextMatchId = nextRoundId?.get(m / 2)
                val nextMatchSlot = (m % 2) + 1

                matches.add(
                    Match(
                        id = matchId,
                        roundIndex = r,
                        matchIndexInRound = m,
                        participant1 = null,
                        participant2 = null,
                        winner = null,
                        isBye = false,
                        nextMatchId = nextMatchId,
                        nextMatchSlot = nextMatchSlot,
                        isCompleted = false
                    )
                )
            }

            allRounds.add(
                Round(
                    roundIndex = r,
                    titleKey = getRoundTitleKey(r, totalRounds),
                    matches = matches
                )
            )
        }

        var tournament = Tournament(
            id = id,
            name = name,
            type = type,
            format = TournamentFormat.KNOCKOUT,
            rules = rules,
            participants = participants,
            rounds = allRounds
        )

        // Propagate the Round 0 byes to Round 1
        for (match in round0Matches) {
            if (match.isBye && match.winner != null && match.nextMatchId != null) {
                tournament = advanceWinner(tournament, match.id, match.winner, match.nextMatchId, match.nextMatchSlot)
            }
        }

        return tournament
    }

    /**
     * Generates a round-robin tournament (everyone plays everyone).
     * Uses the polygon / Berger rotation algorithm with balanced rounds.
     */
    fun generateRoundRobinTournament(
        name: String,
        participants: List<Participant>,
        rules: MatchRule = MatchRule(),
        type: TournamentType = TournamentType.SINGLES,
        id: String = UUID.randomUUID().toString()
    ): Tournament {
        require(participants.size >= 2) { "At least 2 participants required" }

        val pool = ArrayList<Participant?>(participants)
        val isOdd = pool.size % 2 != 0
        if (isOdd) {
            pool.add(null) // Ghost participant for bye
        }

        val n = pool.size
        val totalRounds = n - 1
        val matchesPerRound = n / 2
        val rounds = ArrayList<Round>(totalRounds)

        val rotatingList = ArrayList<Participant?>(pool)

        for (r in 0 until totalRounds) {
            val matches = ArrayList<Match>()
            for (m in 0 until matchesPerRound) {
                val p1 = rotatingList[m]
                val p2 = rotatingList[n - 1 - m]

                // If one of the players is ghost null, it's a bye round for the other
                if (p1 != null && p2 != null) {
                    matches.add(
                        Match(
                            id = UUID.randomUUID().toString(),
                            roundIndex = r,
                            matchIndexInRound = matches.size,
                            participant1 = p1,
                            participant2 = p2,
                            winner = null,
                            isBye = false,
                            isCompleted = false
                        )
                    )
                }
            }

            rounds.add(
                Round(
                    roundIndex = r,
                    titleKey = "round_n",
                    roundNumber = r + 1,
                    matches = matches
                )
            )

            // Rotate players: fix index 0, rotate indices 1 until n-1
            val last = rotatingList.removeAt(n - 1)
            rotatingList.add(1, last)
        }

        return Tournament(
            id = id,
            name = name,
            type = type,
            format = TournamentFormat.ROUND_ROBIN,
            rules = rules,
            participants = participants,
            rounds = rounds
        )
    }

    /**
     * Advances a winner to their next match slot in the tournament.
     */
    private fun advanceWinner(
        tournament: Tournament,
        completedMatchId: String,
        winner: Participant,
        nextMatchId: String,
        slot: Int
    ): Tournament {
        val updatedRounds = tournament.rounds.map { round ->
            val updatedMatches = round.matches.map { match ->
                if (match.id == nextMatchId) {
                    if (slot == 1) {
                        match.copy(participant1 = winner)
                    } else {
                        match.copy(participant2 = winner)
                    }
                } else {
                    match
                }
            }
            round.copy(matches = updatedMatches)
        }
        return tournament.copy(rounds = updatedRounds)
    }

    /**
     * Updates the score for a specific match, determines if match is completed,
     * and propagates the winner to the next round if applicable.
     */
    fun updateMatchScore(
        tournament: Tournament,
        matchId: String,
        sets: List<SetScore>
    ): Tournament {
        var updatedTournament = tournament
        var completedWinner: Participant? = null
        var completedNextMatchId: String? = null
        var completedNextSlot: Int = 1
        var isFinalMatch = false

        val updatedRounds = tournament.rounds.map { round ->
            val updatedMatches = round.matches.map { match ->
                if (match.id == matchId) {
                    val p1Sets = sets.count { it.winner(tournament.rules) == 1 }
                    val p2Sets = sets.count { it.winner(tournament.rules) == 2 }
                    val setsNeeded = tournament.rules.setsToWin

                    val winner = when {
                        p1Sets >= setsNeeded -> match.participant1
                        p2Sets >= setsNeeded -> match.participant2
                        else -> null
                    }
                    val isCompleted = winner != null

                    if (isCompleted && winner != null) {
                        completedWinner = winner
                        completedNextMatchId = match.nextMatchId
                        completedNextSlot = match.nextMatchSlot
                        if (match.roundIndex == tournament.rounds.size - 1) {
                            isFinalMatch = true
                        }
                    }

                    match.copy(
                        sets = sets,
                        winner = winner,
                        isCompleted = isCompleted
                    )
                } else {
                    match
                }
            }
            round.copy(matches = updatedMatches)
        }

        updatedTournament = updatedTournament.copy(rounds = updatedRounds)

        // If winner decided and there's a next match, propagate
        if (completedWinner != null && completedNextMatchId != null) {
            updatedTournament = advanceWinner(
                updatedTournament,
                matchId,
                completedWinner!!,
                completedNextMatchId!!,
                completedNextSlot
            )
        }

        // If this is the final knockout match and it finished
        if (isFinalMatch && completedWinner != null) {
            val finalRound = updatedTournament.rounds.lastOrNull()
            val finalMatch = finalRound?.matches?.firstOrNull()
            val runnerUp = if (finalMatch?.participant1?.id == completedWinner?.id) {
                finalMatch?.participant2
            } else {
                finalMatch?.participant1
            }

            // Find 3rd place from semi-final losers if available
            val thirdPlace = if (updatedTournament.rounds.size >= 2) {
                val semiRound = updatedTournament.rounds[updatedTournament.rounds.size - 2]
                val semiLosers = semiRound.matches.mapNotNull { m ->
                    if (m.winner != null) {
                        if (m.participant1?.id == m.winner.id) m.participant2 else m.participant1
                    } else null
                }
                semiLosers.firstOrNull()
            } else null

            updatedTournament = updatedTournament.copy(
                isFinished = true,
                winner = completedWinner,
                runnerUp = runnerUp,
                thirdPlace = thirdPlace
            )
        }

        // Check if all round-robin matches are completed
        if (tournament.format == TournamentFormat.ROUND_ROBIN) {
            val allMatches = updatedTournament.rounds.flatMap { it.matches }
            val allCompleted = allMatches.isNotEmpty() && allMatches.all { it.isCompleted }
            if (allCompleted) {
                val standings = calculateStandings(updatedTournament)
                val winner = standings.firstOrNull()?.participant
                val runnerUp = standings.getOrNull(1)?.participant
                val thirdPlace = standings.getOrNull(2)?.participant
                updatedTournament = updatedTournament.copy(
                    isFinished = true,
                    winner = winner,
                    runnerUp = runnerUp,
                    thirdPlace = thirdPlace
                )
            }
        }

        return updatedTournament
    }

    /**
     * Resets/clears a match result (Undo).
     */
    fun resetMatchScore(
        tournament: Tournament,
        matchId: String
    ): Tournament {
        val targetMatch = tournament.rounds.flatMap { it.matches }.firstOrNull { it.id == matchId }
            ?: return tournament

        // Clear next match slot if this match had advanced a winner
        var updatedRounds = tournament.rounds.map { round ->
            val updatedMatches = round.matches.map { match ->
                if (match.id == matchId) {
                    match.copy(
                        sets = emptyList(),
                        winner = null,
                        isCompleted = false
                    )
                } else if (targetMatch.nextMatchId != null && match.id == targetMatch.nextMatchId) {
                    if (targetMatch.nextMatchSlot == 1) {
                        match.copy(participant1 = null, winner = null, isCompleted = false)
                    } else {
                        match.copy(participant2 = null, winner = null, isCompleted = false)
                    }
                } else {
                    match
                }
            }
            round.copy(matches = updatedMatches)
        }

        return tournament.copy(
            rounds = updatedRounds,
            isFinished = false,
            winner = null,
            runnerUp = null,
            thirdPlace = null
        )
    }

    /**
     * Calculates the standings table for a Round Robin tournament.
     * Points: 2 for win, 0 for loss.
     * Tie-breakers: total points, then set difference, then point difference.
     */
    fun calculateStandings(tournament: Tournament): List<Standing> {
        val statsMap = tournament.participants.associateWith {
            Standing(participant = it)
        }.toMutableMap()

        val allMatches = tournament.rounds.flatMap { it.matches }

        for (match in allMatches) {
            if (!match.isCompleted || match.isBye) continue
            val p1 = match.participant1 ?: continue
            val p2 = match.participant2 ?: continue

            val p1Stats = statsMap[p1] ?: Standing(p1)
            val p2Stats = statsMap[p2] ?: Standing(p2)

            val p1SetsWon = match.player1SetsWon(tournament.rules)
            val p2SetsWon = match.player2SetsWon(tournament.rules)

            var p1Points = 0
            var p2Points = 0
            for (set in match.sets) {
                p1Points += set.player1Score
                p2Points += set.player2Score
            }

            val p1Won = match.winner?.id == p1.id
            val p2Won = match.winner?.id == p2.id

            statsMap[p1] = p1Stats.copy(
                played = p1Stats.played + 1,
                won = p1Stats.won + if (p1Won) 1 else 0,
                lost = p1Stats.lost + if (p2Won) 1 else 0,
                setsWon = p1Stats.setsWon + p1SetsWon,
                setsLost = p1Stats.setsLost + p2SetsWon,
                pointsWon = p1Stats.pointsWon + p1Points,
                pointsLost = p1Stats.pointsLost + p2Points,
                points = p1Stats.points + (if (p1Won) 2 else 0)
            )

            statsMap[p2] = p2Stats.copy(
                played = p2Stats.played + 1,
                won = p2Stats.won + if (p2Won) 1 else 0,
                lost = p2Stats.lost + if (p1Won) 1 else 0,
                setsWon = p2Stats.setsWon + p2SetsWon,
                setsLost = p2Stats.setsLost + p1SetsWon,
                pointsWon = p2Stats.pointsWon + p2Points,
                pointsLost = p2Stats.pointsLost + p1Points,
                points = p2Stats.points + (if (p2Won) 2 else 0)
            )
        }

        return statsMap.values.sortedWith(
            compareByDescending<Standing> { it.points }
                .thenByDescending { it.setDiff }
                .thenByDescending { it.pointDiff }
                .thenByDescending { it.setsWon }
        )
    }

    /**
     * Randomly pairs a list of player names into doubles teams.
     * Returns: (List of paired participants, odd player name left over if count was odd)
     */
    fun pairDoublesRandomly(names: List<String>): Pair<List<Participant>, String?> {
        val shuffled = names.shuffled()
        val pairs = ArrayList<Participant>()
        val count = shuffled.size
        val isOdd = count % 2 != 0
        val pairCount = count / 2

        for (i in 0 until pairCount) {
            val p1 = shuffled[i * 2]
            val p2 = shuffled[i * 2 + 1]
            pairs.add(
                Participant(
                    id = UUID.randomUUID().toString(),
                    name = p1,
                    partnerName = p2
                )
            )
        }

        val leftover = if (isOdd) shuffled.last() else null
        return Pair(pairs, leftover)
    }

    fun getRoundTitleKey(roundIndex: Int, totalRounds: Int): String {
        val fromEnd = totalRounds - 1 - roundIndex
        return when (fromEnd) {
            0 -> "final"
            1 -> "semi"
            2 -> "quarter"
            3 -> "round_of_16"
            4 -> "round_of_32"
            else -> "preliminary"
        }
    }
}
