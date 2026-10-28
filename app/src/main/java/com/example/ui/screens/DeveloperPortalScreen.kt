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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
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
                    val digest = MessageDigest.getInstance("SHA-256")
                    val dir = File(context.filesDir, "developer_apks").apply { mkdirs() }
                    val destFile = File(dir, "${System.currentTimeMillis()}_$fileName")
                    val output = FileOutputStream(destFile)

                    val buffer = ByteArray(65536) // 64KB buffer for large files
                    var bytesRead: Int
                    var totalBytes = 0L
                    var lastProgressUpdate = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        digest.update(buffer, 0, bytesRead)
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead

                        if (totalBytes - lastProgressUpdate > 256 * 1024L || bytesRead == -1) {
                            lastProgressUpdate = totalBytes
                            val currentProgress = if (fileSize > 0) (totalBytes.toFloat() / fileSize).coerceIn(0f, 1f) else 0.5f
                            val text = "${(currentProgress * 100).toInt()}% (${String.format("%.1f", totalBytes / (1024f * 1024f))} MB / ${String.format("%.1f", fileSize.coerceAtLeast(totalBytes) / (1024f * 1024f))} MB)"
                            withContext(Dispatchers.Main) {
                                sha256Progress = currentProgress
                                sha256ProgressText = text
                            }
                        }
                    }
                    output.flush()
                    output.close()
                    input.close()

                    val hashBytes = digest.digest()
                    val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
                    val mb = (totalBytes.toFloat() / (1024f * 1024f))

                    withContext(Dispatchers.Main) {
                        sha256Progress = 1f
                        sha256ProgressText = "100% (${String.format("%.1f", mb)} MB)"
                        calculatedSha256 = hashHex
                        localSavedApkPath = destFile.absolutePath
                        sizeMb = String.format("%.1f", mb.coerceAtLeast(0.1f))
                        isCalculatingSha256 = false
                        Toast.makeText(context, "APK validated! SHA-256 computed.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        apkValidationError = "Failed to open selected file stream."
                        isCalculatingSha256 = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    apkValidationError = "Error computing checksum: ${e.message}"
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

                        val parsedSize = sizeMb.toFloatOrNull() ?: 12.0f
                        repository.submitApp(
                            name = appName,
                            packageName = packageName,
                            categoryId = selectedCategory,
                            shortDesc = shortDesc,
                            fullDesc = fullDesc.ifBlank { shortDesc },
                            iconUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
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
                            specialAccessPasscode = if (isSpecialAppUpload) (customPasscodeInput.ifBlank { adminSpecialPasscode }) else ""
                        )

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

