package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.StoreRepository
import com.example.ui.theme.MoonAskAmber
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskEmerald
import com.example.ui.theme.MoonAskRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    repository: StoreRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by repository.currentUser.collectAsState()
    val apps by repository.apps.collectAsState()
    val reports by repository.reports.collectAsState()
    val registeredUsers by repository.registeredUsers.collectAsState()
    val currentSpecialPasscode by repository.adminSpecialPasscode.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending Review", "All Apps", "User Accounts", "Admin Special Apps", "Abuse Reports")

    // Admin login form state if not signed in as admin
    var adminEmailInput by remember { mutableStateOf(StoreRepository.SUPER_ADMIN_EMAIL) }
    var adminPassInput by remember { mutableStateOf(StoreRepository.SUPER_ADMIN_PASSWORD) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Special Passcode Editor state
    var newSpecialPasscodeInput by remember { mutableStateOf("") }

    // Password visibility toggle map (uid -> isVisible)
    var passwordVisibilityMap by remember { mutableStateOf(mapOf<String, Boolean>()) }

    // Dialog state
    var selectedAppForAction by remember { mutableStateOf<StoreApp?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "REJECT" or "REQUEST_CHANGES"
    var actionReason by remember { mutableStateOf("") }

    val isSuperAdmin = currentUser?.role == UserRole.ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MoonAskAmber
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Super Admin Console", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!isSuperAdmin) {
            // Admin Authentication Gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MoonAskAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Admin Protected",
                        tint = MoonAskAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Super Admin Access Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Only authorized admin account can review and publish releases.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = adminEmailInput,
                    onValueChange = { adminEmailInput = it },
                    label = { Text("Admin Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = adminPassInput,
                    onValueChange = { adminPassInput = it },
                    label = { Text("Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (loginError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = loginError!!, color = MoonAskRose, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val result = repository.loginWithEmail(adminEmailInput, adminPassInput)
                        if (result.isSuccess && result.getOrNull()?.role == UserRole.ADMIN) {
                            loginError = null
                            Toast.makeText(context, "Super Admin access verified!", Toast.LENGTH_SHORT).show()
                        } else {
                            loginError = "Access denied. Credentials do not match Super Admin."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("admin_login_submit"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskAmber, contentColor = Color.Black)
                ) {
                    Text("Verify Admin Access", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Super Admin Dashboard
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("super_admin_console"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Admin Status Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MoonAskAmber.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛡️", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Logged in as ${currentUser?.email}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Full platform approval, publication, and moderation authority.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { repository.logout() }) {
                                Text("Log out", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Stats Row
                item {
                    val pendingCount = apps.count { it.publicationStatus == PublicationStatus.PENDING_REVIEW }
                    val publishedCount = apps.count { it.publicationStatus == PublicationStatus.PUBLISHED }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatCard(title = "Pending Review", value = "$pendingCount", color = MoonAskAmber, modifier = Modifier.weight(1f))
                        AdminStatCard(title = "Published Apps", value = "$publishedCount", color = MoonAskEmerald, modifier = Modifier.weight(1f))
                        AdminStatCard(title = "Open Reports", value = "${reports.size}", color = MoonAskRose, modifier = Modifier.weight(1f))
                    }
                }

                // Store Display Mode Controller (Admin requirement: show only real user-uploaded apps)
                item {
                    val showOnlyRealApps by repository.showOnlyRealApps.collectAsState()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Store Catalogue Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        text = if (showOnlyRealApps) "Showing ONLY user-uploaded approved apps" else "Showing user apps + demo catalog",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (showOnlyRealApps) MoonAskEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                androidx.compose.material3.Switch(
                                    checked = showOnlyRealApps,
                                    onCheckedChange = { repository.toggleShowOnlyRealApps() }
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        repository.clearDemoApps()
                                        Toast.makeText(context, "Demo apps cleared! Store now holds only real uploaded apps.", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Clear Demo Apps", color = MoonAskRose, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Navigation Tabs
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        edgePadding = 8.dp,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // Tab 0: Pending Review Queue
                if (selectedTabIndex == 0) {
                    val pendingApps = apps.filter { it.publicationStatus == PublicationStatus.PENDING_REVIEW }
                    if (pendingApps.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎉", fontSize = 42.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Review queue is clear!", fontWeight = FontWeight.Bold)
                                    Text("All developer submissions have been evaluated.", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    } else {
                        items(pendingApps) { app ->
                            AdminAppReviewCard(
                                app = app,
                                onApprove = {
                                    repository.approveAndPublishApp(app.id)
                                    Toast.makeText(context, "${app.name} is now Approved & Published!", Toast.LENGTH_SHORT).show()
                                },
                                onRequestChanges = {
                                    selectedAppForAction = app
                                    actionType = "REQUEST_CHANGES"
                                    actionReason = ""
                                },
                                onReject = {
                                    selectedAppForAction = app
                                    actionType = "REJECT"
                                    actionReason = ""
                                }
                            )
                        }
                    }
                }

                // Tab 1: All Apps (Featured controls)
                if (selectedTabIndex == 1) {
                    items(apps) { app ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (app.isAdminSpecial) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MoonAskAmber.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "SPECIAL",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MoonAskAmber
                                                )
                                            }
                                        }
                                    }
                                    Text("${app.packageName} • v${app.versionName}", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { repository.toggleFeaturedApp(app.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Feature App",
                                        tint = if (app.isFeatured) MoonAskAmber else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // Tab 2: Registered User Accounts Management
                if (selectedTabIndex == 2) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "👥 User Registry & Access Control",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "View all registered users with emails and passwords. Control account blocking, force logout, and toggle Free Upload exemption.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(registeredUsers) { user ->
                        val isPasswordVisible = passwordVisibilityMap[user.uid] ?: false
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (user.isBlocked) MoonAskRose.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (user.isBlocked) androidx.compose.foundation.BorderStroke(1.dp, MoonAskRose.copy(alpha = 0.4f)) else null
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (user.role == UserRole.ADMIN) MoonAskAmber.copy(alpha = 0.2f)
                                                else if (user.isBlocked) MoonAskRose.copy(alpha = 0.2f)
                                                else MoonAskCyanAccent.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (user.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (user.role == UserRole.ADMIN) MoonAskAmber else if (user.isBlocked) MoonAskRose else MoonAskCyanAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(user.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (user.isBlocked) MoonAskRose.copy(alpha = 0.2f) else MoonAskEmerald.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = if (user.isBlocked) "BLOCKED" else "ACTIVE",
                                                    color = if (user.isBlocked) MoonAskRose else MoonAskEmerald,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${user.email} • Role: ${user.role.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Registered Password Row with Toggle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Password: ",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = if (isPasswordVisible) user.password.ifBlank { "(None)" } else "••••••••",
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isPasswordVisible) MoonAskAmber else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                passwordVisibilityMap = passwordVisibilityMap + (user.uid to !isPasswordVisible)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Free Upload Status & Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (user.hasFreeUpload) MoonAskEmerald.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (user.hasFreeUpload) "✓ Free Upload Active" else "No Free Upload",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (user.hasFreeUpload) MoonAskEmerald else Color.Gray
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Toggle Free Upload
                                        OutlinedButton(
                                            onClick = {
                                                repository.toggleFreeUpload(user.uid)
                                                Toast.makeText(context, "Free Upload toggled for ${user.email}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(if (user.hasFreeUpload) "Revoke Free" else "Grant Free", fontSize = 10.sp)
                                        }

                                        // Force Out Button
                                        OutlinedButton(
                                            onClick = {
                                                repository.forceOutUser(user.uid)
                                                Toast.makeText(context, "Forced logout for ${user.displayName}", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Force Out", fontSize = 10.sp)
                                        }

                                        // Block / Unblock Button (prevent self-block for super admin)
                                        if (user.email != StoreRepository.SUPER_ADMIN_EMAIL) {
                                            Button(
                                                onClick = {
                                                    if (user.isBlocked) {
                                                        repository.unblockUser(user.uid)
                                                        Toast.makeText(context, "User unblocked", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        repository.blockUser(user.uid)
                                                        Toast.makeText(context, "User blocked and logged out", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (user.isBlocked) MoonAskEmerald else MoonAskRose,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Block, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(if (user.isBlocked) "Unblock" else "Block", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 3: Admin Special Apps & Passcode Configuration
                if (selectedTabIndex == 3) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = MoonAskAmber
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Admin Special App Passcode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Admin Special Apps are visible to all users in MoonAsk Store, but require this passcode to download and install. Normal users without the passcode cannot download or install these apps.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Current Active Passcode:", fontSize = 12.sp)
                                        Text(
                                            text = currentSpecialPasscode,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = MoonAskAmber,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = newSpecialPasscodeInput,
                                        onValueChange = { newSpecialPasscodeInput = it },
                                        label = { Text("New Passcode (e.g. 7996)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (newSpecialPasscodeInput.isNotBlank()) {
                                                repository.setAdminSpecialPasscode(newSpecialPasscodeInput)
                                                Toast.makeText(context, "Admin Special Passcode updated to '$newSpecialPasscodeInput'!", Toast.LENGTH_LONG).show()
                                                newSpecialPasscodeInput = ""
                                            } else {
                                                Toast.makeText(context, "Enter a valid passcode", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MoonAskAmber, contentColor = Color.Black)
                                    ) {
                                        Text("Update", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Admin Special Applications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    val specialApps = apps.filter { it.isAdminSpecial }
                    if (specialApps.isEmpty()) {
                        item {
                            Text(
                                text = "No Admin Special apps published yet. Use Developer Portal to publish a Special App with passcode protection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(specialApps) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MoonAskAmber.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
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
                                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MoonAskAmber, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Text("${app.packageName} • v${app.versionName}", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MoonAskAmber.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Required Passcode: ${app.specialAccessPasscode.ifBlank { currentSpecialPasscode }}",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MoonAskAmber,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab 4: Abuse Reports
                if (selectedTabIndex == 4) {
                    items(reports) { rep ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(rep.appName, fontWeight = FontWeight.Bold)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MoonAskRose.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = rep.reason,
                                            color = MoonAskRose,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(rep.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Reason Dialog for Rejection / Changes Requested
    if (selectedAppForAction != null && actionType != null) {
        val app = selectedAppForAction!!
        AlertDialog(
            onDismissRequest = {
                selectedAppForAction = null
                actionType = null
            },
            title = {
                Text(if (actionType == "REJECT") "Reject ${app.name}" else "Request Changes for ${app.name}")
            },
            text = {
                Column {
                    Text(
                        "Please provide specific feedback for developer ${app.developerName}:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionReason,
                        onValueChange = { actionReason = it },
                        label = { Text("Reason / Feedback *") },
                        placeholder = { Text("e.g. Incompatible permissions, broken download link, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionReason.isNotBlank()) {
                            if (actionType == "REJECT") {
                                repository.rejectApp(app.id, actionReason)
                                Toast.makeText(context, "${app.name} has been rejected.", Toast.LENGTH_SHORT).show()
                            } else {
                                repository.requestChanges(app.id, actionReason)
                                Toast.makeText(context, "Changes requested for ${app.name}.", Toast.LENGTH_SHORT).show()
                            }
                            selectedAppForAction = null
                            actionType = null
                        } else {
                            Toast.makeText(context, "Please enter a reason", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (actionType == "REJECT") MoonAskRose else MoonAskAmber,
                        contentColor = Color.White
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    selectedAppForAction = null
                    actionType = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminAppReviewCard(
    app: StoreApp,
    onApprove: () -> Unit,
    onRequestChanges: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                        Text(app.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (app.isUserUploaded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MoonAskCyanAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "USER APK UPLOAD",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MoonAskCyanAccent
                                )
                            }
                        }
                    }
                    Text("By: ${app.developerName} • ${app.packageName} • v${app.versionName}", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(app.shortDescription, style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "SHA-256: ${app.sha256Checksum.take(24)}...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskEmerald, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onRequestChanges,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Changes", fontSize = 12.sp)
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MoonAskRose, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 12.sp)
                }
            }
        }
    }
}
