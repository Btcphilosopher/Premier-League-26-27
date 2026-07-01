package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.model.ChatMessage
import com.example.data.model.Screen
import com.example.data.model.TeamStanding
import com.example.data.repository.FootballRepository
import com.example.service.GeminiService
import com.example.service.MatchSimulationEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FootballViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = FootballRepository(db)

    // Current Navigation Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen Backstack for detail views
    private val screenBackstack = mutableListOf<Screen>()

    // Selected gameweek (1-38)
    private val _selectedGameweek = MutableStateFlow(2)
    val selectedGameweek: StateFlow<Int> = _selectedGameweek.asStateFlow()

    // Flow states
    val teams = repository.allTeams.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val players = repository.allPlayers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val fixtures = repository.allFixtures.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val liveMatches = repository.liveMatches.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val transfers = repository.allTransfers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val notifications = repository.allNotifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Dynamic Live Standings
    val standings: StateFlow<List<TeamStanding>> = combine(teams, fixtures) { teamList, fixtureList ->
        calculateStandings(teamList, fixtureList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Match Detail state
    private val _selectedMatchId = MutableStateFlow<Int?>(null)
    val selectedMatchId: StateFlow<Int?> = _selectedMatchId.asStateFlow()

    val selectedMatch: StateFlow<MatchFixtureEntity?> = _selectedMatchId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getMatchByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedMatchEvents: StateFlow<List<MatchEventEntity>> = _selectedMatchId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getEventsByMatch(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Clubhouse selected team detail
    private val _selectedTeamId = MutableStateFlow<Int?>(null)
    val selectedTeamId: StateFlow<Int?> = _selectedTeamId.asStateFlow()

    val selectedTeam: StateFlow<TeamEntity?> = _selectedTeamId.flatMapLatest { id ->
        if (id == null) flowOf<TeamEntity?>(null) else flow {
            emit(repository.getTeamById(id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedTeamPlayers: StateFlow<List<PlayerEntity>> = _selectedTeamId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getPlayersByTeam(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Football Assistant
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "assistant",
                text = "Welcome to the AI Football Intelligence Assistant! Ask me about tactical trends, player comparisons, gameweek outcomes, or fantasy captain picks for the 2026-27 season!"
            )
        )
    )
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Simulation states
    val isSimulating = MatchSimulationEngine.isSimulating
    val simSpeedSeconds = MatchSimulationEngine.simSpeedSeconds

    init {
        viewModelScope.launch {
            try {
                // Prepopulate if needed
                DatabaseInitializer.prepopulateIfNeeded(db)
                // Start Match Simulation Engine
                MatchSimulationEngine.startSimulation(db, viewModelScope, getApplication())
            } catch (e: Exception) {
                Log.e("FootballViewModel", "Error initializing data", e)
            }
        }
    }

    // Navigation methods
    fun navigateTo(screen: Screen) {
        if (screen is Screen.Dashboard || screen is Screen.Fixtures || screen is Screen.Standings || screen is Screen.Clubhouse || screen is Screen.AiAssistant) {
            screenBackstack.clear()
        } else {
            screenBackstack.add(_currentScreen.value)
        }
        _currentScreen.value = screen
        if (screen is Screen.MatchDetail) {
            _selectedMatchId.value = screen.matchId
        } else if (screen is Screen.TeamDetail) {
            _selectedTeamId.value = screen.teamId
        }
    }

    fun navigateBack(): Boolean {
        if (screenBackstack.isNotEmpty()) {
            val prev = screenBackstack.removeAt(screenBackstack.size - 1)
            _currentScreen.value = prev
            if (prev is Screen.MatchDetail) {
                _selectedMatchId.value = prev.matchId
            } else if (prev is Screen.TeamDetail) {
                _selectedTeamId.value = prev.teamId
            }
            return true
        }
        _currentScreen.value = Screen.Dashboard
        return false
    }

    fun setGameweek(gw: Int) {
        _selectedGameweek.value = gw
    }

    // AI Chat action
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return
        val userMsg = ChatMessage(sender = "user", text = text)
        _chatHistory.value = _chatHistory.value + userMsg

        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val response = GeminiService.askAssistant(text)
                val aiMsg = ChatMessage(sender = "assistant", text = response)
                _chatHistory.value = _chatHistory.value + aiMsg
            } catch (e: Exception) {
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    sender = "assistant",
                    text = "Tactical communications issue. Please check your connection and try again."
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                sender = "assistant",
                text = "Chat history cleared. How can I assist with your 2026-27 football tactics today?"
            )
        )
    }

    // Simulation Controls
    fun toggleSimulation(enabled: Boolean) {
        MatchSimulationEngine.setSimulating(enabled)
    }

    fun changeSimulationSpeed(seconds: Long) {
        MatchSimulationEngine.setSimSpeed(seconds)
    }

    fun injectEvent(
        matchId: Int,
        minute: Int,
        type: String,
        teamId: Int,
        playerName: String,
        playerNameOut: String,
        detail: String
    ) {
        viewModelScope.launch {
            repository.injectEvent(
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

    fun clearNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateFavoriteTeam(teamId: Int) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(current.copy(favoriteTeamId = teamId))
        }
    }

    fun updateNotificationsSettings(goals: Boolean, reminders: Boolean, redCards: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(
                current.copy(
                    notificationGoals = goals,
                    notificationReminders = reminders,
                    notificationRedCards = redCards
                )
            )
        }
    }

    // High fidelity dynamic standings calculator
    private fun calculateStandings(teams: List<TeamEntity>, fixtures: List<MatchFixtureEntity>): List<TeamStanding> {
        val standingsMap = teams.associate { team ->
            team.id to TeamStanding(
                teamId = team.id,
                teamName = team.name,
                teamCode = team.code,
                primaryColor = team.primaryColor,
                gamesPlayed = 0,
                won = 0,
                drawn = 0,
                lost = 0,
                goalsFor = 0,
                goalsAgainst = 0,
                goalDifference = 0,
                points = 0,
                form = emptyList()
            )
        }.toMutableMap()

        // Process finished and live matches to build up-to-date real-time metrics
        val sortedFixtures = fixtures.sortedBy { it.kickoffTime }

        for (fixture in sortedFixtures) {
            if (fixture.status == "SCHEDULED") continue

            val homeId = fixture.homeTeamId
            val awayId = fixture.awayTeamId
            val homeScore = fixture.homeScore
            val awayScore = fixture.awayScore

            val homeStanding = standingsMap[homeId]
            val awayStanding = standingsMap[awayId]

            if (homeStanding != null && awayStanding != null) {
                // Home Team Stats
                val homeWon = if (homeScore > awayScore) 1 else 0
                val homeDrawn = if (homeScore == awayScore) 1 else 0
                val homeLost = if (homeScore < awayScore) 1 else 0
                val homePoints = homeWon * 3 + homeDrawn * 1

                val updatedHomeForm = (homeStanding.form + when {
                    homeScore > awayScore -> "W"
                    homeScore == awayScore -> "D"
                    else -> "L"
                }).takeLast(5)

                standingsMap[homeId] = homeStanding.copy(
                    gamesPlayed = homeStanding.gamesPlayed + 1,
                    won = homeStanding.won + homeWon,
                    drawn = homeStanding.drawn + homeDrawn,
                    lost = homeStanding.lost + homeLost,
                    goalsFor = homeStanding.goalsFor + homeScore,
                    goalsAgainst = homeStanding.goalsAgainst + awayScore,
                    goalDifference = homeStanding.goalDifference + (homeScore - awayScore),
                    points = homeStanding.points + homePoints,
                    form = updatedHomeForm
                )

                // Away Team Stats
                val awayWon = if (awayScore > homeScore) 1 else 0
                val awayDrawn = if (awayScore == homeScore) 1 else 0
                val awayLost = if (awayScore < homeScore) 1 else 0
                val awayPoints = awayWon * 3 + awayDrawn * 1

                val updatedAwayForm = (awayStanding.form + when {
                    awayScore > homeScore -> "W"
                    awayScore == homeScore -> "D"
                    else -> "L"
                }).takeLast(5)

                standingsMap[awayId] = awayStanding.copy(
                    gamesPlayed = awayStanding.gamesPlayed + 1,
                    won = awayStanding.won + awayWon,
                    drawn = awayStanding.drawn + awayDrawn,
                    lost = awayStanding.lost + awayLost,
                    goalsFor = awayStanding.goalsFor + awayScore,
                    goalsAgainst = awayStanding.goalsAgainst + homeScore,
                    goalDifference = awayStanding.goalDifference + (awayScore - homeScore),
                    points = awayStanding.points + awayPoints,
                    form = updatedAwayForm
                )
            }
        }

        // Sort table strictly according to Premier League rules
        return standingsMap.values.sortedWith(
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
                .thenBy { it.teamName }
        )
    }
}
