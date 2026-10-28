package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import com.example.data.model.UserRole
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ai.GeminiStoreAssistant
import com.example.data.installer.DownloadInstallState
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.example.ui.theme.MoonAskAmber
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskEmerald
import com.example.ui.theme.MoonAskRose
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDetailsScreen(
    appId: String,
    repository: StoreRepository,
    installerManager: PackageInstallerManager,
    aiAssistant: GeminiStoreAssistant,
    onBackClick: () -> Unit,
    onInstallApp: (StoreApp) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val apps by repository.apps.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val reviewsMap by repository.reviews.collectAsState()
    val installStates by installerManager.installState.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val adminSpecialPasscode by repository.adminSpecialPasscode.collectAsState()

    val app = apps.find { it.id == appId } ?: return

    val isFavorite = favorites.contains(app.id)
    val appReviews = reviewsMap[app.id] ?: emptyList()
    val installState = installStates[app.id] ?: installerManager.getStateForApp(app.id)
    val isInstalled = installerManager.isAppInstalled(app.packageName)

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("Misleading content") }
    var reportDescription by remember { mutableStateOf("") }

    var isAiAuditing by remember { mutableStateOf(false) }
    var aiAuditReport by remember { mutableStateOf<String?>(null) }

    // Admin Special App Passcode state
    var showPasscodeDialog by remember { mutableStateOf(false) }
    var enteredPasscode by remember { mutableStateOf("") }
    var passcodeError by remember { mutableStateOf<String?>(null) }
    var isPasscodeUnlocked by remember { mutableStateOf(false) }

    fun onSafeInstallClick() {
        if (app.isAdminSpecial && !isPasscodeUnlocked && currentUser?.role != UserRole.ADMIN) {
            showPasscodeDialog = true
        } else {
            onInstallApp(app)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { repository.toggleFavorite(app.id) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MoonAskRose else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { installerManager.shareAppViaGmail(app) }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Share via Gmail",
                            tint = MoonAskBluePrimary
                        )
                    }
                    IconButton(onClick = { installerManager.shareAppNative(app) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Native Share"
                        )
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Report App",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("app_details_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Icon, Name, Developer, Category
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = app.iconUrl,
                            contentDescription = app.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (app.isAdminSpecial) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MoonAskAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "SPECIAL",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MoonAskAmber
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = app.developerName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Developer",
                                tint = MoonAskCyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${app.categoryId.replaceFirstChar { it.uppercase() }} • Contains Ads (None)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Admin Special App Callout
            if (app.isAdminSpecial) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MoonAskAmber.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MoonAskAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Admin Special Application",
                                    fontWeight = FontWeight.Bold,
                                    color = MoonAskAmber,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (currentUser?.role == UserRole.ADMIN) "Super Admin logged in: Direct privileged access enabled." else "Restricted release: Enter Admin Special Passcode to install.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format("%.1f", app.rating),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MoonAskAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${app.reviewCount} reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(modifier = Modifier.height(28.dp).width(1.dp).background(MaterialTheme.colorScheme.outline))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${app.sizeMb} MB",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Download Size",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(modifier = Modifier.height(28.dp).width(1.dp).background(MaterialTheme.colorScheme.outline))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${app.downloadCount / 1000}k+",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Downloads",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(modifier = Modifier.height(28.dp).width(1.dp).background(MaterialTheme.colorScheme.outline))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "v${app.versionName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Version",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Download & Install Action Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (installState) {
                        is DownloadInstallState.Downloading -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Downloading APK (${(installState.progress * 100).toInt()}%)...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${installState.downloadedBytes / (1024 * 1024)}MB / ${installState.totalBytes / (1024 * 1024)}MB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { installState.progress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = MoonAskCyanAccent
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { installerManager.cancelDownload(app.id) },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                        is DownloadInstallState.Preparing, is DownloadInstallState.VerifyingChecksum, is DownloadInstallState.PromptingInstaller -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    if (installState is DownloadInstallState.VerifyingChecksum) "Verifying SHA-256..." else "Launching Android Installer..."
                                )
                            }
                        }
                        else -> {
                            if (isInstalled || installState is DownloadInstallState.Installed) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { installerManager.openInstalledApp(app.packageName) },
                                        modifier = Modifier.weight(1f).height(50.dp).testTag("details_open_button"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoonAskEmerald)
                                    ) {
                                        Icon(imageVector = Icons.Default.Launch, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open App", fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { onSafeInstallClick() },
                                        modifier = Modifier.height(50.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Reinstall")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onSafeInstallClick() },
                                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("details_install_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (app.isAdminSpecial) MoonAskAmber else MoonAskBluePrimary,
                                        contentColor = if (app.isAdminSpecial) Color.Black else Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (app.isAdminSpecial) Icons.Default.Lock else Icons.Default.Download,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (app.isAdminSpecial) "Unlock & Install (${app.sizeMb} MB)" else "Install (${app.sizeMb} MB)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gmail & Native Share Callout Banner
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MoonAskCyanAccent.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskCyanAccent.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MoonAskCyanAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share via Gmail or Native Share",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Send a secure listing link and direct APK download URL with step-by-step install guide to friends.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { installerManager.shareAppViaGmail(app) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MoonAskCyanAccent
                            )
                        }
                    }
                }
            }

            // Gemini AI Permissions Safety Inspector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MoonAskEmerald
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Package Security & SHA-256",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = {
                                    isAiAuditing = true
                                    scope.launch {
                                        aiAuditReport = aiAssistant.analyzeApkPermissions(app.name, app.permissions)
                                        isAiAuditing = false
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoonAskCyanAccent.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MoonAskCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAiAuditing) "Scanning..." else "Gemini Audit",
                                    color = MoonAskCyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SHA-256 Checksum Display
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SHA-256: ${app.sha256Checksum.take(18)}...${app.sha256Checksum.takeLast(6)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MoonAskEmerald.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MoonAskEmerald
                                    )
                                }
                            }
                        }

                        // AI Audit Output
                        if (aiAuditReport != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskCyanAccent.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = aiAuditReport!!,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Declared Permissions (${app.permissions.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            app.permissions.forEach { perm ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = perm,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Screenshots Gallery
            if (app.screenshots.isNotEmpty()) {
                item {
                    Text(
                        text = "Screenshots Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(app.screenshots) { screenshotUrl ->
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = screenshotUrl,
                                    contentDescription = "Screenshot",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // What's New
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What's New in v${app.versionName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = app.whatsNew,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // About this app
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "About this application",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = app.fullDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Ratings and Reviews
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ratings & Reviews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text("Write a Review")
                    }
                }

                if (appReviews.isEmpty()) {
                    Text(
                        text = "No reviews yet. Be the first to share your experience!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        appReviews.forEach { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = review.authorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = review.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        repeat(review.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = MoonAskAmber,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = review.comment,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Write a Review Dialog
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Review ${app.name}") },
            text = {
                Column {
                    Text("Select Rating:")
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= reviewRating) MoonAskAmber else Color.Gray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { reviewRating = star }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Your Review") },
                        placeholder = { Text("What did you like or dislike about this app?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewComment.isNotBlank()) {
                            repository.addReview(app.id, reviewRating, reviewComment)
                            showReviewDialog = false
                            reviewComment = ""
                            Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report App Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report ${app.name}") },
            text = {
                Column {
                    Text("Select reason:")
                    listOf("Suspicious APK / Malware", "Misleading description", "Copyright infringement", "Broken download").forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = reportReason == reason,
                                onClick = { reportReason = reason }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(reason, fontSize = 13.sp)
                        }
                    }
                    OutlinedTextField(
                        value = reportDescription,
                        onValueChange = { reportDescription = it },
                        label = { Text("Additional details") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.submitReport(app.id, app.name, reportReason, reportDescription)
                        showReportDialog = false
                        reportDescription = ""
                        Toast.makeText(context, "Report submitted for moderation review", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Admin Special App Passcode Verification Dialog
    if (showPasscodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasscodeDialog = false
                passcodeError = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MoonAskAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Special App")
                }
            },
            text = {
                Column {
                    Text(
                        text = "This application is restricted by MoonAsk Super Admin. Please enter the special access passcode to install:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredPasscode,
                        onValueChange = { enteredPasscode = it },
                        label = { Text("Passcode (e.g. 7996)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (passcodeError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = passcodeError!!, color = MoonAskRose, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val correctPasscode = app.specialAccessPasscode.ifBlank { adminSpecialPasscode }
                        if (enteredPasscode.trim() == correctPasscode.trim()) {
                            isPasscodeUnlocked = true
                            showPasscodeDialog = false
                            passcodeError = null
                            Toast.makeText(context, "Passcode verified! Access granted.", Toast.LENGTH_SHORT).show()
                            onInstallApp(app)
                        } else {
                            passcodeError = "Incorrect passcode. Contact Super Admin (uddinsknijam102@gmail.com)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskAmber, contentColor = Color.Black)
                ) {
                    Text("Unlock & Install", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasscodeDialog = false
                    passcodeError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
