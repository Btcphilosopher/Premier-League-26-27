package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val shortName: String,
    val code: String,
    val logoUrl: String,
    val primaryColor: String,
    val secondaryColor: String,
    val venue: String,
    val manager: String,
    val tacticalOverview: String,
    val injuryListJson: String = "[]" // JSON representation of injuries
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teamId: Int,
    val name: String,
    val position: String, // GK, DEF, MID, FWD
    val goals: Int,
    val assists: Int,
    val minutesPlayed: Int,
    val expectedGoals: Double,
    val expectedAssists: Double,
    val passAccuracy: Double,
    val yellowCards: Int,
    val redCards: Int,
    val injuryStatus: String, // Fit, Injured, Doubtful
    val injuryDetails: String,
    val isValuePlayer: Boolean = false,
    val isDifferential: Boolean = false,
    val fantasyFDR: Int = 3
)

@Entity(tableName = "match_fixtures")
data class MatchFixtureEntity(
    @PrimaryKey val id: Int,
    val gameweek: Int,
    val homeTeamId: Int,
    val awayTeamId: Int,
    val homeScore: Int,
    val awayScore: Int,
    val status: String, // SCHEDULED, LIVE, FT
    val kickoffTime: Long, // timestamp
    val venue: String,
    val difficultyRatingHome: Int, // 1-5
    val difficultyRatingAway: Int, // 1-5
    val tvBroadcaster: String, // Sky Sports, TNT Sports, etc.
    val possessionHome: Int = 50,
    val possessionAway: Int = 50,
    val shotsHome: Int = 0,
    val shotsAway: Int = 0,
    val shotsOnTargetHome: Int = 0,
    val shotsOnTargetAway: Int = 0,
    val passesHome: Int = 0,
    val passesAway: Int = 0,
    val passAccuracyHome: Int = 0,
    val passAccuracyAway: Int = 0,
    val foulsHome: Int = 0,
    val foulsAway: Int = 0,
    val xgHome: Double = 0.0,
    val xgAway: Double = 0.0
)

@Entity(tableName = "match_events")
data class MatchEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val matchId: Int,
    val minute: Int,
    val type: String, // GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION, VAR
    val teamId: Int,
    val playerName: String,
    val playerNameOut: String = "",
    val detail: String = ""
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val fromTeam: String,
    val toTeam: String,
    val fee: String,
    val status: String, // CONFIRMED, RUMOUR
    val window: String, // Summer 2026, Winter 2027
    val date: Long
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String // GOAL, RED_CARD, INFO
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val favoriteTeamId: Int = 1,
    val notificationGoals: Boolean = true,
    val notificationReminders: Boolean = true,
    val notificationRedCards: Boolean = true,
    val fantasyCaptainId: Int = -1
)
