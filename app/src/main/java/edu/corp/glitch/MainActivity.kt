package edu.corp.glitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import edu.corp.glitch.data.preferences.UserPreferences
import edu.corp.glitch.ui.navigation.AuthNavGraph
import edu.corp.glitch.ui.screens.live.LiveScreen
import edu.corp.glitch.ui.screens.profile.ProfileScreen
import edu.corp.glitch.ui.screens.search.SearchScreen
import edu.corp.glitch.ui.screens.settings.SettingsScreen
import edu.corp.glitch.ui.screens.stream.StreamScreen
import edu.corp.glitch.ui.theme.GlitchTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val isDarkMode by userPreferences.isDarkMode.collectAsState(initial = false)
            
            // Update system bars based on theme
            LaunchedEffect(isDarkMode) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    },
                    navigationBarStyle = if (isDarkMode) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }
            
            GlitchTheme(darkTheme = isDarkMode) {
                GlitchApp(userPreferences)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlitchApp(userPreferences: UserPreferences) {
    val authToken by userPreferences.authToken.collectAsState(initial = null)
    val isLoggedIn = authToken != null
    
    if (!isLoggedIn) {
        val authNavController = rememberNavController()
        AuthNavGraph(
            navController = authNavController,
            onLoginSuccess = { /* Will trigger recomposition */ }
        )
    } else {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LiveTv, "Live") },
                    label = { Text("Live") },
                    selected = currentDestination?.hierarchy?.any { it.route == "live" } == true,
                    onClick = { navController.navigate("live") }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, "Search") },
                    label = { Text("Search") },
                    selected = currentDestination?.hierarchy?.any { it.route == "search" } == true,
                    onClick = { navController.navigate("search") }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = { navController.navigate("profile") }
                )
                
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "live",
            modifier = Modifier.padding(padding)
        ) {
            composable("live") {
              LiveScreen(
                  onStreamClick = { username ->
                      navController.navigate("stream/$username")
                  }
              )
            }

            composable("stream/{username}") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username")
                if (username != null) {
                    StreamScreen(
                        username = username,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
            
            composable("search") {
                SearchScreen(
                    onStreamClick = { username ->
                        navController.navigate("stream/$username")
                    },
                    onUserClick = { username ->
                        navController.navigate("profile/$username")
                    }
                )
            }
            
            composable("profile") {
                ProfileScreen(
                    username = null,
                    onNavigateToOwnProfile = { navController.navigate("profile") }
                )
            }
            
            composable("profile/{username}") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username")
                ProfileScreen(
                    username = username,
                    onNavigateToOwnProfile = { navController.navigate("profile") }
                )
            }
            
            composable("settings") {
                SettingsScreen(
                    onLogout = { /* Will trigger recomposition */ }
                )
            }
        }
    }
}
