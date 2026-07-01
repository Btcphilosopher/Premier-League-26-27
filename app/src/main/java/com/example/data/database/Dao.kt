package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :id LIMIT 1")
    suspend fun getTeamById(id: Int): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeams(teams: List<TeamEntity>)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    fun getPlayersByTeam(teamId: Int): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId")
    suspend fun getPlayersByTeamList(teamId: Int): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE isValuePlayer = 1 OR isDifferential = 1")
    fun getFantasyPickPlayers(): Flow<List<PlayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)

    @Update
    suspend fun updatePlayer(player: PlayerEntity)
}

@Dao
interface MatchFixtureDao {
    @Query("SELECT * FROM match_fixtures ORDER BY kickoffTime ASC")
    fun getAllFixtures(): Flow<List<MatchFixtureEntity>>

    @Query("SELECT * FROM match_fixtures WHERE gameweek = :gameweek ORDER BY kickoffTime ASC")
    fun getFixturesByGameweek(gameweek: Int): Flow<List<MatchFixtureEntity>>

    @Query("SELECT * FROM match_fixtures WHERE status = 'LIVE'")
    fun getLiveMatches(): Flow<List<MatchFixtureEntity>>

    @Query("SELECT * FROM match_fixtures WHERE id = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: Int): MatchFixtureEntity?

    @Query("SELECT * FROM match_fixtures WHERE id = :matchId LIMIT 1")
    fun getMatchByIdFlow(matchId: Int): Flow<MatchFixtureEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixtures(fixtures: List<MatchFixtureEntity>)

    @Update
    suspend fun updateFixture(fixture: MatchFixtureEntity)
}

@Dao
interface MatchEventDao {
    @Query("SELECT * FROM match_events WHERE matchId = :matchId ORDER BY minute DESC, id DESC")
    fun getEventsByMatch(matchId: Int): Flow<List<MatchEventEntity>>

    @Query("SELECT * FROM match_events WHERE matchId = :matchId ORDER BY minute DESC, id DESC")
    suspend fun getEventsByMatchList(matchId: Int): List<MatchEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MatchEventEntity)

    @Query("DELETE FROM match_events WHERE matchId = :matchId")
    suspend fun deleteEventsByMatch(matchId: Int)
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY date DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfers(transfers: List<TransferEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)
}
