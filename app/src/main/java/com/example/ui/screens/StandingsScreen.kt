package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.TeamStanding
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

@Composable
fun StandingsScreen(
    viewModel: FootballViewModel,
    teamsList: List<TeamEntity>,
    modifier: Modifier = Modifier
) {
    val standings by viewModel.standings.collectAsState()
    val allFixtures by viewModel.fixtures.collectAsState()

    var selectedSplitTab by remember { mutableStateOf(0) } // 0 = Overall, 1 = Home, 2 = Away

    // Dynamically calculate splits if tab is 1 or 2
    val displayedStandings = remember(standings, allFixtures, selectedSplitTab, teamsList) {
        if (selectedSplitTab == 0) {
            standings
        } else {
            calculateSplitStandings(teamsList, allFixtures, isHomeOnly = selectedSplitTab == 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("standings_screen")
    ) {
        // Upper header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "LEAGUE STANDINGS",
                color = PlSleekPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Live Barclays Table",
                color = PlTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Custom Split Tab: Overall, Home split, Away split
        TabRow(
            selectedTabIndex = selectedSplitTab,
            containerColor = PlCardPurple,
            contentColor = PlSleekPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSplitTab]),
                    color = PlSleekPurple
                )
            },
            divider = { Divider(color = PlTextSecondary.copy(alpha = 0.1f)) }
        ) {
            Tab(
                selected = selectedSplitTab == 0,
                onClick = { selectedSplitTab = 0 },
                text = { Text("OVERALL", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSplitTab == 1,
                onClick = { selectedSplitTab = 1 },
                text = { Text("HOME SPLIT", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedSplitTab == 2,
                onClick = { selectedSplitTab = 2 },
                text = { Text("AWAY SPLIT", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PlDarkPurple)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#  Club",
                color = PlTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(2.5f)
            )
            Row(
                modifier = Modifier.weight(3.5f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "P", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = "W", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                Text(text = "D", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                Text(text = "L", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                Text(text = "GD", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                Text(text = "PTS", color = PlTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
            }
            Text(
                text = "Form",
                color = PlTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(90.dp),
                textAlign = TextAlign.Center
            )
        }

        // Table Rows
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            itemsIndexed(displayedStandings) { index, row ->
                val qualificationColor = when {
                    index < 4 -> PlNeonGreen // Champions League (1-4)
                    index == 4 -> PlSkyBlue  // Europa League (5)
                    index == 5 -> PlGold     // Conference League (6)
                    index >= 17 -> PlErrorRed // Relegation (18-20)
                    else -> Color.Transparent
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.TeamDetail(row.teamId)) }
                        .background(if (index % 2 == 0) PlCardPurple.copy(alpha = 0.3f) else PlDarkPurple)
                        .border(
                            width = if (qualificationColor != Color.Transparent) 1.5.dp else 0.dp,
                            color = qualificationColor.copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Position rank + indicator line
                    Row(
                        modifier = Modifier.weight(2.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color border tag on side
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(qualificationColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${index + 1}",
                            color = if (qualificationColor != Color.Transparent && qualificationColor != PlErrorRed) qualificationColor else PlTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(18.dp)
                        )
                        TeamBadge(
                            code = row.teamCode,
                            primaryColorHex = row.primaryColor,
                            secondaryColorHex = "#FFFFFF",
                            size = 22,
                            borderWidth = 1
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = row.teamName,
                            color = PlTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Numeric stats
                    Row(
                        modifier = Modifier.weight(3.5f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "${row.gamesPlayed}", color = PlTextPrimary, fontSize = 13.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        Text(text = "${row.won}", color = PlTextPrimary, fontSize = 13.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                        Text(text = "${row.drawn}", color = PlTextPrimary, fontSize = 13.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                        Text(text = "${row.lost}", color = PlTextPrimary, fontSize = 13.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                        Text(
                            text = "${if (row.goalDifference > 0) "+" else ""}${row.goalDifference}",
                            color = if (row.goalDifference > 0) PlNeonGreen else if (row.goalDifference < 0) PlErrorRed else PlTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.width(28.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${row.points}",
                            color = PlGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // Form Guideline (Last 5 Games)
                    Row(
                        modifier = Modifier.width(90.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (row.form.isEmpty()) {
                            Text(text = "-", color = PlTextSecondary, fontSize = 11.sp)
                        } else {
                            row.form.takeLast(5).forEach { f ->
                                Spacer(modifier = Modifier.width(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (f) {
                                                "W" -> PlNeonGreen
                                                "D" -> PlTextSecondary
                                                else -> PlErrorRed
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = f,
                                        color = PlDarkPurple,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Zones Legend Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PlCardPurple)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PlNeonGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "UCL", color = PlTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PlSkyBlue))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "UEL", color = PlTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PlGold))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "UECL", color = PlTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PlErrorRed))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Relegation", color = PlTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Split calculations for Home vs Away standing snapshot
private fun calculateSplitStandings(
    teams: List<TeamEntity>,
    fixtures: List<MatchFixtureEntity>,
    isHomeOnly: Boolean
): List<TeamStanding> {
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

    val sortedFixtures = fixtures.sortedBy { it.kickoffTime }

    for (fixture in sortedFixtures) {
        if (fixture.status == "SCHEDULED") continue

        val homeId = fixture.homeTeamId
        val awayId = fixture.awayTeamId
        val homeScore = fixture.homeScore
        val awayScore = fixture.awayScore

        if (isHomeOnly) {
            val standing = standingsMap[homeId] ?: continue
            val won = if (homeScore > awayScore) 1 else 0
            val drawn = if (homeScore == awayScore) 1 else 0
            val lost = if (homeScore < awayScore) 1 else 0
            val points = won * 3 + drawn * 1
            val updatedForm = (standing.form + when {
                homeScore > awayScore -> "W"
                homeScore == awayScore -> "D"
                else -> "L"
            }).takeLast(5)

            standingsMap[homeId] = standing.copy(
                gamesPlayed = standing.gamesPlayed + 1,
                won = standing.won + won,
                drawn = standing.drawn + drawn,
                lost = standing.lost + lost,
                goalsFor = standing.goalsFor + homeScore,
                goalsAgainst = standing.goalsAgainst + awayScore,
                goalDifference = standing.goalDifference + (homeScore - awayScore),
                points = standing.points + points,
                form = updatedForm
            )
        } else {
            // Away Only
            val standing = standingsMap[awayId] ?: continue
            val won = if (awayScore > homeScore) 1 else 0
            val drawn = if (awayScore == homeScore) 1 else 0
            val lost = if (awayScore < homeScore) 1 else 0
            val points = won * 3 + drawn * 1
            val updatedForm = (standing.form + when {
                awayScore > homeScore -> "W"
                awayScore == homeScore -> "D"
                else -> "L"
            }).takeLast(5)

            standingsMap[awayId] = standing.copy(
                gamesPlayed = standing.gamesPlayed + 1,
                won = standing.won + won,
                drawn = standing.drawn + drawn,
                lost = standing.lost + lost,
                goalsFor = standing.goalsFor + awayScore,
                goalsAgainst = standing.goalsAgainst + homeScore,
                goalDifference = standing.goalDifference + (awayScore - homeScore),
                points = standing.points + points,
                form = updatedForm
            )
        }
    }

    return standingsMap.values.sortedWith(
        compareByDescending<TeamStanding> { it.points }
            .thenByDescending { it.goalDifference }
            .thenByDescending { it.goalsFor }
            .thenBy { it.teamName }
    )
}
