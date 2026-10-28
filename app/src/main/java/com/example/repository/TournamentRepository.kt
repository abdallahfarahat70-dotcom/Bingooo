package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Match
import com.example.model.MatchRule
import com.example.model.Participant
import com.example.model.Round
import com.example.model.SetScore
import com.example.model.Tournament
import com.example.model.TournamentFormat
import com.example.model.TournamentType
import org.json.JSONArray
import org.json.JSONObject

class TournamentRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bingbong_tournaments", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_ID = "active_tournament_id"
        private const val KEY_TOURNAMENT_IDS = "saved_tournament_ids"
        private const val PREFIX_TOURNAMENT = "tournament_"
    }

    fun getActiveTournament(): Tournament? {
        val activeId = prefs.getString(KEY_ACTIVE_ID, null) ?: return null
        return getTournament(activeId)
    }

    fun setActiveTournament(id: String?) {
        prefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun getAllTournaments(): List<Tournament> {
        val idsJson = prefs.getString(KEY_TOURNAMENT_IDS, "[]") ?: "[]"
        val jsonArray = JSONArray(idsJson)
        val list = mutableListOf<Tournament>()
        for (i in 0 until jsonArray.length()) {
            val id = jsonArray.getString(i)
            getTournament(id)?.let { list.add(it) }
        }
        return list.sortedByDescending { it.createdAt }
    }

    fun getTournament(id: String): Tournament? {
        val raw = prefs.getString(PREFIX_TOURNAMENT + id, null) ?: return null
        return try {
            deserializeTournament(JSONObject(raw))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveTournament(tournament: Tournament) {
        val json = serializeTournament(tournament)
        prefs.edit().putString(PREFIX_TOURNAMENT + tournament.id, json.toString()).apply()

        // Update list of IDs
        val idsJson = prefs.getString(KEY_TOURNAMENT_IDS, "[]") ?: "[]"
        val jsonArray = JSONArray(idsJson)
        val existingIds = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            existingIds.add(jsonArray.getString(i))
        }
        if (!existingIds.contains(tournament.id)) {
            existingIds.add(0, tournament.id)
            val updatedArray = JSONArray(existingIds)
            prefs.edit().putString(KEY_TOURNAMENT_IDS, updatedArray.toString()).apply()
        }
        setActiveTournament(tournament.id)
    }

    fun deleteTournament(id: String) {
        prefs.edit().remove(PREFIX_TOURNAMENT + id).apply()
        val idsJson = prefs.getString(KEY_TOURNAMENT_IDS, "[]") ?: "[]"
        val jsonArray = JSONArray(idsJson)
        val existingIds = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val currId = jsonArray.getString(i)
            if (currId != id) existingIds.add(currId)
        }
        prefs.edit().putString(KEY_TOURNAMENT_IDS, JSONArray(existingIds).toString()).apply()

        if (prefs.getString(KEY_ACTIVE_ID, null) == id) {
            setActiveTournament(existingIds.firstOrNull())
        }
    }

    private fun serializeTournament(t: Tournament): JSONObject {
        val obj = JSONObject()
        obj.put("id", t.id)
        obj.put("name", t.name)
        obj.put("type", t.type.name)
        obj.put("format", t.format.name)
        obj.put("createdAt", t.createdAt)
        obj.put("isFinished", t.isFinished)

        // Rules
        val rulesObj = JSONObject()
        rulesObj.put("bestOf", t.rules.bestOf)
        rulesObj.put("pointsToWin", t.rules.pointsToWin)
        rulesObj.put("pointsMargin", t.rules.pointsMargin)
        obj.put("rules", rulesObj)

        // Participants
        val partsArr = JSONArray()
        for (p in t.participants) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("name", p.name)
            p.partnerName?.let { pObj.put("partnerName", it) }
            partsArr.put(pObj)
        }
        obj.put("participants", partsArr)

        // Rounds & matches
        val roundsArr = JSONArray()
        for (r in t.rounds) {
            val rObj = JSONObject()
            rObj.put("roundIndex", r.roundIndex)
            rObj.put("titleKey", r.titleKey)
            rObj.put("roundNumber", r.roundNumber)

            val matchesArr = JSONArray()
            for (m in r.matches) {
                val mObj = JSONObject()
                mObj.put("id", m.id)
                mObj.put("roundIndex", m.roundIndex)
                mObj.put("matchIndexInRound", m.matchIndexInRound)
                m.participant1?.let { p ->
                    mObj.put("p1_id", p.id)
                    mObj.put("p1_name", p.name)
                    p.partnerName?.let { mObj.put("p1_partner", it) }
                }
                m.participant2?.let { p ->
                    mObj.put("p2_id", p.id)
                    mObj.put("p2_name", p.name)
                    p.partnerName?.let { mObj.put("p2_partner", it) }
                }
                m.winner?.let { p ->
                    mObj.put("w_id", p.id)
                    mObj.put("w_name", p.name)
                    p.partnerName?.let { mObj.put("w_partner", it) }
                }
                mObj.put("isBye", m.isBye)
                mObj.put("isCompleted", m.isCompleted)
                m.nextMatchId?.let { mObj.put("nextMatchId", it) }
                mObj.put("nextMatchSlot", m.nextMatchSlot)

                // Sets
                val setsArr = JSONArray()
                for (s in m.sets) {
                    val sObj = JSONObject()
                    sObj.put("p1", s.player1Score)
                    sObj.put("p2", s.player2Score)
                    setsArr.put(sObj)
                }
                mObj.put("sets", setsArr)
                matchesArr.put(mObj)
            }
            rObj.put("matches", matchesArr)
            roundsArr.put(rObj)
        }
        obj.put("rounds", roundsArr)

        t.winner?.let { p ->
            val wObj = JSONObject()
            wObj.put("id", p.id)
            wObj.put("name", p.name)
            p.partnerName?.let { wObj.put("partnerName", it) }
            obj.put("winner", wObj)
        }
        t.runnerUp?.let { p ->
            val rObj = JSONObject()
            rObj.put("id", p.id)
            rObj.put("name", p.name)
            p.partnerName?.let { rObj.put("partnerName", it) }
            obj.put("runnerUp", rObj)
        }
        t.thirdPlace?.let { p ->
            val tObj = JSONObject()
            tObj.put("id", p.id)
            tObj.put("name", p.name)
            p.partnerName?.let { tObj.put("partnerName", it) }
            obj.put("thirdPlace", tObj)
        }

        return obj
    }

    private fun deserializeTournament(obj: JSONObject): Tournament {
        val id = obj.getString("id")
        val name = obj.getString("name")
        val type = TournamentType.valueOf(obj.optString("type", TournamentType.SINGLES.name))
        val format = TournamentFormat.valueOf(obj.optString("format", TournamentFormat.KNOCKOUT.name))
        val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        val isFinished = obj.optBoolean("isFinished", false)

        val rulesObj = obj.optJSONObject("rules")
        val rules = if (rulesObj != null) {
            MatchRule(
                bestOf = rulesObj.optInt("bestOf", 3),
                pointsToWin = rulesObj.optInt("pointsToWin", 11),
                pointsMargin = rulesObj.optInt("pointsMargin", 2)
            )
        } else {
            MatchRule()
        }

        val partsArr = obj.optJSONArray("participants") ?: JSONArray()
        val participants = mutableListOf<Participant>()
        for (i in 0 until partsArr.length()) {
            val pObj = partsArr.getJSONObject(i)
            participants.add(
                Participant(
                    id = pObj.getString("id"),
                    name = pObj.getString("name"),
                    partnerName = if (pObj.has("partnerName")) pObj.getString("partnerName") else null
                )
            )
        }

        val roundsArr = obj.optJSONArray("rounds") ?: JSONArray()
        val rounds = mutableListOf<Round>()
        for (r in 0 until roundsArr.length()) {
            val rObj = roundsArr.getJSONObject(r)
            val rIndex = rObj.getInt("roundIndex")
            val titleKey = rObj.getString("titleKey")
            val roundNum = rObj.optInt("roundNumber", rIndex + 1)

            val matchesArr = rObj.optJSONArray("matches") ?: JSONArray()
            val matches = mutableListOf<Match>()
            for (m in 0 until matchesArr.length()) {
                val mObj = matchesArr.getJSONObject(m)
                val mId = mObj.getString("id")
                val matchIdx = mObj.optInt("matchIndexInRound", m)

                val p1 = if (mObj.has("p1_id")) {
                    Participant(
                        id = mObj.getString("p1_id"),
                        name = mObj.getString("p1_name"),
                        partnerName = if (mObj.has("p1_partner")) mObj.getString("p1_partner") else null
                    )
                } else null

                val p2 = if (mObj.has("p2_id")) {
                    Participant(
                        id = mObj.getString("p2_id"),
                        name = mObj.getString("p2_name"),
                        partnerName = if (mObj.has("p2_partner")) mObj.getString("p2_partner") else null
                    )
                } else null

                val winner = if (mObj.has("w_id")) {
                    Participant(
                        id = mObj.getString("w_id"),
                        name = mObj.getString("w_name"),
                        partnerName = if (mObj.has("w_partner")) mObj.getString("w_partner") else null
                    )
                } else null

                val isBye = mObj.optBoolean("isBye", false)
                val isCompleted = mObj.optBoolean("isCompleted", false)
                val nextMatchId = if (mObj.has("nextMatchId")) mObj.getString("nextMatchId") else null
                val nextMatchSlot = mObj.optInt("nextMatchSlot", 1)

                val setsArr = mObj.optJSONArray("sets") ?: JSONArray()
                val sets = mutableListOf<SetScore>()
                for (s in 0 until setsArr.length()) {
                    val sObj = setsArr.getJSONObject(s)
                    sets.add(
                        SetScore(
                            player1Score = sObj.optInt("p1", 0),
                            player2Score = sObj.optInt("p2", 0)
                        )
                    )
                }

                matches.add(
                    Match(
                        id = mId,
                        roundIndex = rIndex,
                        matchIndexInRound = matchIdx,
                        participant1 = p1,
                        participant2 = p2,
                        sets = sets,
                        winner = winner,
                        isBye = isBye,
                        nextMatchId = nextMatchId,
                        nextMatchSlot = nextMatchSlot,
                        isCompleted = isCompleted
                    )
                )
            }
            rounds.add(Round(rIndex, titleKey, roundNum, matches))
        }

        val winner = if (obj.has("winner")) {
            val wObj = obj.getJSONObject("winner")
            Participant(
                id = wObj.getString("id"),
                name = wObj.getString("name"),
                partnerName = if (wObj.has("partnerName")) wObj.getString("partnerName") else null
            )
        } else null

        val runnerUp = if (obj.has("runnerUp")) {
            val rObj = obj.getJSONObject("runnerUp")
            Participant(
                id = rObj.getString("id"),
                name = rObj.getString("name"),
                partnerName = if (rObj.has("partnerName")) rObj.getString("partnerName") else null
            )
        } else null

        val thirdPlace = if (obj.has("thirdPlace")) {
            val tObj = obj.getJSONObject("thirdPlace")
            Participant(
                id = tObj.getString("id"),
                name = tObj.getString("name"),
                partnerName = if (tObj.has("partnerName")) tObj.getString("partnerName") else null
            )
        } else null

        return Tournament(
            id = id,
            name = name,
            type = type,
            format = format,
            rules = rules,
            participants = participants,
            rounds = rounds,
            createdAt = createdAt,
            isFinished = isFinished,
            winner = winner,
            runnerUp = runnerUp,
            thirdPlace = thirdPlace
        )
    }
}
