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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MatchFixtureEntity
import com.example.data.database.TeamEntity
import com.example.data.model.Screen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

@Composable
fun DashboardScreen(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    val liveMatches by viewModel.liveMatches.collectAsState()
    val allFixtures by viewModel.fixtures.collectAsState()
    val standings by viewModel.standings.collectAsState()
    val players by viewModel.players.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val teamsMap = remember(teamsList) { teamsList.associateBy { it.id } }

    var showNotificationsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App header with alerts icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PREMIER LEAGUE 2026-27",
                        color = PlSleekPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Live Football Intelligence",
                        color = PlTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                IconButton(
                    onClick = { showNotificationsDialog = true },
                    modifier = Modifier
                        .background(PlCardPurple, CircleShape)
                        .border(1.dp, PlSleekBorder, CircleShape)
                        .testTag("alerts_button")
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = PlSleekPurple
                        )
                        if (notifications.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(PlErrorRed)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }

        // Live Matches Banner (Dynamic WebSocket Updates)
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "LIVE IN PROGRESS",
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (liveMatches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PlCardPurple)
                            .border(1.dp, PlTextSecondary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches currently live. Head over to the AI & Admin hub to trigger or inject live match events!",
                            color = PlTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (match in liveMatches) {
                            val home = teamsMap[match.homeTeamId]
                            val away = teamsMap[match.awayTeamId]
                            LiveMatchBadge(match = match, home = home, away = away) {
                                viewModel.navigateTo(Screen.MatchDetail(match.id))
                            }
                        }
                    }
                }
            }
        }

        // Today's Fixtures / Gameweek Summary
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S FIXTURES",
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "SEE ALL",
                    color = PlNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Fixtures) }
                        .padding(4.dp)
                )
            }
        }

        // Display 2 Gameweek 2 fixtures that are scheduled or finished
        val gwFixtures = allFixtures.filter { it.gameweek == 2 && it.status != "LIVE" }.take(2)
        if (gwFixtures.isEmpty()) {
            item {
                Text(
                    text = "No scheduled matches for today.",
                    color = PlTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(gwFixtures) { match ->
                val home = teamsMap[match.homeTeamId]
                val away = teamsMap[match.awayTeamId]
                MatchRowItem(match = match, homeTeam = home, awayTeam = away) {
                    viewModel.navigateTo(Screen.MatchDetail(match.id))
                }
            }
        }

        // Personalized Club Feed
        item {
            val favTeamId = profile?.favoriteTeamId ?: 1
            val favTeamName = teamsMap[favTeamId]?.name ?: "Arsenal"
            val favTeamCode = teamsMap[favTeamId]?.code ?: "ARS"
            val favTeamColor = teamsMap[favTeamId]?.primaryColor ?: "#EF0107"

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "YOUR CLUB FEED: ${favTeamName.uppercase()}",
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PlCard(
                    borderColor = try { Color(android.graphics.Color.parseColor(favTeamColor)).copy(alpha = 0.3f) } catch (e: Exception) { PlNeonGreen.copy(alpha = 0.2f) }
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        TeamBadge(
                            code = favTeamCode,
                            primaryColorHex = favTeamColor,
                            secondaryColorHex = teamsMap[favTeamId]?.secondaryColor ?: "#FFFFFF",
                            size = 44
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Tactical Preview: Gameweek 2 Clashes",
                                color = PlTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Manager ${teamsMap[favTeamId]?.manager ?: "Mikel Arteta"} has finalized tactical briefings for the upcoming clash at ${teamsMap[favTeamId]?.venue ?: "Emirates Stadium"}. Insights point towards high positional intensity and exploiting space in wide channels. Ask the AI Football Assistant in the chat tab to predict this match's outcome!",
                                color = PlTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PlNeonGreen.copy(alpha = 0.15f))
                                    .clickable { viewModel.navigateTo(Screen.Clubhouse) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enter Clubhouse",
                                    color = PlNeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Go",
                                    tint = PlNeonGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Table snapshot & Fantasy Insights (FDR, Value picks)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STANDINGS SNAPSHOT",
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "FULL STANDINGS",
                    color = PlNeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.Standings) }
                        .padding(4.dp)
                )
            }

            PlCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Club", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.5f))
                    Text(text = "PL", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                    Text(text = "GD", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(text = "PTS", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }

                standings.take(4).forEachIndexed { index, standing ->
                    Divider(color = PlTextSecondary.copy(alpha = 0.05f), thickness = 0.5.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.TeamDetail(standing.teamId)) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (index < 3) PlNeonGreen else PlTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp)
                        )
                        TeamBadge(
                            code = standing.teamCode,
                            primaryColorHex = standing.primaryColor,
                            secondaryColorHex = "#FFFFFF",
                            size = 24,
                            borderWidth = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = standing.teamName,
                            color = PlTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(2.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${standing.gamesPlayed}",
                            color = PlTextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${if (standing.goalDifference > 0) "+" else ""}${standing.goalDifference}",
                            color = if (standing.goalDifference > 0) PlNeonGreen else if (standing.goalDifference < 0) PlErrorRed else PlTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${standing.points}",
                            color = PlGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Fantasy Insights Engine Section (FDR, Value Picks, differentials)
        item {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Text(
                    text = "FANTASY INSIGHTS ENGINE",
                    color = PlTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Differential Picks
                    PlCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Star", tint = PlGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Captain Choice", color = PlGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val topPlayer = players.maxByOrNull { it.goals * 3 + it.assists * 2 }
                        Text(
                            text = topPlayer?.name ?: "Bukayo Saka",
                            color = PlTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FDR Rating: 4.8",
                            color = PlTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${topPlayer?.goals ?: 16}G / ${topPlayer?.assists ?: 12}A this season",
                            color = PlNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Value Player Picks
                    PlCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = PlSkyBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Value Differential", color = PlSkyBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val differentialPlayer = players.firstOrNull { it.isDifferential } ?: players.firstOrNull()
                        Text(
                            text = differentialPlayer?.name ?: "Leon Bailey",
                            color = PlTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "FDR Rating: 3.2",
                            color = PlTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Expected Goals: ${differentialPlayer?.expectedGoals ?: 8.5} xG",
                            color = PlSkyBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // System Notifications Dialog Log
    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LIVE Match Alerts", color = PlTextPrimary, fontWeight = FontWeight.Black)
                    TextButton(onClick = { viewModel.clearNotifications() }) {
                        Text(text = "CLEAR ALL", color = PlErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    if (notifications.isEmpty()) {
                        Text(
                            text = "No match alerts yet. Active live events (goals, red cards) in simulated matches will trigger alerts here in real-time!",
                            color = PlTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(notifications) { notif ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PlDarkPurple)
                                        .border(0.5.dp, PlTextSecondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (notif.type) {
                                                        "GOAL" -> PlNeonGreen
                                                        "RED_CARD" -> PlErrorRed
                                                        else -> PlSkyBlue
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = notif.title,
                                            color = PlTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        color = PlTextSecondary,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNotificationsDialog = false }) {
                    Text(text = "CLOSE")
                }
            },
            containerColor = PlCardPurple
        )
    }
}

@Composable
fun LiveMatchBadge(
    match: MatchFixtureEntity,
    home: TeamEntity?,
    away: TeamEntity?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PlCardPurple)
            .border(1.dp, PlNeonGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(PlErrorRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE UPDATE",
                        color = PlErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PlNeonGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "xG: ${"%.2f".format(match.xgHome)} - ${"%.2f".format(match.xgAway)}",
                        color = PlNeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    TeamBadge(
                        code = home?.code ?: "HM",
                        primaryColorHex = home?.primaryColor ?: "#EF0107",
                        secondaryColorHex = "#FFFFFF",
                        size = 32
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = home?.shortName ?: "Home",
                        color = PlTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Score
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${match.homeScore}",
                        color = PlNeonGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = " - ",
                        color = PlTextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "${match.awayScore}",
                        color = PlNeonGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Away
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = away?.shortName ?: "Away",
                        color = PlTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamBadge(
                        code = away?.code ?: "AW",
                        primaryColorHex = away?.primaryColor ?: "#034694",
                        secondaryColorHex = "#FFFFFF",
                        size = 32
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = match.possessionHome.toFloat() / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = PlNeonGreen,
                trackColor = PlSkyBlue
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${match.possessionHome}% Possession",
                    color = PlTextSecondary,
                    fontSize = 9.sp
                )
                Text(
                    text = "${match.possessionAway}% Possession",
                    color = PlTextSecondary,
                    fontSize = 9.sp
                )
            }
        }
    }
}
