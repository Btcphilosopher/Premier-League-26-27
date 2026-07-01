package com.example.data.model

import com.example.data.database.TeamEntity

data class TeamStanding(
    val teamId: Int,
    val teamName: String,
    val teamCode: String,
    val primaryColor: String,
    val gamesPlayed: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val goalDifference: Int,
    val points: Int,
    val form: List<String>
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface Screen {
    object Dashboard : Screen
    object Fixtures : Screen
    object Standings : Screen
    object Clubhouse : Screen
    object AiAssistant : Screen
    data class MatchDetail(val matchId: Int) : Screen
    data class TeamDetail(val teamId: Int) : Screen
}
