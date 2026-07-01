package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PlayerEntity
import com.example.data.database.TeamEntity
import com.example.data.database.TransferEntity
import com.example.data.model.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClubhouseScreen(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (val screen = currentScreen) {
        is Screen.TeamDetail -> {
            TeamDetailView(viewModel = viewModel, teamId = screen.teamId, teamsList = teamsList)
        }
        else -> {
            ClubhouseMainView(viewModel = viewModel, teamsList = teamsList)
        }
    }
}

@Composable
fun ClubhouseMainView(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>
) {
    val transfers by viewModel.transfers.collectAsState()
    var selectedClubhouseTab by remember { mutableStateOf(0) } // 0 = Clubs, 1 = Transfer Hub

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CLUBHOUSE & TRANSFERS",
                color = PlSleekPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = if (selectedClubhouseTab == 0) "Premier League Clubs" else "Transfer Window Tracker",
                color = PlTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        TabRow(
            selectedTabIndex = selectedClubhouseTab,
            containerColor = PlCardPurple,
            contentColor = PlSleekPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedClubhouseTab]),
                    color = PlSleekPurple
                )
            },
            divider = { Divider(color = PlTextSecondary.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedClubhouseTab == 0,
                onClick = { selectedClubhouseTab = 0 },
                text = { Text("ALL 20 CLUBS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedClubhouseTab == 1,
                onClick = { selectedClubhouseTab = 1 },
                text = { Text("TRANSFER HUB", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        if (selectedClubhouseTab == 0) {
            // Display Grid of all 20 Clubs
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(teamsList) { team ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlCardPurple)
                            .border(0.5.dp, PlSleekBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.navigateTo(Screen.TeamDetail(team.id)) }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TeamBadge(
                                code = team.code,
                                primaryColorHex = team.primaryColor,
                                secondaryColorHex = team.secondaryColor,
                                size = 48
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = team.name,
                                color = PlTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Venue: ${team.venue}",
                                color = PlTextSecondary,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            // Display Transfer Hub with Confirmed vs Rumours
            val confirmedTransfers = remember(transfers) { transfers.filter { it.status == "CONFIRMED" } }
            val rumouredTransfers = remember(transfers) { transfers.filter { it.status == "RUMOUR" } }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Summer 2026 Window Header info
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(PlSleekPurple.copy(alpha = 0.08f))
                            .border(1.dp, PlSleekPurple.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "SUMMER 2026 WINDOW IS ACTIVE",
                                color = PlSleekPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All confirmed transfers and latest rumors with analytical AI ratings. Squad updates are dynamically injected upon signing confirmation.",
                                color = PlTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Confirmed Section
                item {
                    Text(
                        text = "CONFIRMED TRANSFERS",
                        color = PlTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
                    )
                }

                if (confirmedTransfers.isEmpty()) {
                    item {
                        Text("No transfers confirmed yet.", color = PlTextSecondary, fontSize = 12.sp)
                    }
                } else {
                    items(confirmedTransfers) { transfer ->
                        TransferRowItem(transfer = transfer)
                    }
                }

                // Rumours Section
                item {
                    Text(
                        text = "HOT TRANSFER RUMOURS",
                        color = PlGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                if (rumouredTransfers.isEmpty()) {
                    item {
                        Text("No rumors tracked.", color = PlTextSecondary, fontSize = 12.sp)
                    }
                } else {
                    items(rumouredTransfers) { rumour ->
                        TransferRowItem(transfer = rumour)
                    }
                }
            }
        }
    }
}

@Composable
fun TransferRowItem(transfer: TransferEntity) {
    val isConfirmed = transfer.status == "CONFIRMED"
    PlCard(
        modifier = Modifier.padding(vertical = 5.dp),
        borderColor = if (isConfirmed) PlNeonGreen.copy(alpha = 0.1f) else PlGold.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = transfer.playerName,
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transfer.fromTeam,
                        color = PlTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " → ",
                        color = if (isConfirmed) PlNeonGreen else PlGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transfer.toTeam,
                        color = PlTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isConfirmed) PlNeonGreen.copy(alpha = 0.15f) else PlGold.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isConfirmed) "SIGNED" else "RUMOUR",
                        color = if (isConfirmed) PlNeonGreen else PlGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fee: ${transfer.fee}",
                    color = PlTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun TeamDetailView(
    viewModel: FootballViewModel,
    teamId: Int,
    teamsList: List<TeamEntity>
) {
    val team = remember(teamsList, teamId) { teamsList.find { it.id == teamId } }
    val squadPlayers by viewModel.selectedTeamPlayers.collectAsState()

    if (team == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Club profile not found.", color = PlTextSecondary)
        }
        return
    }

    var selectedSectionTab by remember { mutableStateOf(0) } // 0 = Tactical Profile, 1 = Squad, 2 = Injuries

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("team_detail_view")
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateBack() },
                modifier = Modifier
                    .background(PlCardPurple, CircleShape)
                    .border(1.dp, PlSleekBorder, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PlSleekPurple)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "CLUB PROFILE", color = PlSleekPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(text = team.name, color = PlTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // Hero Team Card Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(android.graphics.Color.parseColor(team.primaryColor)).copy(alpha = 0.35f),
                            PlCardPurple
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(android.graphics.Color.parseColor(team.primaryColor)).copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = team.code,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stadium: ${team.venue}",
                        color = PlTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manager: ${team.manager}",
                        color = PlGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                TeamBadge(
                    code = team.code,
                    primaryColorHex = team.primaryColor,
                    secondaryColorHex = team.secondaryColor,
                    size = 64
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detail View Tabs
        TabRow(
            selectedTabIndex = selectedSectionTab,
            containerColor = PlCardPurple,
            contentColor = PlSleekPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSectionTab]),
                    color = PlSleekPurple
                )
            },
            divider = { Divider(color = PlTextSecondary.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedSectionTab == 0,
                onClick = { selectedSectionTab = 0 },
                text = { Text("TACTICS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSectionTab == 1,
                onClick = { selectedSectionTab = 1 },
                text = { Text("SQUAD (${squadPlayers.size})", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSectionTab == 2,
                onClick = { selectedSectionTab = 2 },
                text = { Text("INJURIES", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        // Section contents
        when (selectedSectionTab) {
            0 -> {
                // Tactics and Manager Overview
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        PlCard {
                            Text(
                                text = "Tactical Philosophy",
                                color = PlNeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = team.tacticalOverview,
                                color = PlTextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    item {
                        PlCard {
                            Text(
                                text = "Manager Profile",
                                color = PlGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Manager",
                                    tint = PlGold,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = team.manager,
                                        color = PlTextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Head Coach - Season 2026-27",
                                        color = PlTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Squad List
                if (squadPlayers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No players registered in database.", color = PlTextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(squadPlayers.sortedBy { positionRank(it.position) }) { player ->
                            PlCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = player.name,
                                            color = PlTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PlNeonGreen.copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = player.position,
                                                    color = PlNeonGreen,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Accuracy: ${player.passAccuracy}%",
                                                color = PlTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "Goals", color = PlTextSecondary, fontSize = 10.sp)
                                            Text(text = "${player.goals}", color = PlGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "Assists", color = PlTextSecondary, fontSize = 10.sp)
                                            Text(text = "${player.assists}", color = PlSkyBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "xG", color = PlTextSecondary, fontSize = 10.sp)
                                            Text(text = "${player.expectedGoals}", color = PlTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Injury List
                val injuredPlayers = remember(squadPlayers) { squadPlayers.filter { it.injuryStatus != "Fit" } }

                if (injuredPlayers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Healthy", tint = PlNeonGreen, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Fully Healthy Squad!", color = PlNeonGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("No current injuries or medical doubts reported.", color = PlTextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(injuredPlayers) { player ->
                            val isInjured = player.injuryStatus == "Injured"
                            PlCard(
                                borderColor = if (isInjured) PlErrorRed.copy(alpha = 0.2f) else PlGold.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Medical Alert",
                                            tint = if (isInjured) PlErrorRed else PlGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = player.name,
                                                color = PlTextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = player.position,
                                                color = PlTextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isInjured) PlErrorRed.copy(alpha = 0.15f) else PlGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = player.injuryStatus.uppercase(),
                                                color = if (isInjured) PlErrorRed else PlGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = player.injuryDetails,
                                            color = PlTextSecondary,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun positionRank(position: String): Int {
    return when (position) {
        "GK" -> 1
        "DEF" -> 2
        "MID" -> 3
        "FWD" -> 4
        else -> 5
    }
}
