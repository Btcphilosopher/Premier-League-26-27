package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

object MatchSimulationEngine {

    private var simulationJob: Job? = null
    private val _isSimulating = MutableStateFlow(true)
    val isSimulating: StateFlow<Boolean> = _isSimulating

    private val _simSpeedSeconds = MutableStateFlow(5L) // How many seconds between events
    val simSpeedSeconds: StateFlow<Long> = _simSpeedSeconds

    fun setSimulating(enabled: Boolean) {
        _isSimulating.value = enabled
    }

    fun setSimSpeed(seconds: Long) {
        _simSpeedSeconds.value = seconds
    }

    fun startSimulation(db: AppDatabase, scope: CoroutineScope, context: Context) {
        if (simulationJob != null) return

        simulationJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                if (_isSimulating.value) {
                    try {
                        simulateTick(db, context)
                    } catch (e: Exception) {
                        Log.e("MatchSimulationEngine", "Error during simulation tick", e)
                    }
                }
                delay(_simSpeedSeconds.value * 1000L)
            }
        }
    }

    private suspend fun simulateTick(db: AppDatabase, context: Context) {
        val fixtureDao = db.matchFixtureDao()
        val eventDao = db.matchEventDao()
        val teamDao = db.teamDao()
        val playerDao = db.playerDao()
        val notificationDao = db.notificationDao()

        // 1. Get all matches
        // To find live matches, we query once
        val liveMatches = db.matchFixtureDao().getMatchByIdFlow(201) // We know 201 and 202 are live
        // Let's grab them directly
        val matchIds = listOf(201, 202)

        for (matchId in matchIds) {
            val match = fixtureDao.getMatchById(matchId) ?: continue
            if (match.status != "LIVE") continue

            // Simulate minutes ticking up. Standard games have 90 mins.
            // Let's assume matches are between 45 and 90.
            val currentEvents = eventDao.getEventsByMatchList(matchId)
            val lastEventMinute = currentEvents.firstOrNull()?.minute ?: 45
            val nextMinute = lastEventMinute + Random.nextInt(1, 4)

            if (nextMinute > 90) {
                // End match
                val updatedMatch = match.copy(
                    status = "FT"
                )
                fixtureDao.updateFixture(updatedMatch)

                val ftEvent = MatchEventEntity(
                    matchId = matchId,
                    minute = 90,
                    type = "VAR",
                    teamId = 0,
                    playerName = "",
                    detail = "Full Time! The referee blows the final whistle."
                )
                eventDao.insertEvent(ftEvent)

                notificationDao.insertNotification(
                    NotificationEntity(
                        title = "Full Time: ${getTeamName(db, match.homeTeamId)} ${match.homeScore}-${match.awayScore} ${getTeamName(db, match.awayTeamId)}",
                        message = "The match at ${match.venue} has finished.",
                        timestamp = System.currentTimeMillis(),
                        type = "INFO"
                    )
                )
                continue
            }

            // Let's update match statistics
            val isHomeAttack = Random.nextBoolean()
            val possessionDelta = Random.nextInt(-3, 4)
            val newPossessionHome = (match.possessionHome + possessionDelta).coerceIn(30, 70)
            val newPossessionAway = 100 - newPossessionHome

            val shotsHomeDelta = if (isHomeAttack && Random.nextFloat() > 0.6) 1 else 0
            val shotsAwayDelta = if (!isHomeAttack && Random.nextFloat() > 0.6) 1 else 0
            
            val shotsOnTargetHomeDelta = if (shotsHomeDelta > 0 && Random.nextBoolean()) 1 else 0
            val shotsOnTargetAwayDelta = if (shotsAwayDelta > 0 && Random.nextBoolean()) 1 else 0

            val xgHomeDelta = if (shotsHomeDelta > 0) Random.nextDouble(0.05, 0.25) else 0.0
            val xgAwayDelta = if (shotsAwayDelta > 0) Random.nextDouble(0.05, 0.25) else 0.0

            val passesHomeDelta = Random.nextInt(5, 15)
            val passesAwayDelta = Random.nextInt(5, 15)

            // Randomly generate events
            val eventChance = Random.nextFloat()
            var newHomeScore = match.homeScore
            var newAwayScore = match.awayScore

            if (eventChance > 0.70) {
                // Generate a real event!
                val eventType = chooseRandomEventType()
                val actingTeamId = if (Random.nextBoolean()) match.homeTeamId else match.awayTeamId
                val actingTeamCode = getTeamCode(db, actingTeamId)
                val opposingTeamCode = getTeamCode(db, if (actingTeamId == match.homeTeamId) match.awayTeamId else match.homeTeamId)
                val playersList = playerDao.getPlayersByTeamList(actingTeamId)
                val player = playersList.randomOrNull() ?: PlayerEntity(teamId = actingTeamId, name = "Unknown Player", position = "MID", goals = 0, assists = 0, minutesPlayed = 0, expectedGoals = 0.0, expectedAssists = 0.0, passAccuracy = 0.0, yellowCards = 0, redCards = 0, injuryStatus = "Fit", injuryDetails = "")

                when (eventType) {
                    "GOAL" -> {
                        val isPenalty = Random.nextFloat() > 0.85
                        var detail = if (isPenalty) "Penalty cleanly converted!" else "Sensational strike into the corner."
                        val assister = playersList.filter { it.id != player.id }.randomOrNull()
                        if (assister != null && !isPenalty) {
                            detail += " Assist: ${assister.name}"
                        }

                        if (actingTeamId == match.homeTeamId) {
                            newHomeScore++
                        } else {
                            newAwayScore++
                        }

                        // Save Event
                        eventDao.insertEvent(
                            MatchEventEntity(
                                matchId = matchId,
                                minute = nextMinute,
                                type = "GOAL",
                                teamId = actingTeamId,
                                playerName = player.name,
                                detail = detail
                            )
                        )

                        // Update player database goals
                        playerDao.updatePlayer(player.copy(goals = player.goals + 1))

                        // Send Notification
                        notificationDao.insertNotification(
                            NotificationEntity(
                                title = "GOAL! ${getTeamName(db, match.homeTeamId)} $newHomeScore-$newAwayScore ${getTeamName(db, match.awayTeamId)}",
                                message = "${player.name} ($nextMinute') scores for $actingTeamCode! $detail",
                                timestamp = System.currentTimeMillis(),
                                type = "GOAL"
                            )
                        )
                    }
                    "YELLOW_CARD" -> {
                        eventDao.insertEvent(
                            MatchEventEntity(
                                matchId = matchId,
                                minute = nextMinute,
                                type = "YELLOW_CARD",
                                teamId = actingTeamId,
                                playerName = player.name,
                                detail = "Booked for a late tactical challenge."
                            )
                        )
                        playerDao.updatePlayer(player.copy(yellowCards = player.yellowCards + 1))
                    }
                    "RED_CARD" -> {
                        eventDao.insertEvent(
                            MatchEventEntity(
                                matchId = matchId,
                                minute = nextMinute,
                                type = "RED_CARD",
                                teamId = actingTeamId,
                                playerName = player.name,
                                detail = "Sent off! Straight red card for a dangerous high-tackle."
                            )
                        )
                        playerDao.updatePlayer(player.copy(redCards = player.redCards + 1))

                        notificationDao.insertNotification(
                            NotificationEntity(
                                title = "RED CARD! $actingTeamCode down to 10 men",
                                message = "${player.name} sent off in the ${nextMinute}th minute!",
                                timestamp = System.currentTimeMillis(),
                                type = "RED_CARD"
                            )
                        )
                    }
                    "SUBSTITUTION" -> {
                        val benchPlayer = playersList.filter { it.position != "GK" && it.id != player.id }.randomOrNull()
                        if (benchPlayer != null) {
                            eventDao.insertEvent(
                                MatchEventEntity(
                                    matchId = matchId,
                                    minute = nextMinute,
                                    type = "SUBSTITUTION",
                                    teamId = actingTeamId,
                                    playerName = benchPlayer.name,
                                    playerNameOut = player.name,
                                    detail = "Manager changes tactics."
                                )
                            )
                        }
                    }
                    "VAR" -> {
                        val decisionOverturned = Random.nextBoolean()
                        val detail = if (decisionOverturned) {
                            "VAR review completes: Overturned! Goal disallowed for a tight offside in build-up."
                        } else {
                            "VAR review completes: Decision Stands! No penalty awarded after check."
                        }
                        eventDao.insertEvent(
                            MatchEventEntity(
                                matchId = matchId,
                                minute = nextMinute,
                                type = "VAR",
                                teamId = actingTeamId,
                                playerName = "",
                                detail = detail
                            )
                        )
                    }
                }
            }

            // Update match fixture in database
            val updatedMatch = match.copy(
                homeScore = newHomeScore,
                awayScore = newAwayScore,
                possessionHome = newPossessionHome,
                possessionAway = newPossessionAway,
                shotsHome = match.shotsHome + shotsHomeDelta,
                shotsAway = match.shotsAway + shotsAwayDelta,
                shotsOnTargetHome = match.shotsOnTargetHome + shotsOnTargetHomeDelta,
                shotsOnTargetAway = match.shotsOnTargetAway + shotsOnTargetAwayDelta,
                xgHome = match.xgHome + xgHomeDelta,
                xgAway = match.xgAway + xgAwayDelta,
                passesHome = match.passesHome + passesHomeDelta,
                passesAway = match.passesAway + passesAwayDelta,
                passAccuracyHome = if (match.passAccuracyHome == 0) Random.nextInt(75, 93) else match.passAccuracyHome,
                passAccuracyAway = if (match.passAccuracyAway == 0) Random.nextInt(75, 93) else match.passAccuracyAway,
                foulsHome = match.foulsHome + Random.nextInt(0, 2),
                foulsAway = match.foulsAway + Random.nextInt(0, 2)
            )
            fixtureDao.updateFixture(updatedMatch)
        }
    }

    private fun chooseRandomEventType(): String {
        val r = Random.nextFloat()
        return when {
            r < 0.25 -> "GOAL"
            r < 0.55 -> "YELLOW_CARD"
            r < 0.70 -> "SUBSTITUTION"
            r < 0.85 -> "VAR"
            else -> "RED_CARD"
        }
    }

    private suspend fun getTeamName(db: AppDatabase, teamId: Int): String {
        return db.teamDao().getTeamById(teamId)?.name ?: "Team $teamId"
    }

    private suspend fun getTeamCode(db: AppDatabase, teamId: Int): String {
        return db.teamDao().getTeamById(teamId)?.code ?: "TM"
    }

    // Direct injection of custom events via Admin panel
    suspend fun injectCustomEvent(
        db: AppDatabase,
        matchId: Int,
        minute: Int,
        type: String,
        teamId: Int,
        playerName: String,
        playerNameOut: String,
        detail: String
    ) {
        val eventDao = db.matchEventDao()
        val fixtureDao = db.matchFixtureDao()
        val notificationDao = db.notificationDao()
        val playerDao = db.playerDao()

        val match = fixtureDao.getMatchById(matchId) ?: return

        // Insert event
        val event = MatchEventEntity(
            matchId = matchId,
            minute = minute,
            type = type,
            teamId = teamId,
            playerName = playerName,
            playerNameOut = playerNameOut,
            detail = detail
        )
        eventDao.insertEvent(event)

        var newHomeScore = match.homeScore
        var newAwayScore = match.awayScore

        // Apply stat updates
        if (type == "GOAL") {
            if (teamId == match.homeTeamId) {
                newHomeScore++
            } else {
                newAwayScore++
            }

            // Update player goals
            if (playerName.isNotEmpty()) {
                val teamPlayers = playerDao.getPlayersByTeamList(teamId)
                val pl = teamPlayers.find { it.name.contains(playerName, ignoreCase = true) }
                if (pl != null) {
                    playerDao.updatePlayer(pl.copy(goals = pl.goals + 1))
                }
            }

            notificationDao.insertNotification(
                NotificationEntity(
                    title = "GOAL! ${getTeamName(db, match.homeTeamId)} $newHomeScore-$newAwayScore ${getTeamName(db, match.awayTeamId)}",
                    message = "$playerName ($minute') scores! $detail",
                    timestamp = System.currentTimeMillis(),
                    type = "GOAL"
                )
            )
        } else if (type == "RED_CARD") {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "RED CARD!",
                    message = "$playerName has been sent off for ${getTeamName(db, teamId)} in the ${minute}th minute!",
                    timestamp = System.currentTimeMillis(),
                    type = "RED_CARD"
                )
            )
        } else if (type == "VAR") {
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "VAR Decision Alert",
                    message = "Minute $minute: $detail",
                    timestamp = System.currentTimeMillis(),
                    type = "INFO"
                )
            )
        }

        // Update match stats
        fixtureDao.updateFixture(
            match.copy(
                homeScore = newHomeScore,
                awayScore = newAwayScore,
                shotsHome = match.shotsHome + if (teamId == match.homeTeamId) 1 else 0,
                shotsAway = match.shotsAway + if (teamId == match.awayTeamId) 1 else 0,
                shotsOnTargetHome = match.shotsOnTargetHome + if (teamId == match.homeTeamId && type == "GOAL") 1 else 0,
                shotsOnTargetAway = match.shotsOnTargetAway + if (teamId == match.awayTeamId && type == "GOAL") 1 else 0,
                xgHome = match.xgHome + if (teamId == match.homeTeamId) 0.15 else 0.0,
                xgAway = match.xgAway + if (teamId == match.awayTeamId) 0.15 else 0.0
            )
        )
    }
}
