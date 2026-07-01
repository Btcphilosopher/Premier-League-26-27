package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.database.MatchEventEntity
import com.example.data.database.MatchFixtureEntity
import com.example.data.database.TeamEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

@Composable
fun LiveMatchCentre(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    val match by viewModel.selectedMatch.collectAsState()
    val events by viewModel.selectedMatchEvents.collectAsState()

    val teamsMap = remember(teamsList) { teamsList.associateBy { it.id } }

    if (match == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PlNeonGreen)
        }
        return
    }

    val activeMatch = match!!
    val homeTeam = teamsMap[activeMatch.homeTeamId]
    val awayTeam = teamsMap[activeMatch.awayTeamId]

    var selectedDetailTab by remember { mutableStateOf(0) } // 0 = Timeline, 1 = Stats, 2 = Lineups

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("live_match_centre_screen")
    ) {
        // Back toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                Text(
                    text = "MATCH CENTRE - GW ${activeMatch.gameweek}",
                    color = PlSleekPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${homeTeam?.shortName} vs ${awayTeam?.shortName}",
                    color = PlTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Live Scoreboard Banner
        ScoreboardBanner(match = activeMatch, home = homeTeam, away = awayTeam)

        Spacer(modifier = Modifier.height(16.dp))

        // Match sub-tabs
        TabRow(
            selectedTabIndex = selectedDetailTab,
            containerColor = PlCardPurple,
            contentColor = PlSleekPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDetailTab]),
                    color = PlSleekPurple
                )
            },
            divider = { Divider(color = PlTextSecondary.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedDetailTab == 0,
                onClick = { selectedDetailTab = 0 },
                text = { Text("LIVE TIMELINE", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedDetailTab == 1,
                onClick = { selectedDetailTab = 1 },
                text = { Text("STATS ENGINE", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedDetailTab == 2,
                onClick = { selectedDetailTab = 2 },
                text = { Text("TACTICAL SQUADS", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        // Selected Sub-view
        when (selectedDetailTab) {
            0 -> TimelineView(events = events, homeTeamId = activeMatch.homeTeamId, teamsMap = teamsMap)
            1 -> StatsEngineView(match = activeMatch)
            2 -> TacticalSquadsView(homeTeam = homeTeam, awayTeam = awayTeam)
        }
    }
}

@Composable
fun ScoreboardBanner(
    match: MatchFixtureEntity,
    home: TeamEntity?,
    away: TeamEntity?
) {
    val isLive = match.status == "LIVE"
    val isFt = match.status == "FT"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(PlCardPurple)
            .border(1.dp, if (isLive) PlSleekPurple else PlSleekBorder, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Live Status details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.venue,
                    color = PlTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlErrorRed)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE UPDATE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isFt) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlTextSecondary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FULL TIME",
                            color = PlTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlGold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UPCOMING SCHEDULE",
                            color = PlGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Massive Scoreline Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team logo + name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamBadge(
                        code = home?.code ?: "HM",
                        primaryColorHex = home?.primaryColor ?: "#EF0107",
                        secondaryColorHex = home?.secondaryColor ?: "#FFFFFF",
                        size = 56
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = home?.name ?: "Home Team",
                        color = PlTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isLive || isFt) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${match.homeScore}",
                                color = if (isLive) PlNeonGreen else PlTextPrimary,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = " : ",
                                color = PlTextSecondary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Text(
                                text = "${match.awayScore}",
                                color = if (isLive) PlNeonGreen else PlTextPrimary,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Text(
                            text = "v",
                            color = PlTextSecondary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Expected goals
                    if (isLive || isFt) {
                        Text(
                            text = "xG: ${"%.2f".format(match.xgHome)} - ${"%.2f".format(match.xgAway)}",
                            color = PlNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Away Team logo + name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    TeamBadge(
                        code = away?.code ?: "AW",
                        primaryColorHex = away?.primaryColor ?: "#034694",
                        secondaryColorHex = away?.secondaryColor ?: "#FFFFFF",
                        size = 56
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = away?.name ?: "Away Team",
                        color = PlTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = PlTextSecondary.copy(alpha = 0.08f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Stadium referee indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Referee: Michael Oliver", color = PlTextSecondary, fontSize = 11.sp)
                Text(text = "Attendance: 60,115", color = PlTextSecondary, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun TimelineView(
    events: List<MatchEventEntity>,
    homeTeamId: Int,
    teamsMap: Map<Int, TeamEntity>
) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Info, contentDescription = "No Events", tint = PlTextSecondary, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kickoff has commenced. Tactical commentary and match events stream live here.",
                    color = PlTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events.sortedByDescending { it.minute }) { event ->
            val team = teamsMap[event.teamId]
            val isHome = event.teamId == homeTeamId

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 290.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PlCardPurple)
                        .border(
                            1.dp,
                            when (event.type) {
                                "GOAL" -> PlNeonGreen.copy(alpha = 0.2f)
                                "RED_CARD" -> PlErrorRed.copy(alpha = 0.2f)
                                else -> PlTextSecondary.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (event.type) {
                                            "GOAL" -> PlNeonGreen
                                            "RED_CARD" -> PlErrorRed
                                            "YELLOW_CARD" -> PlGold
                                            else -> PlSkyBlue
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (event.type == "GOAL") "G" else if (event.type.contains("CARD")) "C" else "E",
                                    color = PlDarkPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (event.playerName.isNotEmpty()) event.playerName else event.type,
                                color = PlTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${event.minute}'",
                            color = PlGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.detail,
                        color = PlTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatsEngineView(match: MatchFixtureEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            PlCard {
                Text(
                    text = "Live STATS ENGINE",
                    color = PlNeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Possession
                val posHome = match.possessionHome
                val posAway = match.possessionAway
                ComparisonBar(
                    title = "Possession",
                    homeValueStr = "$posHome%",
                    awayValueStr = "$posAway%",
                    homeProgress = posHome.toFloat() / 100f,
                    awayProgress = posAway.toFloat() / 100f
                )

                // Expected Goals
                val xgH = match.xgHome
                val xgA = match.xgAway
                ComparisonBar(
                    title = "Expected Goals (xG)",
                    homeValueStr = "%.2f".format(xgH),
                    awayValueStr = "%.2f".format(xgA),
                    homeProgress = (xgH / (xgH + xgA + 0.01f)).toFloat(),
                    awayProgress = (xgA / (xgH + xgA + 0.01f)).toFloat()
                )

                // Total Shots
                val shH = match.shotsHome
                val shA = match.shotsAway
                ComparisonBar(
                    title = "Total Shots",
                    homeValueStr = "$shH",
                    awayValueStr = "$shA",
                    homeProgress = (shH.toFloat() / (shH + shA + 0.1f)),
                    awayProgress = (shA.toFloat() / (shH + shA + 0.1f))
                )

                // Shots on Target
                val sotH = match.shotsOnTargetHome
                val sotA = match.shotsOnTargetAway
                ComparisonBar(
                    title = "Shots on Target",
                    homeValueStr = "$sotH",
                    awayValueStr = "$sotA",
                    homeProgress = (sotH.toFloat() / (sotH + sotA + 0.1f)),
                    awayProgress = (sotA.toFloat() / (sotH + sotA + 0.1f))
                )

                // Passes completed
                val passH = match.passesHome
                val passA = match.passesAway
                ComparisonBar(
                    title = "Passes Completed",
                    homeValueStr = "$passH",
                    awayValueStr = "$passA",
                    homeProgress = (passH.toFloat() / (passH + passA + 0.1f)),
                    awayProgress = (passA.toFloat() / (passH + passA + 0.1f))
                )

                // Pass Accuracy
                ComparisonBar(
                    title = "Pass Accuracy",
                    homeValueStr = "${match.passAccuracyHome}%",
                    awayValueStr = "${match.passAccuracyAway}%",
                    homeProgress = match.passAccuracyHome.toFloat() / 100f,
                    awayProgress = match.passAccuracyAway.toFloat() / 100f
                )

                // Fouls committed
                val fH = match.foulsHome
                val fA = match.foulsAway
                ComparisonBar(
                    title = "Fouls Committed",
                    homeValueStr = "$fH",
                    awayValueStr = "$fA",
                    homeProgress = (fH.toFloat() / (fH + fA + 0.1f)),
                    awayProgress = (fA.toFloat() / (fH + fA + 0.1f))
                )
            }
        }
    }
}

@Composable
fun TacticalSquadsView(homeTeam: TeamEntity?, awayTeam: TeamEntity?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Home Squad
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamBadge(code = homeTeam?.code ?: "HM", primaryColorHex = homeTeam?.primaryColor ?: "#EF0107", secondaryColorHex = "#FFFFFF", size = 28)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "${homeTeam?.name} Squad Lineup", color = PlTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "STARTING XI (4-3-3)", color = PlNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "1. Raya (GK)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "4. White (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "2. Saliba (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "6. Gabriel (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "12. Timber (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "8. Ødegaard (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "41. Rice (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "23. Merino (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "7. Saka (FWD)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "29. Havertz (FWD)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "11. Martinelli (FWD)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "SUBSTITUTES", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "32. Neto (GK)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "15. Kiwior (DEF)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "20. Jorginho (MID)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "53. Nwaneri (MID)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "9. Jesus (FWD)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "19. Trossard (FWD)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }

        // Away Squad
        item {
            PlCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TeamBadge(code = awayTeam?.code ?: "AW", primaryColorHex = awayTeam?.primaryColor ?: "#034694", secondaryColorHex = "#FFFFFF", size = 28)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "${awayTeam?.name} Squad Lineup", color = PlTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "STARTING XI (4-2-3-1)", color = PlNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "1. Sanchez (GK)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "2. James (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "29. Fofana (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "6. Colwill (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "3. Cucurella (DEF)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "25. Caicedo (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "45. Lavia (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "11. Madueke (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "20. Palmer (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "7. Neto (MID)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "15. Jackson (FWD)", color = PlTextPrimary, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "SUBSTITUTES", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "12. Jorgensen (GK)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "4. Adarabioyo (DEF)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "8. Fernandez (MID)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "14. Felix (FWD)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "18. Nkunku (FWD)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                        Text(text = "9. Guiu (FWD)", color = PlTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}
