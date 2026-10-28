package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.GeminiStoreAssistant
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.example.ui.components.GeminiAiAssistantDialog
import com.example.ui.screens.AppDetailsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DeveloperPortalScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.screens.TermsPrivacyScreen
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    APPS,
    REELS,
    PROFILE,
    DETAILS,
    SEARCH,
    DEVELOPER_PORTAL,
    ADMIN_CONSOLE,
    TERMS_PRIVACY,
    LOGIN
}

@Composable
fun MoonAskApp(
    repository: StoreRepository,
    installerManager: PackageInstallerManager,
    aiAssistant: GeminiStoreAssistant
) {
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedAppId by remember { mutableStateOf<String?>(null) }
    var showAiAssistantDialog by remember { mutableStateOf(false) }

    // Screen stack for back navigation
    var screenStack by remember { mutableStateOf(listOf(Screen.HOME)) }

    fun navigateTo(screen: Screen, appId: String? = null) {
        if (appId != null) selectedAppId = appId
        screenStack = screenStack + screen
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            currentScreen = newStack.last()
        } else {
            currentScreen = Screen.HOME
        }
    }

    BackHandler(enabled = screenStack.size > 1 || currentScreen != Screen.HOME) {
        navigateBack()
    }

    val isTopLevelScreen = currentScreen in listOf(Screen.HOME, Screen.APPS, Screen.REELS, Screen.PROFILE)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (isTopLevelScreen) {
                Box {
                    NavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("main_bottom_nav"),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        // 1. Home
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = {
                                currentScreen = Screen.HOME
                                screenStack = listOf(Screen.HOME)
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MoonAskBluePrimary,
                                selectedTextColor = MoonAskBluePrimary,
                                indicatorColor = MoonAskBluePrimary.copy(alpha = 0.15f)
                            )
                        )

                        // 2. Apps
                        NavigationBarItem(
                            selected = currentScreen == Screen.APPS,
                            onClick = {
                                currentScreen = Screen.APPS
                                screenStack = listOf(Screen.HOME, Screen.APPS)
                            },
                            icon = { Icon(Icons.Default.Apps, contentDescription = "Apps") },
                            label = { Text("Apps", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MoonAskBluePrimary,
                                selectedTextColor = MoonAskBluePrimary,
                                indicatorColor = MoonAskBluePrimary.copy(alpha = 0.15f)
                            )
                        )

                        // Center Spacer for Floating (+) Button (Matching PDF Design)
                        NavigationBarItem(
                            selected = false,
                            onClick = { navigateTo(Screen.DEVELOPER_PORTAL) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MoonAskBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Publish App",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            },
                            label = { Text("", fontSize = 0.sp) }
                        )

                        // 4. Reels
                        NavigationBarItem(
                            selected = currentScreen == Screen.REELS,
                            onClick = {
                                currentScreen = Screen.REELS
                                screenStack = listOf(Screen.HOME, Screen.REELS)
                            },
                            icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Reels") },
                            label = { Text("Reels", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MoonAskBluePrimary,
                                selectedTextColor = MoonAskBluePrimary,
                                indicatorColor = MoonAskBluePrimary.copy(alpha = 0.15f)
                            )
                        )

                        // 5. Profile
                        NavigationBarItem(
                            selected = currentScreen == Screen.PROFILE,
                            onClick = {
                                currentScreen = Screen.PROFILE
                                screenStack = listOf(Screen.HOME, Screen.PROFILE)
                            },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MoonAskBluePrimary,
                                selectedTextColor = MoonAskBluePrimary,
                                indicatorColor = MoonAskBluePrimary.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    repository = repository,
                    installerManager = installerManager,
                    onAppClick = { appId -> navigateTo(Screen.DETAILS, appId) },
                    onSearchClick = { navigateTo(Screen.SEARCH) },
                    onAiAssistantClick = { showAiAssistantDialog = true },
                    onProfileClick = { navigateTo(Screen.PROFILE) },
                    onInstallApp = { app ->
                        scope.launch {
                            installerManager.downloadAndInstall(app)
                        }
                    }
                )

                Screen.APPS -> CategoriesScreen(
                    repository = repository,
                    onBackClick = { navigateBack() },
                    onCategoryClick = { navigateTo(Screen.SEARCH) }
                )

                Screen.REELS -> ReelsScreen(
                    repository = repository,
                    onAppClick = { appId -> navigateTo(Screen.DETAILS, appId) }
                )

                Screen.PROFILE -> ProfileScreen(
                    repository = repository,
                    onBackClick = { navigateBack() },
                    onSearchClick = { navigateTo(Screen.SEARCH) },
                    onTermsPrivacyClick = { navigateTo(Screen.TERMS_PRIVACY) },
                    onPublishAppClick = { navigateTo(Screen.DEVELOPER_PORTAL) },
                    onAdminConsoleClick = { navigateTo(Screen.ADMIN_CONSOLE) },
                    onDeveloperPortalClick = { navigateTo(Screen.DEVELOPER_PORTAL) },
                    onCategoriesClick = { navigateTo(Screen.APPS) },
                    onLoginClick = { navigateTo(Screen.LOGIN) }
                )

                Screen.DETAILS -> {
                    selectedAppId?.let { id ->
                        AppDetailsScreen(
                            appId = id,
                            repository = repository,
                            installerManager = installerManager,
                            aiAssistant = aiAssistant,
                            onBackClick = { navigateBack() },
                            onInstallApp = { app ->
                                scope.launch {
                                    installerManager.downloadAndInstall(app)
                                }
                            }
                        )
                    }
                }

                Screen.SEARCH -> SearchScreen(
                    repository = repository,
                    installerManager = installerManager,
                    onAppClick = { appId -> navigateTo(Screen.DETAILS, appId) },
                    onBackClick = { navigateBack() },
                    onAiAssistantClick = { showAiAssistantDialog = true },
                    onInstallApp = { app ->
                        scope.launch {
                            installerManager.downloadAndInstall(app)
                        }
                    }
                )

                Screen.DEVELOPER_PORTAL -> DeveloperPortalScreen(
                    repository = repository,
                    onBackClick = { navigateBack() },
                    onAppClick = { appId -> navigateTo(Screen.DETAILS, appId) }
                )

                Screen.ADMIN_CONSOLE -> SuperAdminScreen(
                    repository = repository,
                    onBackClick = { navigateBack() }
                )

                Screen.TERMS_PRIVACY -> TermsPrivacyScreen(
                    onBackClick = { navigateBack() },
                    onSearchClick = { navigateTo(Screen.SEARCH) }
                )

                Screen.LOGIN -> LoginScreen(
                    repository = repository,
                    onBackClick = { navigateBack() },
                    onLoginSuccess = { navigateBack() }
                )
            }

            // Gemini AI Store Assistant Dialog
            if (showAiAssistantDialog) {
                GeminiAiAssistantDialog(
                    repository = repository,
                    assistant = aiAssistant,
                    onDismiss = { showAiAssistantDialog = false },
                    onAppClick = { appId ->
                        showAiAssistantDialog = false
                        navigateTo(Screen.DETAILS, appId)
                    }
                )
            }
        }
    }
}
