package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.example.ui.components.AppCardItem
import com.example.ui.theme.MoonAskAmber
import com.example.ui.theme.MoonAskBlueDark
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskCyanLight
import com.example.ui.theme.MoonAskEmerald
import com.example.ui.theme.MoonAskNavy

@Composable
fun HomeScreen(
    repository: StoreRepository,
    installerManager: PackageInstallerManager,
    onAppClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onProfileClick: () -> Unit,
    onInstallApp: (StoreApp) -> Unit
) {
    val apps by repository.apps.collectAsState()
    val categories by repository.categories.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val installStates by installerManager.installState.collectAsState()

    var selectedCategoryId by remember { mutableStateOf("all") }

    val showOnlyRealApps by repository.showOnlyRealApps.collectAsState()
    val hasUploadedPublished = apps.any { it.isUserUploaded && it.publicationStatus == PublicationStatus.PUBLISHED }

    val publishedApps = apps.filter { it.publicationStatus == PublicationStatus.PUBLISHED }

    // Prioritize user-uploaded approved apps as requested: don't show demo apps when real uploaded apps are approved
    val storeDisplayApps = if (hasUploadedPublished || showOnlyRealApps) {
        val userApps = publishedApps.filter { it.isUserUploaded }
        if (userApps.isNotEmpty()) userApps else publishedApps
    } else {
        publishedApps
    }

    val featuredApps = storeDisplayApps.filter { it.isFeatured }
    val filteredApps = if (selectedCategoryId == "all") {
        storeDisplayApps
    } else {
        storeDisplayApps.filter { it.categoryId.equals(selectedCategoryId, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top MoonAsk Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(MoonAskCyanAccent, MoonAskBluePrimary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌙", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MoonAsk Store",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Official Android Distribution",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onAiAssistantClick,
                            modifier = Modifier.testTag("home_ai_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = MoonAskCyanAccent
                            )
                        }

                        IconButton(
                            onClick = onProfileClick,
                            modifier = Modifier.testTag("home_profile_avatar")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar Trigger
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onSearchClick() }
                        .testTag("home_search_bar"),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search apps, categories, games...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MoonAskCyanAccent.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MoonAskCyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ask AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MoonAskCyanAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // Hero Spotlight Banner
        item {
            val heroApp = featuredApps.firstOrNull() ?: publishedApps.firstOrNull()
            if (heroApp != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onAppClick(heroApp.id) }
                        .testTag("hero_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MoonAskNavy)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MoonAskNavy, MoonAskBlueDark, Color(0xFF0F172A))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = heroApp.iconUrl,
                                    contentDescription = heroApp.name,
                                    modifier = Modifier.size(76.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MoonAskAmber
                                ) {
                                    Text(
                                        text = "★ EDITOR'S SPOTLIGHT",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = heroApp.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = heroApp.shortDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Categories Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                FilterChip(
                    selected = selectedCategoryId == "all",
                    onClick = { selectedCategoryId = "all" },
                    label = { Text("All Apps") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id },
                        label = { Text(category.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        // "Your published app" Banner (Matching PDF Page 1)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onProfileClick() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MoonAskNavy)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(MoonAskNavy, Color(0xFF1E3A8A), MoonAskBluePrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Your published app",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your published app",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage, update and grow your apps on MoonAsk",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { onProfileClick() }
                    ) {
                        Text(
                            text = "View Apps →",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Recommended for you (Matching PDF Page 1 3-item Grid/Row)
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recommended for you",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See all ›",
                        fontSize = 12.sp,
                        color = MoonAskBluePrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onSearchClick() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val recommendedList = storeDisplayApps.filter { !it.isAdminSpecial }.take(3)
                    recommendedList.forEach { app ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAppClick(app.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = app.iconUrl,
                                        contentDescription = app.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MoonAskAmber,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = String.format("%.1f", app.rating),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val isInstalled = installerManager.isAppInstalled(app.packageName)
                                val state = installStates[app.id] ?: installerManager.getStateForApp(app.id)
                                Button(
                                    onClick = {
                                        if (isInstalled) {
                                            installerManager.openInstalledApp(app.packageName)
                                        } else {
                                            onInstallApp(app)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    shape = RoundedCornerShape(15.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isInstalled) MoonAskEmerald else MoonAskBluePrimary
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    if (state is com.example.data.installer.DownloadInstallState.PromptingInstaller) {
                                        Text("Installing...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    } else {
                                        Text(if (isInstalled) "Open" else "Install", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Popular Section (Matching PDF Page 1 3-item Grid/Row)
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "See all ›",
                        fontSize = 12.sp,
                        color = MoonAskBluePrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onSearchClick() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val popularList = storeDisplayApps.filter { !it.isAdminSpecial }.drop(1).take(3)
                    popularList.forEach { app ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAppClick(app.id) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = app.iconUrl,
                                        contentDescription = app.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = app.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = MoonAskAmber,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = String.format("%.1f", app.rating),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val isInstalled = installerManager.isAppInstalled(app.packageName)
                                val state = installStates[app.id] ?: installerManager.getStateForApp(app.id)
                                Button(
                                    onClick = {
                                        if (isInstalled) {
                                            installerManager.openInstalledApp(app.packageName)
                                        } else {
                                            onInstallApp(app)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    shape = RoundedCornerShape(15.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isInstalled) MoonAskEmerald else MoonAskBluePrimary
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    if (state is com.example.data.installer.DownloadInstallState.PromptingInstaller) {
                                        Text("Installing...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    } else {
                                        Text(if (isInstalled) "Open" else "Install", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Featured Apps Section (Horizontal Cards)
        if (selectedCategoryId == "all" && featuredApps.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Featured Releases",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(featuredApps) { app ->
                            Card(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable { onAppClick(app.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .align(Alignment.CenterHorizontally),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = app.iconUrl,
                                            contentDescription = app.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = app.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${app.sizeMb} MB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MoonAskAmber,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${app.rating}",
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

        // Main Apps Feed Section
        item {
            Text(
                text = if (selectedCategoryId == "all") "Popular & Recommended" else "Apps in ${categories.find { it.id == selectedCategoryId }?.name ?: ""}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(filteredApps, key = { it.id }) { app ->
            val installState = installStates[app.id] ?: installerManager.getStateForApp(app.id)
            val isInstalled = installerManager.isAppInstalled(app.packageName)

            AppCardItem(
                app = app,
                installState = installState,
                isInstalled = isInstalled,
                onAppClick = { onAppClick(app.id) },
                onInstallClick = { onInstallApp(app) },
                onOpenClick = { installerManager.openInstalledApp(app.packageName) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}
