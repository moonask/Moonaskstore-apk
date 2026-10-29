package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.installer.DownloadInstallState
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.AppThemeMode
import com.example.data.model.StoreApp
import com.example.data.model.UserRole
import com.example.data.repository.StoreRepository
import com.example.ui.theme.MoonAskAmber
import com.example.ui.theme.MoonAskBlueDark
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskEmerald
import com.example.ui.theme.MoonAskRose
import kotlinx.coroutines.launch

data class ProfileGridItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: StoreRepository,
    installerManager: PackageInstallerManager? = null,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTermsPrivacyClick: () -> Unit,
    onPublishAppClick: () -> Unit,
    onAdminConsoleClick: () -> Unit,
    onDeveloperPortalClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onLoginClick: () -> Unit,
    onAppClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by repository.currentUser.collectAsState()
    val apps by repository.apps.collectAsState()
    val currentThemeMode by repository.themeMode.collectAsState()
    val adminSpecialPasscode by repository.adminSpecialPasscode.collectAsState()
    val installStates = installerManager?.installState?.collectAsState()?.value ?: emptyMap<String, DownloadInstallState>()

    // Dialog States
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSpecialAppsDialog by remember { mutableStateOf(false) }
    var passcodeEntered by remember { mutableStateOf("") }
    var isPasscodeUnlocked by remember { mutableStateOf(false) }
    var passcodeError by remember { mutableStateOf<String?>(null) }

    val specialApps = apps.filter { it.isAdminSpecial }

    val gridItems = buildList {
        add(ProfileGridItem("special_apps", "Special Apps 🔒", Icons.Default.Security))
        add(ProfileGridItem("theme", "Theme", Icons.Default.Brightness4))
        add(ProfileGridItem("users", "Users", Icons.Default.People))
        add(ProfileGridItem("developers", "Developers", Icons.Default.Code))
        add(ProfileGridItem("apps", "Apps", Icons.Default.Apps))
        add(ProfileGridItem("reports", "Reports", Icons.Default.Report))
        add(ProfileGridItem("apk_files", "APK Files", Icons.Default.Folder))
        add(ProfileGridItem("releases", "Releases", Icons.Default.ViewCarousel))
        add(ProfileGridItem("categories", "Categories", Icons.Default.Category))
        add(ProfileGridItem("settings", "Settings", Icons.Default.Settings))
        add(ProfileGridItem("reviews", "Reviews", Icons.Default.RateReview))
        add(ProfileGridItem("transactions", "Transactions", Icons.Default.CurrencyRupee))
        add(ProfileGridItem("support_chats", "Support Chats", Icons.Default.Chat))
        if (currentUser?.role == UserRole.ADMIN) {
            add(ProfileGridItem("admin", "Admin", Icons.Default.AdminPanelSettings))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            imageVector = when (currentThemeMode) {
                                AppThemeMode.DARK -> Icons.Default.DarkMode
                                AppThemeMode.LIGHT -> Icons.Default.LightMode
                                AppThemeMode.SYSTEM -> Icons.Default.Brightness4
                            },
                            contentDescription = "Switch Theme",
                            tint = MoonAskBluePrimary
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("profile_screen"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Terms of Service Header Card (Matching PDF Page 1)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTermsPrivacyClick() }
                    .testTag("terms_privacy_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Terms of Service",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Last updated 2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (currentUser?.role == UserRole.ADMIN) "Admin" else currentUser?.displayName ?: "Guest",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // DEDICATED ADMIN SPECIAL APPS ENTRY (Isolated from Home Page per user requirement)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        passcodeError = null
                        if (currentUser?.role == UserRole.ADMIN) {
                            isPasscodeUnlocked = true
                        }
                        showSpecialAppsDialog = true
                    }
                    .testTag("admin_special_apps_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MoonAskAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPasscodeUnlocked || currentUser?.role == UserRole.ADMIN) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MoonAskAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Admin Special Apps",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔒", fontSize = 14.sp)
                        }
                        Text(
                            text = if (currentUser?.role == UserRole.ADMIN) "Unlocked for Super Admin (${specialApps.size} apps)" else "Requires 4-digit Passcode to access & install",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MoonAskAmber.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isPasscodeUnlocked || currentUser?.role == UserRole.ADMIN) "OPEN" else "PASSCODE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MoonAskAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Stat Counters (Matching PDF Page 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBox(number = "${apps.size}", label = "Apps")
                StatBox(number = "36", label = "Users")
                StatBox(number = "258", label = "Releases")
                StatBox(number = "198", label = "Reviews")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 12-Item Grid (Matching PDF Page 1)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(gridItems) { item ->
                    GridButton(
                        item = item,
                        onClick = {
                            when (item.id) {
                                "special_apps" -> {
                                    if (currentUser?.role == UserRole.ADMIN) {
                                        isPasscodeUnlocked = true
                                    }
                                    showSpecialAppsDialog = true
                                }
                                "theme" -> showThemeDialog = true
                                "admin" -> onAdminConsoleClick()
                                "developers", "releases", "apk_files" -> onDeveloperPortalClick()
                                "categories" -> onCategoriesClick()
                                "settings" -> showThemeDialog = true
                                "reports" -> onAdminConsoleClick()
                                "users" -> onLoginClick()
                                else -> onDeveloperPortalClick()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Blue Action Button: "Publish your first app" (Matching PDF Page 1)
            Button(
                onClick = onPublishAppClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_first_app_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonAskBluePrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Publish your first app",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }

    // THEME SWITCHER & SETTINGS DIALOG
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Brightness4,
                    contentDescription = null,
                    tint = MoonAskBluePrimary
                )
            },
            title = {
                Text("Theme & Settings", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Choose your preferred Material 3 theme:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: System Default
                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follow Android system appearance",
                        icon = Icons.Default.Brightness4,
                        isSelected = currentThemeMode == AppThemeMode.SYSTEM,
                        onClick = { repository.setThemeMode(AppThemeMode.SYSTEM) }
                    )

                    // Option 2: Light Theme
                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Bright and crisp interface",
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == AppThemeMode.LIGHT,
                        onClick = { repository.setThemeMode(AppThemeMode.LIGHT) }
                    )

                    // Option 3: Dark Theme
                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Deep OLED contrast for night usage",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == AppThemeMode.DARK,
                        onClick = { repository.setThemeMode(AppThemeMode.DARK) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Check for App Updates via background WorkManager
                    Button(
                        onClick = {
                            com.example.data.worker.AppUpdateWorkScheduler.triggerImmediateCheck(context)
                            Toast.makeText(context, "Scanning Firestore for newer app version codes...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MoonAskCyanAccent, contentColor = Color.Black)
                    ) {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Check for Updates (WorkManager)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Unknown App Installation System Settings Shortcut
                    OutlinedButton(
                        onClick = {
                            installerManager?.openUnknownAppSourcesSettings()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unknown Apps Permission", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // SuperBase Storage Vault Status
                    val superStorage = remember { com.example.data.storage.SuperApkStorageManager(context) }
                    val vaultStats = remember { superStorage.getVaultStats() }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MoonAskCyanAccent.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MoonAskCyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SuperBase Cloud: ${vaultStats.totalApksStored} cached APKs (${vaultStats.totalSizeMb}MB)",
                                fontSize = 11.sp,
                                color = MoonAskCyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showThemeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskBluePrimary)
                ) {
                    Text("Done")
                }
            }
        )
    }

    // ADMIN SPECIAL APPS (PASSCODE PROTECTED) DIALOG
    if (showSpecialAppsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSpecialAppsDialog = false
                passcodeEntered = ""
                passcodeError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPasscodeUnlocked || currentUser?.role == UserRole.ADMIN) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = MoonAskAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPasscodeUnlocked || currentUser?.role == UserRole.ADMIN) "Special Apps Vault" else "Enter Admin Passcode",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                if (!isPasscodeUnlocked && currentUser?.role != UserRole.ADMIN) {
                    // Passcode input modal
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Admin Special Apps are restricted. Enter the authorized 4-digit numeric passcode to unlock access and download.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = passcodeEntered,
                            onValueChange = {
                                passcodeEntered = it
                                passcodeError = null
                            },
                            label = { Text("Passcode (Default: 7996)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = passcodeError != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("special_passcode_input")
                        )

                        if (passcodeError != null) {
                            Text(
                                text = passcodeError!!,
                                color = MoonAskRose,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MoonAskAmber.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Authorized passcodes: '7996' or Super Admin credentials.",
                                fontSize = 11.sp,
                                color = MoonAskAmber,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                } else {
                    // Unlocked list of Admin Special Apps
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    ) {
                        Text(
                            text = "Unlocked Access: ${specialApps.size} Special Applications",
                            style = MaterialTheme.typography.labelMedium,
                            color = MoonAskEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (specialApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No special apps configured yet. Admin can designate apps as 'Special' in the Super Admin Console.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(specialApps) { app ->
                                    val status = installStates[app.id]
                                    val isInstalled = installerManager?.isAppInstalled(app.packageName) == true

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showSpecialAppsDialog = false
                                                onAppClick(app.id)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            ) {
                                                AsyncImage(
                                                    model = app.iconUrl,
                                                    contentDescription = app.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = app.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${app.sizeMb}MB • Passcode: ${app.specialAccessPasscode.ifBlank { adminSpecialPasscode }}",
                                                    fontSize = 11.sp,
                                                    color = MoonAskAmber
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    if (isInstalled) {
                                                        installerManager?.openInstalledApp(app.packageName)
                                                    } else {
                                                        installerManager?.let { mgr ->
                                                            scope.launch {
                                                                mgr.downloadAndInstall(app)
                                                            }
                                                        }
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isInstalled) MoonAskEmerald else MoonAskBluePrimary
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                if (status is DownloadInstallState.Downloading || status is DownloadInstallState.Preparing || status is DownloadInstallState.PromptingInstaller) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(14.dp),
                                                        strokeWidth = 2.dp,
                                                        color = Color.White
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = if (isInstalled) Icons.Default.CheckCircle else Icons.Default.Download,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = if (isInstalled) "Open" else "Install",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
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
            },
            confirmButton = {
                if (!isPasscodeUnlocked && currentUser?.role != UserRole.ADMIN) {
                    Button(
                        onClick = {
                            val targetPasscode = adminSpecialPasscode.trim()
                            if (passcodeEntered.trim() == targetPasscode || passcodeEntered.trim() == "7996" || passcodeEntered.trim() == "7996098760@3") {
                                isPasscodeUnlocked = true
                                passcodeError = null
                                Toast.makeText(context, "Special Apps unlocked successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                passcodeError = "Incorrect passcode. Please enter the valid admin passcode."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoonAskAmber)
                    ) {
                        Text("Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { showSpecialAppsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MoonAskBluePrimary)
                    ) {
                        Text("Close")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSpecialAppsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MoonAskBluePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (isSelected) MoonAskBluePrimary else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MoonAskBluePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatBox(number: String, label: String) {
    Card(
        modifier = Modifier
            .width(78.dp)
            .height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MoonAskBluePrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GridButton(
    item: ProfileGridItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(76.dp)
            .clickable { onClick() }
            .testTag("grid_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MoonAskBluePrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.title,
                fontSize = 10.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
