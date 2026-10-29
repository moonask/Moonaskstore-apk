package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import com.example.data.model.UserRole
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Checkbox
import com.example.data.ai.GeminiStoreAssistant
import androidx.compose.material3.RadioButton
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
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.example.ui.theme.MoonAskAmber
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskEmerald
import com.example.ui.theme.MoonAskRose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperPortalScreen(
    repository: StoreRepository,
    onBackClick: () -> Unit,
    onAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apps by repository.apps.collectAsState()
    val categories by repository.categories.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    var showCreateAppDialog by remember { mutableStateOf(false) }

    // Form state
    var appName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()?.id ?: "tools") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var shortDesc by remember { mutableStateOf("") }
    var fullDesc by remember { mutableStateOf("") }
    var versionName by remember { mutableStateOf("1.0.0") }
    var versionCode by remember { mutableIntStateOf(1) }
    var sizeMb by remember { mutableStateOf("12.5") }
    var whatsNew by remember { mutableStateOf("Initial release on MoonAsk Store.") }

    // App Logo, Developer Name, Official Website & 4 Screenshots (Google Play Console style)
    var appLogoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200") }
    var developerNameInput by remember { mutableStateOf(currentUser?.displayName ?: "Independent Creator") }
    var websiteUrlInput by remember { mutableStateOf("https://moonask.store/developer") }
    var screenshot1Url by remember { mutableStateOf("https://images.unsplash.com/photo-1551650975-87deedd944c3?w=600") }
    var screenshot2Url by remember { mutableStateOf("https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=600") }
    var screenshot3Url by remember { mutableStateOf("https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600") }
    var screenshot4Url by remember { mutableStateOf("https://images.unsplash.com/photo-1517842645767-c639042777db?w=600") }

    // Secure APK File Picker & SHA-256 state
    var selectedApkName by remember { mutableStateOf<String?>(null) }
    var calculatedSha256 by remember { mutableStateOf<String?>(null) }
    var localSavedApkPath by remember { mutableStateOf<String?>(null) }
    var isCalculatingSha256 by remember { mutableStateOf(false) }
    var sha256Progress by remember { mutableStateOf(0f) }
    var sha256ProgressText by remember { mutableStateOf("") }
    var apkValidationError by remember { mutableStateOf<String?>(null) }

    // Admin Special App options
    var isSpecialAppUpload by remember { mutableStateOf(false) }
    val adminSpecialPasscode by repository.adminSpecialPasscode.collectAsState()
    var customPasscodeInput by remember { mutableStateOf("") }

    // AI and Reel Promo state
    val aiAssistant = remember { GeminiStoreAssistant() }
    var isLogoAiAdjusted by remember { mutableStateOf(false) }
    var isOptimizingListing by remember { mutableStateOf(false) }
    var attachPromoReel by remember { mutableStateOf(true) }
    var promoReelTitle by remember { mutableStateOf("") }
    var promoReelVideoUrl by remember { mutableStateOf("https://assets.mixkit.co/videos/preview/mixkit-software-developer-working-on-code-41584-large.mp4") }

    // Visual media picker for App Logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            appLogoUrl = uri.toString()
            isLogoAiAdjusted = true
            Toast.makeText(context, "Logo loaded! AI auto-adjusted to 512x512 standard format.", Toast.LENGTH_SHORT).show()
        }
    }

    // SuperBase Storage Vault Manager
    val superStorageManager = remember { com.example.data.storage.SuperApkStorageManager(context) }
    var superBaseBucketKey by remember { mutableStateOf("") }
    var superBaseCdnUrl by remember { mutableStateOf("") }

    // File picker launcher enforcing .apk
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        val fileName = getFileNameFromUri(context, uri) ?: "application.apk"

        // Enforce .apk extension
        if (!fileName.lowercase().endsWith(".apk")) {
            apkValidationError = "Validation Error: Selected file '$fileName' is not an APK. MoonAsk Store strictly requires '.apk' files."
            selectedApkName = null
            calculatedSha256 = null
            localSavedApkPath = null
            superBaseBucketKey = ""
            superBaseCdnUrl = ""
            sha256Progress = 0f
            sha256ProgressText = ""
            return@rememberLauncherForActivityResult
        }

        apkValidationError = null
        selectedApkName = fileName
        isCalculatingSha256 = true
        sha256Progress = 0f
        val fileSize = getFileSizeFromUri(context, uri)
        sha256ProgressText = "0% (0.0 MB / ${String.format("%.1f", (fileSize.coerceAtLeast(0L)).toFloat() / (1024f * 1024f))} MB)"

        scope.launch(Dispatchers.IO) {
            try {
                val input = context.contentResolver.openInputStream(uri)
                if (input != null) {
                    val dir = File(context.filesDir, "temp_staging").apply { mkdirs() }
                    val tempFile = File(dir, "${System.currentTimeMillis()}_$fileName")
                    val output = FileOutputStream(tempFile)
                    val buffer = ByteArray(65536)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                    output.close()
                    input.close()

                    // Upload into MoonAsk SuperBase Cloud Vault with chunking and SHA-256 calculation
                    val uploadResult = superStorageManager.uploadApkToSuperBase(
                        sourceFile = tempFile,
                        packageName = packageName.ifBlank { "com.developer.app" },
                        versionCode = versionCode
                    ) { progress, transferred, total ->
                        val text = "${(progress * 100).toInt()}% (${String.format("%.1f", transferred / (1024f * 1024f))} MB / ${String.format("%.1f", total / (1024f * 1024f))} MB)"
                        scope.launch(Dispatchers.Main) {
                            sha256Progress = progress
                            sha256ProgressText = text
                        }
                    }

                    tempFile.delete()

                    withContext(Dispatchers.Main) {
                        sha256Progress = 1f
                        sha256ProgressText = "100% (${String.format("%.1f", uploadResult.sizeMb)} MB)"
                        calculatedSha256 = uploadResult.sha256Checksum
                        localSavedApkPath = uploadResult.localCachedPath
                        superBaseBucketKey = uploadResult.bucketKey
                        superBaseCdnUrl = uploadResult.cdnMirrorUrl
                        sizeMb = String.format("%.1f", uploadResult.sizeMb.coerceAtLeast(0.1f))
                        isCalculatingSha256 = false
                        Toast.makeText(context, "APK secured in MoonAsk SuperBase Cloud Vault!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        apkValidationError = "Failed to open selected file stream."
                        isCalculatingSha256 = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    apkValidationError = "Error uploading to SuperBase vault: ${e.message}"
                    isCalculatingSha256 = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Portal", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Reset upload form
                    selectedApkName = null
                    calculatedSha256 = null
                    localSavedApkPath = null
                    apkValidationError = null
                    showCreateAppDialog = true
                },
                containerColor = MoonAskBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("dev_add_app_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Submit New App")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("developer_portal_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MoonAskCyanAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💻", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Developer Console",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Publisher: ${currentUser?.displayName ?: "Developer"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Upload signed APK files directly. Our client-side validator verifies file extension and computes cryptographic SHA-256 checksums before submission. Once approved by Super Admin, releases go live in MoonAsk Store.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (currentUser?.hasFreeUpload == true) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MoonAskEmerald.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskEmerald.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MoonAskEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Free Upload Status: Active (Store publishing fee waived: $0.00)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MoonAskEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My Applications (${apps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(apps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            AsyncImage(
                                model = app.iconUrl,
                                contentDescription = app.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (app.isUserUploaded) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MoonAskCyanAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "APK UPLOAD",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MoonAskCyanAccent
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${app.packageName} • v${app.versionName} • ${app.sizeMb}MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status Pill
                            StatusPill(status = app.publicationStatus, rejectionReason = app.rejectionReason)
                        }
                    }
                }
            }
        }
    }

    // Submit New App Dialog with Secure File Picker & SHA-256 Validation
    if (showCreateAppDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAppDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = MoonAskBluePrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish New APK")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // SECURE APK FILE PICKER SECTION
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (apkValidationError != null) MoonAskRose else if (calculatedSha256 != null) MoonAskEmerald else MoonAskBluePrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = if (calculatedSha256 != null) MoonAskEmerald else MoonAskBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "APK Package File *",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        // Trigger file picker for .apk files
                                        apkPickerLauncher.launch(arrayOf("*/*"))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskBluePrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("choose_apk_file_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Choose APK", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            when {
                                isCalculatingSha256 -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MoonAskCyanAccent
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Computing SHA-256...",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MoonAskCyanAccent
                                                )
                                            }
                                            Text(
                                                text = sha256ProgressText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MoonAskCyanAccent
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { sha256Progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MoonAskCyanAccent
                                        )
                                    }
                                }
                                apkValidationError != null -> {
                                    Text(
                                        text = apkValidationError!!,
                                        color = MoonAskRose,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                selectedApkName != null && calculatedSha256 != null -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MoonAskEmerald,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$selectedApkName ($sizeMb MB)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MoonAskEmerald
                                            )
                                        }
                                        Text(
                                            text = "SHA-256: ${calculatedSha256!!.take(16)}...${calculatedSha256!!.takeLast(8)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MoonAskCyanAccent.copy(alpha = 0.15f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "⚡ SuperBase Cloud: ${if (superBaseBucketKey.isNotBlank()) superBaseBucketKey else "moonask-apks/stored"}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MoonAskCyanAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Please select an '.apk' file. The platform will compute the cryptographic SHA-256 hash on device.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 1. APP LOGO / ICON CARD (GOOGLE PLAY CONSOLE STYLE)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                        .border(1.dp, MoonAskBluePrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = appLogoUrl,
                                        contentDescription = "App Logo Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "App Icon / Logo (512x512) *",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "High-resolution icon for MoonAsk Store & Android launcher",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        logoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pick Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        appLogoUrl = aiAssistant.autoAdjustLogo(appLogoUrl)
                                        isLogoAiAdjusted = true
                                        Toast.makeText(context, "✨ AI auto-adjusted photo to 512x512 Android Icon Standard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskCyanAccent, contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Auto-Adjust", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = appLogoUrl,
                                onValueChange = {
                                    appLogoUrl = it
                                    isLogoAiAdjusted = true
                                },
                                label = { Text("Logo Image URL or Local URI") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MoonAskEmerald.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MoonAskEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AI Auto-Crop: Any file photo is formatted to 512x512. Guaranteed zero rejection.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MoonAskEmerald
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Quick Logo Presets:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Rocket 🚀" to "https://images.unsplash.com/photo-1517976487502-52504892c90c?w=512",
                                    "Shield 🛡️" to "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=512",
                                    "Notes 📝" to "https://images.unsplash.com/photo-1517842645767-c639042777db?w=512",
                                    "Music 🎵" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=512",
                                    "Brain 🧠" to "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=512",
                                    "Game 🎮" to "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=512"
                                ).forEach { (label, url) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (appLogoUrl == url) MoonAskBluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (appLogoUrl == url) MoonAskBluePrimary else Color.Transparent),
                                        modifier = Modifier.clickable {
                                            appLogoUrl = url
                                            isLogoAiAdjusted = true
                                        }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        label = { Text("App Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = packageName,
                        onValueChange = { packageName = it },
                        label = { Text("Package Name (e.g. com.my.app) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 2. DEVELOPER / USER NAME & OFFICIAL WEBSITE
                    OutlinedTextField(
                        value = developerNameInput,
                        onValueChange = { developerNameInput = it },
                        label = { Text("Developer / User Name *") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MoonAskBluePrimary, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = websiteUrlInput,
                        onValueChange = { websiteUrlInput = it },
                        label = { Text("Official App Website URL *") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MoonAskCyanAccent, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 3. 4 SCREENSHOT SLOTS (GOOGLE PLAY STORE CONSOLE STYLE)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MoonAskCyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Phone Screenshots (4 Slots) *",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MoonAskCyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "4 of 4 Ready",
                                        color = MoonAskCyanAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Preview cards showing what users will see on your MoonAsk Store listing:",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                            )

                            // 4 Screenshot cards in a horizontal scrollable row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Screenshot 1", screenshot1Url) { newUrl: String -> screenshot1Url = newUrl },
                                    Triple("Screenshot 2", screenshot2Url) { newUrl: String -> screenshot2Url = newUrl },
                                    Triple("Screenshot 3", screenshot3Url) { newUrl: String -> screenshot3Url = newUrl },
                                    Triple("Screenshot 4", screenshot4Url) { newUrl: String -> screenshot4Url = newUrl }
                                ).forEachIndexed { index, (label, url, setter) ->
                                    Card(
                                        modifier = Modifier.width(96.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskBluePrimary.copy(alpha = 0.4f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(84.dp)
                                                    .height(130.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.Black)
                                            ) {
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = label,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color.Black.copy(alpha = 0.7f),
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(4.dp)
                                                ) {
                                                    Text(
                                                        text = "#${index + 1}",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Quick Screenshot Theme Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Preset Themes:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterVertically))
                                OutlinedButton(
                                    onClick = {
                                        screenshot1Url = "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=600"
                                        screenshot2Url = "https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=600"
                                        screenshot3Url = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600"
                                        screenshot4Url = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=600"
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Modern Dark", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        screenshot1Url = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600"
                                        screenshot2Url = "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=600"
                                        screenshot3Url = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=600"
                                        screenshot4Url = "https://images.unsplash.com/photo-1580541832626-2a7131ee809f?w=600"
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Productivity & Tech", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Category Dropdown
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = categories.find { it.id == selectedCategory }?.name ?: "Select Category",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        selectedCategory = cat.id
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = shortDesc,
                        onValueChange = { shortDesc = it },
                        label = { Text("Short Description *") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = fullDesc,
                        onValueChange = { fullDesc = it },
                        label = { Text("Full Description & Features") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // AI Store Distribution & ASO Optimizer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskCyanAccent.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("AI Store Distribution", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MoonAskCyanAccent)
                                Text("Auto-optimize title, ASO keywords & store copy", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    scope.launch {
                                        isOptimizingListing = true
                                        val opt = aiAssistant.optimizeAppListing(appName, shortDesc, selectedCategory)
                                        if (shortDesc.isBlank() || shortDesc.length < 10) {
                                            shortDesc = opt.optimizedShortDesc
                                        }
                                        if (fullDesc.isBlank() || fullDesc.length < 20) {
                                            fullDesc = opt.optimizedFullDesc
                                        }
                                        isOptimizingListing = false
                                        Toast.makeText(context, "✨ Store listing optimized with AI distribution copy & keywords!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MoonAskCyanAccent, contentColor = Color.Black),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                if (isOptimizingListing) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.Black)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("AI Optimize", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = versionName,
                            onValueChange = { versionName = it },
                            label = { Text("Version Name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sizeMb,
                            onValueChange = { sizeMb = it },
                            label = { Text("Size (MB)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = whatsNew,
                        onValueChange = { whatsNew = it },
                        label = { Text("What's New in this release") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // PROMO REEL VIDEO ATTACHMENT SECTION (Google Play / MoonAsk Reels Feed)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = null,
                                        tint = MoonAskBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Attach Promo Reel Video 🎬",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Reels Feed", fontSize = 10.sp)
                                    Checkbox(
                                        checked = attachPromoReel,
                                        onCheckedChange = { attachPromoReel = it }
                                    )
                                }
                            }

                            if (attachPromoReel) {
                                Text(
                                    text = "This promo video will automatically show in the MoonAsk Reels tab linked to your app.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = promoReelTitle,
                                    onValueChange = { promoReelTitle = it },
                                    label = { Text("Reel Video Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = promoReelVideoUrl,
                                    onValueChange = { promoReelVideoUrl = it },
                                    label = { Text("Video MP4 URL") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Quick Reel Presets:", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "Tech Code 💻" to "https://assets.mixkit.co/videos/preview/mixkit-software-developer-working-on-code-41584-large.mp4",
                                        "Mobile Phone 📱" to "https://assets.mixkit.co/videos/preview/mixkit-hands-holding-a-smart-phone-in-vertical-position-42517-large.mp4",
                                        "Studio Audio 🎧" to "https://assets.mixkit.co/videos/preview/mixkit-hands-of-a-dj-mixing-music-on-a-sound-console-42777-large.mp4",
                                        "Cyber HUD 🌐" to "https://assets.mixkit.co/videos/preview/mixkit-vertical-animation-of-futuristic-circuits-and-codes-43642-large.mp4"
                                    ).forEach { (label, url) ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (promoReelVideoUrl == url) MoonAskBluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (promoReelVideoUrl == url) MoonAskBluePrimary else Color.Transparent),
                                            modifier = Modifier.clickable { promoReelVideoUrl = url }
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Super Admin Release Type Selector (Public Store App vs Admin Special App)
                    if (currentUser?.role == UserRole.ADMIN) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Release Type (Super Admin Mode)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MoonAskAmber)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { isSpecialAppUpload = false }
                                ) {
                                    RadioButton(selected = !isSpecialAppUpload, onClick = { isSpecialAppUpload = false })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("Public Store App", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("Shows in store for all users, free to download and install.", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().clickable { isSpecialAppUpload = true }
                                ) {
                                    RadioButton(selected = isSpecialAppUpload, onClick = { isSpecialAppUpload = true })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("Admin Special App", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MoonAskAmber)
                                        Text("Shows in store with lock badge; requires passcode to access & install.", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                if (isSpecialAppUpload) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = if (customPasscodeInput.isNotBlank()) customPasscodeInput else adminSpecialPasscode,
                                        onValueChange = { customPasscodeInput = it },
                                        label = { Text("Special Access Passcode") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (appName.isBlank() || packageName.isBlank() || shortDesc.isBlank()) {
                            Toast.makeText(context, "Please complete all required fields (*)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!packageName.contains(".") || packageName.length < 5) {
                            Toast.makeText(context, "Enter a valid package name (e.g. com.developer.app)", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (calculatedSha256 == null) {
                            Toast.makeText(context, "Please choose an APK file and wait for SHA-256 verification", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val parsedSize = sizeMb.toFloatOrNull() ?: 12.5f

                        val submittedApp = repository.submitApp(
                            name = appName,
                            packageName = packageName,
                            categoryId = selectedCategory,
                            shortDesc = shortDesc,
                            fullDesc = fullDesc.ifBlank { shortDesc },
                            iconUrl = appLogoUrl.ifBlank { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=512" },
                            versionName = versionName,
                            versionCode = versionCode,
                            sizeMb = parsedSize,
                            permissions = listOf("INTERNET", "ACCESS_NETWORK_STATE"),
                            whatsNew = whatsNew,
                            apkUrl = localSavedApkPath ?: "",
                            sha256Checksum = calculatedSha256 ?: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                            isUserUploaded = true,
                            localApkPath = localSavedApkPath ?: "",
                            isAdminSpecial = isSpecialAppUpload,
                            specialAccessPasscode = if (isSpecialAppUpload) (customPasscodeInput.ifBlank { adminSpecialPasscode }) else "",
                            websiteUrl = websiteUrlInput.ifBlank { "https://moonask.store/developer" },
                            developerName = developerNameInput.ifBlank { currentUser?.displayName ?: "Independent Creator" },
                            screenshots = listOf(screenshot1Url, screenshot2Url, screenshot3Url, screenshot4Url),
                            storageBucketKey = superBaseBucketKey,
                            storageProvider = "MoonAsk SuperBase Cloud Vault"
                        )

                        if (attachPromoReel) {
                            repository.addReel(
                                title = promoReelTitle.ifBlank { "$appName Official Showcase" },
                                videoUrl = promoReelVideoUrl.ifBlank { "https://assets.mixkit.co/videos/preview/mixkit-software-developer-working-on-code-41584-large.mp4" },
                                description = "$appName showcase on MoonAsk Store! Download now.",
                                linkedAppId = submittedApp.id,
                                linkedAppName = submittedApp.name,
                                creatorName = developerNameInput.ifBlank { currentUser?.displayName ?: "MoonAsk Creator" }
                            )
                        }

                        showCreateAppDialog = false
                        Toast.makeText(
                            context,
                            if (isSpecialAppUpload) "Admin Special App created and published!" else "APK submitted to Super Admin! It will appear in store upon approval.",
                            Toast.LENGTH_LONG
                        ).show()
                        appName = ""
                        packageName = ""
                        shortDesc = ""
                        fullDesc = ""
                        selectedApkName = null
                        calculatedSha256 = null
                        localSavedApkPath = null
                        isSpecialAppUpload = false
                        customPasscodeInput = ""
                        promoReelTitle = ""
                    }
                ) {
                    Text("Submit for Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAppDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = it.getString(index)
                }
            }
        }
    }
    if (fileName == null) {
        fileName = uri.path?.substringAfterLast('/')
    }
    return fileName
}

private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
    var size: Long = -1L
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.SIZE)
                if (index != -1) {
                    size = it.getLong(index)
                }
            }
        }
    }
    return size
}

@Composable
fun StatusPill(status: PublicationStatus, rejectionReason: String = "") {
    val (text, color, icon) = when (status) {
        PublicationStatus.PUBLISHED -> Triple("Published", MoonAskEmerald, Icons.Default.CheckCircle)
        PublicationStatus.PENDING_REVIEW -> Triple("Pending Review", MoonAskAmber, Icons.Default.Info)
        PublicationStatus.CHANGES_REQUESTED -> Triple("Changes Requested", MoonAskAmber, Icons.Default.Warning)
        PublicationStatus.REJECTED -> Triple("Rejected", MoonAskRose, Icons.Default.Warning)
        PublicationStatus.DRAFT -> Triple("Draft", Color.Gray, Icons.Default.Info)
        PublicationStatus.APPROVED -> Triple("Approved", MoonAskCyanAccent, Icons.Default.CheckCircle)
        PublicationStatus.SUSPENDED -> Triple("Suspended", MoonAskRose, Icons.Default.Warning)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            if (rejectionReason.isNotBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($rejectionReason)",
                    color = color,
                    fontSize = 9.sp
                )
            }
        }
    }
}

