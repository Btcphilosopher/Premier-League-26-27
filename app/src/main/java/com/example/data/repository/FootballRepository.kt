package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow

class FootballRepository(private val db: AppDatabase) {

    val allTeams: Flow<List<TeamEntity>> = db.teamDao().getAllTeams()
    val allPlayers: Flow<List<PlayerEntity>> = db.playerDao().getAllPlayers()
    val allFixtures: Flow<List<MatchFixtureEntity>> = db.matchFixtureDao().getAllFixtures()
    val liveMatches: Flow<List<MatchFixtureEntity>> = db.matchFixtureDao().getLiveMatches()
    val allTransfers: Flow<List<TransferEntity>> = db.transferDao().getAllTransfers()
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getUserProfile()

    fun getPlayersByTeam(teamId: Int): Flow<List<PlayerEntity>> = db.playerDao().getPlayersByTeam(teamId)

    fun getMatchByIdFlow(matchId: Int): Flow<MatchFixtureEntity?> = db.matchFixtureDao().getMatchByIdFlow(matchId)

    suspend fun getMatchById(matchId: Int): MatchFixtureEntity? = db.matchFixtureDao().getMatchById(matchId)

    suspend fun getTeamById(teamId: Int): TeamEntity? = db.teamDao().getTeamById(teamId)

    fun getEventsByMatch(matchId: Int): Flow<List<MatchEventEntity>> = db.matchEventDao().getEventsByMatch(matchId)

    fun getFixturesByGameweek(gameweek: Int): Flow<List<MatchFixtureEntity>> = db.matchFixtureDao().getFixturesByGameweek(gameweek)

    fun getFantasyPickPlayers(): Flow<List<PlayerEntity>> = db.playerDao().getFantasyPickPlayers()

    suspend fun insertNotification(notification: NotificationEntity) = db.notificationDao().insertNotification(notification)

    suspend fun clearAllNotifications() = db.notificationDao().clearAllNotifications()

    suspend fun saveUserProfile(profile: UserProfileEntity) = db.userProfileDao().saveUserProfile(profile)

    suspend fun updateFixture(fixture: MatchFixtureEntity) = db.matchFixtureDao().updateFixture(fixture)

    suspend fun injectEvent(
        matchId: Int,
        minute: Int,
        type: String,
        teamId: Int,
        playerName: String,
        playerNameOut: String,
        detail: String
    ) {
        com.example.service.MatchSimulationEngine.injectCustomEvent(
            db = db,
            matchId = matchId,
            minute = minute,
            type = type,
            teamId = teamId,
            playerName = playerName,
            playerNameOut = playerNameOut,
            detail = detail
        )
    }
}
