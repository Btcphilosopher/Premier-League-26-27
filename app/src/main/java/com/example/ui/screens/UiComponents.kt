package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MatchFixtureEntity
import com.example.data.database.TeamEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TeamBadge(
    code: String,
    primaryColorHex: String,
    secondaryColorHex: String,
    size: Int = 36,
    borderWidth: Int = 2
) {
    val pColor = try { Color(android.graphics.Color.parseColor(primaryColorHex)) } catch (e: Exception) { PlNeonGreen }
    val sColor = try { Color(android.graphics.Color.parseColor(secondaryColorHex)) } catch (e: Exception) { PlGold }

    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(pColor, sColor.copy(alpha = 0.8f))
                )
            )
            .border(borderWidth.dp, PlTextPrimary.copy(alpha = 0.25f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = code.take(3),
            color = Color.White,
            fontSize = (size * 0.35f).sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PlCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderColor: Color = PlSleekBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(PlCardPurple)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun MatchRowItem(
    match: MatchFixtureEntity,
    homeTeam: TeamEntity?,
    awayTeam: TeamEntity?,
    onClick: () -> Unit
) {
    val isLive = match.status == "LIVE"
    val isFt = match.status == "FT"

    PlCard(
        modifier = Modifier
            .padding(vertical = 6.dp)
            .testTag("match_item_${match.id}"),
        onClick = onClick,
        borderColor = if (isLive) PlSleekPurple else PlSleekBorder
    ) {
        Column {
            // Broadcaster & GW header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "GAMEWEEK ${match.gameweek}",
                        color = PlTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (match.tvBroadcaster.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PlNeonGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = match.tvBroadcaster,
                                color = PlNeonGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                if (isLive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlErrorRed)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (isFt) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PlTextSecondary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FT",
                            color = PlTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    val sdf = SimpleDateFormat("EEE d MMM, HH:mm", Locale.UK)
                    Text(
                        text = sdf.format(Date(match.kickoffTime)),
                        color = PlTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scoreline / Match details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TeamBadge(
                        code = homeTeam?.code ?: "HM",
                        primaryColorHex = homeTeam?.primaryColor ?: "#EF0107",
                        secondaryColorHex = homeTeam?.secondaryColor ?: "#FFFFFF"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = homeTeam?.shortName ?: "Home Team",
                        color = PlTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Scores or Kickoff indicator
                if (isLive || isFt) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${match.homeScore}",
                            color = if (isLive) PlNeonGreen else PlTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = " - ",
                            color = PlTextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Text(
                            text = "${match.awayScore}",
                            color = if (isLive) PlNeonGreen else PlTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    val timeSdf = SimpleDateFormat("HH:mm", Locale.UK)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PlTextSecondary.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = timeSdf.format(Date(match.kickoffTime)),
                            color = PlGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Away Team
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = awayTeam?.shortName ?: "Away Team",
                        color = PlTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TeamBadge(
                        code = awayTeam?.code ?: "AW",
                        primaryColorHex = awayTeam?.primaryColor ?: "#034694",
                        secondaryColorHex = awayTeam?.secondaryColor ?: "#FFFFFF"
                    )
                }
            }

            // FDR and venue detail at footer
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = PlTextSecondary.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.venue,
                    color = PlTextSecondary,
                    fontSize = 11.sp
                )

                // Fixture difficulty indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FDR: ",
                        color = PlTextSecondary,
                        fontSize = 10.sp
                    )
                    FdrDot(rating = match.difficultyRatingHome)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "v",
                        color = PlTextSecondary,
                        fontSize = 8.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    FdrDot(rating = match.difficultyRatingAway)
                }
            }
        }
    }
}

@Composable
fun FdrDot(rating: Int) {
    val color = when (rating) {
        1 -> Color(0xFF00FF85) // Very Easy (Green)
        2 -> Color(0xFF8CEF3F) // Easy (Light Green)
        3 -> Color(0xFFFFDF00) // Moderate (Yellow)
        4 -> Color(0xFFFF7B00) // Hard (Orange)
        else -> Color(0xFFFF1A1A) // Extremely Hard (Red)
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun ComparisonBar(
    title: String,
    homeValueStr: String,
    awayValueStr: String,
    homeProgress: Float,
    awayProgress: Float
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = homeValueStr, color = PlTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = PlTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = awayValueStr, color = PlTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(PlTextSecondary.copy(alpha = 0.15f))
        ) {
            // Home Progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(homeProgress.coerceAtLeast(0.01f))
                    .background(PlNeonGreen)
            )
            // Empty space/divider in between if wanted
            Spacer(modifier = Modifier.width(2.dp).background(PlDarkPurple))
            // Away Progress
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(awayProgress.coerceAtLeast(0.01f))
                    .background(PlSkyBlue)
            )
        }
    }
}
