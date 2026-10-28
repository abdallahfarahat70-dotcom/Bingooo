package com.example

import com.example.engine.TournamentEngine
import com.example.model.MatchRule
import com.example.model.Participant
import com.example.model.SetScore
import com.example.model.TournamentFormat
import com.example.model.TournamentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentEngineTest {

    private fun createPlayers(count: Int): List<Participant> {
        return (1..count).map { i ->
            Participant(id = "p_$i", name = "Player $i")
        }
    }

    @Test
    fun testKnockout2Players() {
        val players = createPlayers(2)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 2", players)
        assertEquals(1, tournament.rounds.size)
        val finalRound = tournament.rounds[0]
        assertEquals(1, finalRound.matches.size)
        val match = finalRound.matches[0]
        assertEquals(players[0], match.participant1)
        assertEquals(players[1], match.participant2)
        assertEquals(false, match.isBye)
    }

    @Test
    fun testKnockout3Players() {
        val players = createPlayers(3)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 3", players)
        // Bracket size 4 -> 2 rounds
        assertEquals(2, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(2, round0.matches.size)

        // Exactly 1 bye (4 - 3 = 1)
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(1, byeMatches.size)
        val byeMatch = byeMatches[0]
        assertNotNull(byeMatch.winner)

        // The bye winner must have advanced to Round 1 (Final)
        val round1 = tournament.rounds[1]
        assertEquals(1, round1.matches.size)
        val finalMatch = round1.matches[0]
        assertTrue(finalMatch.participant1 != null || finalMatch.participant2 != null)
    }

    @Test
    fun testKnockout5Players() {
        val players = createPlayers(5)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 5", players)
        // Bracket size 8 -> 3 rounds
        assertEquals(3, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(4, round0.matches.size)

        // Exactly 3 byes (8 - 5 = 3)
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(3, byeMatches.size)
        val regularMatches = round0.matches.filter { !it.isBye }
        assertEquals(1, regularMatches.size)
    }

    @Test
    fun testKnockout7Players() {
        val players = createPlayers(7)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 7", players)
        // Bracket size 8 -> 3 rounds
        assertEquals(3, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(4, round0.matches.size)

        // Exactly 1 bye (8 - 7 = 1)
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(1, byeMatches.size)
        val regularMatches = round0.matches.filter { !it.isBye }
        assertEquals(3, regularMatches.size)
    }

    @Test
    fun testKnockout8Players() {
        val players = createPlayers(8)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 8", players)
        // Bracket size 8 -> 3 rounds
        assertEquals(3, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(4, round0.matches.size)

        // 0 byes, perfect power of 2
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(0, byeMatches.size)
        assertEquals(4, round0.matches.filter { !it.isBye }.size)
    }

    @Test
    fun testKnockout13Players() {
        val players = createPlayers(13)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 13", players)
        // Bracket size 16 -> 4 rounds
        assertEquals(4, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(8, round0.matches.size)

        // Exactly 3 byes (16 - 13 = 3)
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(3, byeMatches.size)
        assertEquals(5, round0.matches.filter { !it.isBye }.size)
    }

    @Test
    fun testKnockout16Players() {
        val players = createPlayers(16)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 16", players)
        // Bracket size 16 -> 4 rounds
        assertEquals(4, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(8, round0.matches.size)

        // 0 byes
        assertEquals(0, round0.matches.filter { it.isBye }.size)
        assertEquals(8, round0.matches.filter { !it.isBye }.size)
    }

    @Test
    fun testKnockout25Players() {
        val players = createPlayers(25)
        val tournament = TournamentEngine.generateKnockoutTournament("Tournament 25", players)
        // Bracket size 32 -> 5 rounds
        assertEquals(5, tournament.rounds.size)
        val round0 = tournament.rounds[0]
        assertEquals(16, round0.matches.size)

        // Exactly 7 byes (32 - 25 = 7)
        val byeMatches = round0.matches.filter { it.isBye }
        assertEquals(7, byeMatches.size)
        assertEquals(9, round0.matches.filter { !it.isBye }.size)
    }

    @Test
    fun testDoublesWithOddPlayers() {
        val names = listOf("Ali", "Omar", "Hassan", "Ziad", "Karim") // 5 players (odd)
        val (pairs, leftover) = TournamentEngine.pairDoublesRandomly(names)
        assertEquals(2, pairs.size) // 2 teams
        assertNotNull(leftover) // Karim or another left over
        assertTrue(names.contains(leftover))

        // When resolved by adding a player
        val resolvedNames = names + "Tarek" // 6 players (even)
        val (evenPairs, evenLeftover) = TournamentEngine.pairDoublesRandomly(resolvedNames)
        assertEquals(3, evenPairs.size)
        assertEquals(null, evenLeftover)
    }

    @Test
    fun testRoundRobinAndStandings() {
        val players = createPlayers(4)
        val rules = MatchRule(bestOf = 3, pointsToWin = 11, pointsMargin = 2)
        var tournament = TournamentEngine.generateRoundRobinTournament(
            name = "League",
            participants = players,
            rules = rules,
            type = TournamentType.SINGLES
        )

        assertEquals(3, tournament.rounds.size) // 4 players -> 3 rounds
        for (round in tournament.rounds) {
            assertEquals(2, round.matches.size) // 2 matches per round
        }

        // Simulate match 1 in round 0: p1 beats p4 (2 sets to 0: 11-9, 11-8)
        val match = tournament.rounds[0].matches[0]
        val sets = listOf(
            SetScore(player1Score = 11, player2Score = 9),
            SetScore(player1Score = 11, player2Score = 8)
        )
        tournament = TournamentEngine.updateMatchScore(tournament, match.id, sets)

        val standings = TournamentEngine.calculateStandings(tournament)
        assertEquals(4, standings.size)
        // Winner should have 2 points and 2 sets won
        val top = standings[0]
        assertEquals(match.participant1?.id, top.participant.id)
        assertEquals(2, top.points)
        assertEquals(2, top.setsWon)
        assertEquals(0, top.setsLost)
    }
}
