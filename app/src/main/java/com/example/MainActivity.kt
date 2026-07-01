package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FootballViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FootballViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: FootballViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    // Handle system back press
    BackHandler(enabled = currentScreen !is Screen.Dashboard) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(PlDarkPurple),
        bottomBar = {
            // Immersive view: hide bottom bar on detail screens to focus on broadcast match details
            if (currentScreen !is Screen.MatchDetail && currentScreen !is Screen.TeamDetail) {
                PlBottomNavigationBar(
                    currentScreen = currentScreen,
                    onTabSelected = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PlDarkPurple)
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is Screen.Dashboard -> DashboardScreen(viewModel = viewModel, teamsList = teamsList)
                is Screen.Fixtures -> FixturesScreen(viewModel = viewModel, teamsList = teamsList)
                is Screen.Standings -> StandingsScreen(viewModel = viewModel, teamsList = teamsList)
                is Screen.Clubhouse -> ClubhouseScreen(viewModel = viewModel, teamsList = teamsList)
                is Screen.AiAssistant -> AiAdminScreen(viewModel = viewModel, teamsList = teamsList)
                is Screen.MatchDetail -> LiveMatchCentre(viewModel = viewModel, teamsList = teamsList)
                is Screen.TeamDetail -> ClubhouseScreen(viewModel = viewModel, teamsList = teamsList)
            }
        }
    }
}

@Composable
fun PlBottomNavigationBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        NavigationTabItem("HOME", Icons.Default.Home, Screen.Dashboard),
        NavigationTabItem("FIXTURES", Icons.Default.DateRange, Screen.Fixtures),
        NavigationTabItem("STANDINGS", Icons.Default.List, Screen.Standings),
        NavigationTabItem("CLUBHOUSE", Icons.Default.Star, Screen.Clubhouse),
        NavigationTabItem("AI & ADMIN", Icons.Default.Face, Screen.AiAssistant)
    )

    NavigationBar(
        containerColor = PlCardPurple,
        contentColor = PlSleekPurple,
        tonalElevation = 8.dp,
        modifier = Modifier
            .testTag("pl_bottom_nav_bar")
            .border(0.5.dp, PlSleekBorder, RoundedCornerShape(0.dp))
    ) {
        items.forEach { item ->
            val isSelected = currentScreen::class == item.screen::class

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PlSleekPurple else PlTextSecondary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Bold,
                        color = if (isSelected) PlSleekPurple else PlTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PlSleekPurple.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_tab_${item.label.lowercase()}")
            )
        }
    }
}

data class NavigationTabItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)
