package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.example.ui.components.AppCardItem
import com.example.ui.theme.MoonAskCyanAccent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    repository: StoreRepository,
    installerManager: PackageInstallerManager,
    onAppClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onAiAssistantClick: () -> Unit,
    onInstallApp: (StoreApp) -> Unit
) {
    val apps by repository.apps.collectAsState()
    val recentSearches by repository.recentSearches.collectAsState()
    val installStates by installerManager.installState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var minRatingFilter by remember { mutableStateOf(0f) }

    val showOnlyRealApps by repository.showOnlyRealApps.collectAsState()
    val hasUploadedPublished = apps.any { it.isUserUploaded && it.publicationStatus == PublicationStatus.PUBLISHED }
    val publishedApps = apps.filter { it.publicationStatus == PublicationStatus.PUBLISHED }
    val storeDisplayApps = if (hasUploadedPublished || showOnlyRealApps) {
        val userApps = publishedApps.filter { it.isUserUploaded }
        if (userApps.isNotEmpty()) userApps else publishedApps
    } else {
        publishedApps
    }

    val searchResults = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        val q = searchQuery.trim().lowercase()
        storeDisplayApps.filter { app ->
            (app.name.lowercase().contains(q) ||
             app.developerName.lowercase().contains(q) ||
             app.packageName.lowercase().contains(q) ||
             app.categoryId.lowercase().contains(q) ||
             app.shortDescription.lowercase().contains(q)) &&
             app.rating >= minRatingFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("search_input_field"),
                        placeholder = { Text("Search apps & games...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAiAssistantClick) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Search",
                            tint = MoonAskCyanAccent
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
                .testTag("search_results_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Recent Searches or Empty Query View
            if (searchQuery.isBlank()) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(onClick = { repository.clearRecentSearches() }) {
                                Text("Clear", fontSize = 12.sp)
                            }
                        }

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            recentSearches.forEach { term ->
                                AssistChip(
                                    onClick = { searchQuery = term },
                                    label = { Text(term) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Suggested Searches",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Privacy VPN", "Offline Notes", "Chess Game", "Audio Visualizer", "Fitness Tracker").forEach { topic ->
                            AssistChip(
                                onClick = { searchQuery = topic },
                                label = { Text(topic) }
                            )
                        }
                    }
                }
            } else {
                // Results Header & Count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${searchResults.size} results for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "4.5★+ only",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (minRatingFilter > 0f) MoonAskCyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        minRatingFilter = if (minRatingFilter > 0f) 0f else 4.5f
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                if (searchResults.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching applications found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching for another keyword or ask Gemini AI for suggestions.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(searchResults, key = { it.id }) { app ->
                    val installState = installStates[app.id] ?: installerManager.getStateForApp(app.id)
                    val isInstalled = installerManager.isAppInstalled(app.packageName)

                    AppCardItem(
                        app = app,
                        installState = installState,
                        isInstalled = isInstalled,
                        onAppClick = {
                            repository.addRecentSearch(searchQuery)
                            onAppClick(app.id)
                        },
                        onInstallClick = {
                            repository.addRecentSearch(searchQuery)
                            onInstallApp(app)
                        },
                        onOpenClick = { installerManager.openInstalledApp(app.packageName) }
                    )
                }
            }
        }
    }
}
