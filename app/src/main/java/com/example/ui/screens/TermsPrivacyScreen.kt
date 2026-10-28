package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MoonAskBluePrimary
import com.example.ui.theme.MoonAskCyanAccent
import com.example.ui.theme.MoonAskEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsPrivacyScreen(
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Terms of Service", "Privacy Policy", "About MoonAsk")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Legal & About",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("terms_privacy_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Segmented Tab Controls (Terms of Service, Privacy Policy, About MoonAsk)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .clip(RoundedCornerShape(24.dp)),
                            color = MoonAskBluePrimary,
                            height = 36.dp
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTabIndex) {
                0 -> {
                    // Terms of Service Content (Strictly matching PDF Page 2)
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Last updated: 12 Oct 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "1. Acceptance of Terms",
                        content = "By using MoonAsk Store, you agree to these Terms of Service, which govern your use of our platform."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "2. User Accounts",
                        content = "You are responsible for maintaining the security of your account and all activities that occur under your account."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "3. Prohibited Activities",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    BulletPointItem(text = "Uploading illegal content")
                    BulletPointItem(text = "Distributing malware")
                    BulletPointItem(text = "Violating intellectual property rights")
                    BulletPointItem(text = "Spamming or abusing the platform")

                    Spacer(modifier = Modifier.height(20.dp))

                    LegalSection(
                        title = "4. App Publishing & Moderation",
                        content = "All applications submitted by developers require Super Admin review and verification before public distribution. MoonAsk Store reserves the right to suspend or remove any application failing integrity or security checks."
                    )
                }

                1 -> {
                    // Privacy Policy Content
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Last updated: 12 Oct 2024",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "1. Data Collection & Privacy",
                        content = "MoonAsk Store operates with a privacy-first approach. We collect only required account authentication credentials and diagnostic telemetry necessary to deliver package updates and verify APK package integrity. We never sell user data or sell targeted advertiser tracking."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "2. Cryptographic SHA-256 Hashes",
                        content = "All published APK files are validated against cryptographic SHA-256 hashes generated on the client device and confirmed by our Super Admin review queue before distribution, guaranteeing zero tampering."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "3. Gemini AI Privacy Notice",
                        content = "Search queries and permission analysis sent to MoonAsk AI Assistant are processed securely via Firebase AI Logic and Google Gemini 3.8 Flash. No personally identifiable account data is attached to AI prompts."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "4. Your Rights & Account Deletion",
                        content = "You retain complete ownership over your uploaded applications, written reviews, and user account. You may request account deletion or package removal at any time through the support portal."
                    )
                }

                2 -> {
                    // About MoonAsk Store Content
                    Text(
                        text = "About MoonAsk Store",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Official Android Application Distribution Platform",
                        style = MaterialTheme.typography.bodySmall,
                        color = MoonAskCyanAccent
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MoonAskBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🌙", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("MoonAsk Store", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    Text("Version 1.0.0 (Build 2024)", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "MoonAsk Store is an independent Android application distribution platform designed to empower developers and mobile users. We provide open APK distribution, cryptographic package verification, and native installation using Android's PackageInstaller system.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "Platform Leadership & Administration",
                        content = "MoonAsk Store is maintained under the direct supervision of Super Admin Sk Nijam (uddinsknijam102@gmail.com). All developer submissions and community reports are audited to protect user safety."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "Android Package Installer System",
                        content = "MoonAsk Store utilizes standard Android PackageInstaller APIs and Android FileProvider content URIs. APK files are downloaded directly to secure app cache and installed via the official Android system verification prompt."
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    LegalSection(
                        title = "Contact & Developer Support",
                        content = "For developer onboarding, security reports, or inquiries:\n• Super Admin: uddinsknijam102@gmail.com\n• Support: support@moonask.store\n• Website: https://moonask.store"
                    )
                }
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun BulletPointItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MoonAskBluePrimary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
