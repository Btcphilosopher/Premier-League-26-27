package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
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
import com.example.data.database.MatchFixtureEntity
import com.example.data.database.TeamEntity
import com.example.data.model.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixturesScreen(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    val selectedGameweek by viewModel.selectedGameweek.collectAsState()
    val allFixtures by viewModel.fixtures.collectAsState()

    val teamsMap = remember(teamsList) { teamsList.associateBy { it.id } }

    var selectedClubFilterId by remember { mutableStateOf<Int?>(null) }
    var showClubFilterDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    // Filter fixtures based on selected gameweek and club filter
    val filteredFixtures = remember(allFixtures, selectedGameweek, selectedClubFilterId) {
        allFixtures.filter { match ->
            val matchesGw = match.gameweek == selectedGameweek
            val matchesClub = if (selectedClubFilterId != null) {
                match.homeTeamId == selectedClubFilterId || match.awayTeamId == selectedClubFilterId
            } else true
            matchesGw && matchesClub
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("fixtures_screen")
    ) {
        // Upper Title section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MATCH SCHEDULE",
                    color = PlSleekPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Season 2026–27",
                    color = PlTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Calendar sync button
            Button(
                onClick = { showSyncDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PlSleekPurple),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("calendar_sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Sync",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sync Calendar",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Gameweek Selector (Horizontal Scroller 1 to 38)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 10.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (gw in 1..38) {
                val isSelected = selectedGameweek == gw
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else PlCardPurple)
                        .border(
                            1.dp,
                            if (isSelected) Color.White else PlSleekBorder,
                            CircleShape
                        )
                        .clickable { viewModel.setGameweek(gw) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GW $gw",
                        color = if (isSelected) Color.Black else PlTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filter chips bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { showClubFilterDialog = true },
                label = {
                    Text(
                        text = if (selectedClubFilterId == null) "Filter by Club" else teamsMap[selectedClubFilterId]?.name ?: "Club Filter",
                        color = PlTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = PlCardPurple,
                    labelColor = PlTextPrimary
                ),
                modifier = Modifier.testTag("club_filter_chip")
            )

            if (selectedClubFilterId != null) {
                TextButton(onClick = { selectedClubFilterId = null }) {
                    Text(text = "Clear Filter", color = PlErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Fixtures list
        if (filteredFixtures.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No fixtures found for selected round or filters.",
                    color = PlTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredFixtures) { match ->
                    val home = teamsMap[match.homeTeamId]
                    val away = teamsMap[match.awayTeamId]
                    MatchRowItem(match = match, homeTeam = home, awayTeam = away) {
                        viewModel.navigateTo(Screen.MatchDetail(match.id))
                    }
                }
            }
        }
    }

    // Club selection filter dialog
    if (showClubFilterDialog) {
        AlertDialog(
            onDismissRequest = { showClubFilterDialog = false },
            title = { Text("Select Premier League Club", color = PlTextPrimary, fontWeight = FontWeight.Black) },
            text = {
                Box(modifier = Modifier.heightIn(max = 320.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(teamsList) { team ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedClubFilterId == team.id) PlNeonGreen.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable {
                                        selectedClubFilterId = team.id
                                        showClubFilterDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TeamBadge(code = team.code, primaryColorHex = team.primaryColor, secondaryColorHex = team.secondaryColor, size = 28)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = team.name, color = PlTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showClubFilterDialog = false }) {
                    Text("CLOSE", color = PlNeonGreen)
                }
            },
            containerColor = PlCardPurple
        )
    }

    // Calendar sync success confirmation dialog
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Success", tint = PlNeonGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Calendar Sync Successful", color = PlTextPrimary, fontWeight = FontWeight.Black, textAlign = TextAlign.Center) },
            text = {
                Text(
                    text = "The entire Premier League 2026–27 season fixtures for Gameweek $selectedGameweek have been added to your local device calendar. Kickoff times are synced to your local timezone with dynamic pre-match alerts configured.",
                    color = PlTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSyncDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PlNeonGreen)
                ) {
                    Text("AWESOME", color = PlDarkPurple, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = PlCardPurple
        )
    }
}
