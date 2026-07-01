package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TeamEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdminScreen(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = AI Assistant, 1 = Operator Panel, 2 = My Dashboard Settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_admin_screen")
    ) {
        // Main Tab Selection
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PlCardPurple,
            contentColor = PlNeonGreen,
            divider = { Divider(color = PlTextSecondary.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("AI FOOTBALL ANALYST", fontWeight = FontWeight.Bold, fontSize = 10.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("ADMIN PANEL", fontWeight = FontWeight.Bold, fontSize = 10.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("PREFERENCES", fontWeight = FontWeight.Bold, fontSize = 10.sp) }
            )
        }

        when (selectedTab) {
            0 -> AiAssistantView(viewModel = viewModel)
            1 -> OperatorPanelView(viewModel = viewModel, teamsList = teamsList)
            2 -> PreferencesSettingsView(viewModel = viewModel, teamsList = teamsList)
        }
    }
}

@Composable
fun AiAssistantView(viewModel: FootballViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Who is the best captain pick for GW2?",
        "Predict Chelsea vs Arsenal match outcome",
        "Explain Ange Postecoglou's tactical trend",
        "Value players for fantasy differential"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatHistory) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 16.dp
                                )
                            )
                            .background(if (isUser) PlNeonGreen.copy(alpha = 0.15f) else PlCardPurple)
                            .border(
                                1.dp,
                                if (isUser) PlNeonGreen.copy(alpha = 0.3f) else PlTextSecondary.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isUser) "YOU" else "AI ASSISTANT",
                                color = if (isUser) PlNeonGreen else PlGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = PlTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(PlCardPurple)
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = PlNeonGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Analyzing tactical trends...", color = PlTextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (q in quickQuestions) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PlCardPurple)
                        .border(1.dp, PlNeonGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .clickable { viewModel.sendChatMessage(q) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = q, color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Input send box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PlCardPurple)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask about tactics, predictions, or transfers...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PlDarkPurple,
                    unfocusedContainerColor = PlDarkPurple,
                    focusedTextColor = PlTextPrimary,
                    unfocusedTextColor = PlTextPrimary,
                    cursorColor = PlNeonGreen,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                maxLines = 2
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = {
                    if (promptInput.isNotEmpty()) {
                        viewModel.sendChatMessage(promptInput)
                        promptInput = ""
                    }
                },
                modifier = Modifier
                    .background(PlNeonGreen, CircleShape)
                    .size(44.dp)
                    .testTag("ai_chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = PlDarkPurple
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .background(PlCardPurple, CircleShape)
                    .border(1.dp, PlTextSecondary.copy(alpha = 0.15f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear",
                    tint = PlErrorRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorPanelView(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>
) {
    val isSimulating by viewModel.isSimulating.collectAsState()
    val simSpeedSeconds by viewModel.simSpeedSeconds.collectAsState()

    // Form inputs for injection
    val activeMatches = listOf(201, 202) // Chelsea vs Arsenal and Liverpool vs Man United
    var selectedMatchId by remember { mutableStateOf(201) }
    var selectedEventType by remember { mutableStateOf("GOAL") }
    var inputMinute by remember { mutableStateOf("58") }
    var selectedTeamSelection by remember { mutableStateOf(0) } // 0 = Home, 1 = Away
    var inputPlayerName by remember { mutableStateOf("Viktor Gyökeres") }
    var inputDetail by remember { mutableStateOf("Powerful header bulleted past the keeper.") }

    var injectionConfirmationText by remember { mutableStateOf("") }

    val matchesText = mapOf(
        201 to "Chelsea vs Arsenal (Gtech)",
        202 to "Liverpool vs Man United (Anfield)"
    )

    val eventTypes = listOf("GOAL", "YELLOW_CARD", "RED_CARD", "SUBSTITUTION", "VAR")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // WebSocket Simulation Management
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Sim", tint = PlNeonGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "WebSocket Live Engine Simulation", color = PlTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A background simulator mimics live WebSocket feeds, generating real-time score updates, possession fluctuations, match events, and alert notifications.",
                    color = PlTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isSimulating) "ENGINE SIMULATING (ON)" else "ENGINE STOPPED (OFF)",
                            color = if (isSimulating) PlNeonGreen else PlErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Running tick speed: every $simSpeedSeconds seconds",
                            color = PlTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isSimulating,
                        onCheckedChange = { viewModel.toggleSimulation(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PlDarkPurple,
                            checkedTrackColor = PlNeonGreen,
                            uncheckedThumbColor = PlTextSecondary,
                            uncheckedTrackColor = PlDarkPurple
                        ),
                        modifier = Modifier.testTag("simulation_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Sim Tick Frequency (lower is faster):", color = PlTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(2L, 5L, 10L, 20L).forEach { sec ->
                        val isSpeedSel = simSpeedSeconds == sec
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSpeedSel) PlNeonGreen else PlDarkPurple)
                                .clickable { viewModel.changeSimulationSpeed(sec) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sec}s",
                                color = if (isSpeedSel) PlDarkPurple else PlTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Live Event Injector Form
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Inject", tint = PlGold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Live Match Event Injector", color = PlTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Match Selection Selector Row
                Text(text = "Target Live Match:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    activeMatches.forEach { mid ->
                        val isSel = selectedMatchId == mid
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PlGold.copy(alpha = 0.25f) else PlDarkPurple)
                                .border(1.dp, if (isSel) PlGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedMatchId = mid }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = matchesText[mid] ?: "Match",
                                color = if (isSel) PlGold else PlTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Event Type row
                Text(text = "Event Type:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    eventTypes.forEach { type ->
                        val isTypeSel = selectedEventType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isTypeSel) PlNeonGreen else PlDarkPurple)
                                .clickable { selectedEventType = type }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type,
                                color = if (isTypeSel) PlDarkPurple else PlTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Minute input
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Minute:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        TextField(
                            value = inputMinute,
                            onValueChange = { inputMinute = it },
                            placeholder = { Text("e.g. 64") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = PlDarkPurple,
                                unfocusedContainerColor = PlDarkPurple,
                                focusedTextColor = PlTextPrimary,
                                unfocusedTextColor = PlTextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Acting Team (Home vs Away)
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(text = "Acting Team:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("HOME", "AWAY").forEachIndexed { i, label ->
                                val isTeamSel = selectedTeamSelection == i
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isTeamSel) PlNeonGreen.copy(alpha = 0.2f) else PlDarkPurple)
                                        .border(1.dp, if (isTeamSel) PlNeonGreen else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedTeamSelection = i }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = label, color = if (isTeamSel) PlNeonGreen else PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Player name
                Text(text = "Player Name:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextField(
                    value = inputPlayerName,
                    onValueChange = { inputPlayerName = it },
                    placeholder = { Text("Player name...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PlDarkPurple,
                        unfocusedContainerColor = PlDarkPurple,
                        focusedTextColor = PlTextPrimary,
                        unfocusedTextColor = PlTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("admin_player_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Event detail description
                Text(text = "Event Detail:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextField(
                    value = inputDetail,
                    onValueChange = { inputDetail = it },
                    placeholder = { Text("Action description...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PlDarkPurple,
                        unfocusedContainerColor = PlDarkPurple,
                        focusedTextColor = PlTextPrimary,
                        unfocusedTextColor = PlTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Inject event trigger button
                Button(
                    onClick = {
                        val min = inputMinute.toIntOrNull() ?: 55
                        val isHome = selectedTeamSelection == 0
                        // Find matching team ID based on match details
                        val teamId = if (selectedMatchId == 201) {
                            if (isHome) 6 else 1 // Chelsea vs Arsenal
                        } else {
                            if (isHome) 12 else 14 // Liverpool vs United
                        }

                        viewModel.injectEvent(
                            matchId = selectedMatchId,
                            minute = min,
                            type = selectedEventType,
                            teamId = teamId,
                            playerName = inputPlayerName,
                            playerNameOut = "",
                            detail = inputDetail
                        )

                        injectionConfirmationText = "Successfully injected event: $selectedEventType for minute $min!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlNeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("inject_event_button")
                ) {
                    Text(text = "INJECT EVENT STREAM", color = PlDarkPurple, fontWeight = FontWeight.Black)
                }

                if (injectionConfirmationText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = injectionConfirmationText,
                        color = PlNeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun PreferencesSettingsView(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>
) {
    val profile by viewModel.userProfile.collectAsState()
    val teamsMap = remember(teamsList) { teamsList.associateBy { it.id } }

    var goalsEnabled by remember { mutableStateOf(true) }
    var redCardsEnabled by remember { mutableStateOf(true) }
    var remindersEnabled by remember { mutableStateOf(true) }

    var selectedFavTeamId by remember { mutableStateOf(1) }

    LaunchedEffect(profile) {
        profile?.let {
            goalsEnabled = it.notificationGoals
            redCardsEnabled = it.notificationRedCards
            remindersEnabled = it.notificationReminders
            selectedFavTeamId = it.favoriteTeamId
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Favorite Team Selector
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Face, contentDescription = "Face", tint = PlNeonGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Follow Your Favorite Club", color = PlTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Selecting your favorite club customizes your clubhouse banner, personalized feed in the dashboard, and prioritizes pre-match alerts.",
                    color = PlTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Choose Club:", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable List of Teams
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    teamsList.take(8).forEach { team ->
                        val isSel = selectedFavTeamId == team.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) PlNeonGreen.copy(alpha = 0.2f) else PlDarkPurple)
                                .border(1.dp, if (isSel) PlNeonGreen else PlTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedFavTeamId = team.id
                                    viewModel.updateFavoriteTeam(team.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TeamBadge(code = team.code, primaryColorHex = team.primaryColor, secondaryColorHex = team.secondaryColor, size = 18)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = team.shortName, color = PlTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live Alerts Push Preferences
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = PlGold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Push Notification Preferences", color = PlTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Option: Goals Alerts
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Instant Goal Updates", color = PlTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Get real-time scoring updates for all matches", color = PlTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = goalsEnabled,
                        onCheckedChange = {
                            goalsEnabled = it
                            viewModel.updateNotificationsSettings(it, remindersEnabled, redCardsEnabled)
                        }
                    )
                }

                Divider(color = PlTextSecondary.copy(alpha = 0.08f))

                // Option: Red cards
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Major Events & Red Cards", color = PlTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Receive alerts for sending-offs and VAR rulings", color = PlTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = redCardsEnabled,
                        onCheckedChange = {
                            redCardsEnabled = it
                            viewModel.updateNotificationsSettings(goalsEnabled, remindersEnabled, it)
                        }
                    )
                }

                Divider(color = PlTextSecondary.copy(alpha = 0.08f))

                // Option: Reminders
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Kickoff Reminders", color = PlTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Notify me 15 minutes before followed match kickoffs", color = PlTextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = {
                            remindersEnabled = it
                            viewModel.updateNotificationsSettings(goalsEnabled, it, redCardsEnabled)
                        }
                    )
                }
            }
        }
    }
}
