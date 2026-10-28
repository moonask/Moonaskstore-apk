package com.example.data.repository

import com.example.data.model.AppCategory
import com.example.data.model.AppReel
import com.example.data.model.AppReport
import com.example.data.model.AppReview
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class StoreRepository {

    // Current Logged-in User State
    private val _currentUser = MutableStateFlow<UserAccount?>(
        UserAccount(
            uid = "guest_001",
            email = "guest@moonask.store",
            displayName = "Guest Explorer",
            role = UserRole.GUEST
        )
    )
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    // Apps Catalog
    private val _apps = MutableStateFlow<List<StoreApp>>(initialApps())
    val apps: StateFlow<List<StoreApp>> = _apps.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<AppCategory>>(initialCategories())
    val categories: StateFlow<List<AppCategory>> = _categories.asStateFlow()

    // Reels Feed
    private val _reels = MutableStateFlow<List<AppReel>>(initialReels())
    val reels: StateFlow<List<AppReel>> = _reels.asStateFlow()

    // Reviews map appId -> List<AppReview>
    private val _reviews = MutableStateFlow<Map<String, List<AppReview>>>(initialReviews())
    val reviews: StateFlow<Map<String, List<AppReview>>> = _reviews.asStateFlow()

    // Reports list
    private val _reports = MutableStateFlow<List<AppReport>>(initialReports())
    val reports: StateFlow<List<AppReport>> = _reports.asStateFlow()

    // Favorites appId set
    private val _favorites = MutableStateFlow<Set<String>>(setOf("app_1", "app_3"))
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Recent Searches
    private val _recentSearches = MutableStateFlow<List<String>>(listOf("Audio player", "Cipher VPN", "Camera HD", "Notes"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // Registered Users Store for Super Admin inspection & management
    private val _registeredUsers = MutableStateFlow<List<UserAccount>>(initialUsers())
    val registeredUsers: StateFlow<List<UserAccount>> = _registeredUsers.asStateFlow()

    // Admin Special App Passcode (Default 7996, configurable by Admin)
    private val _adminSpecialPasscode = MutableStateFlow("7996")
    val adminSpecialPasscode: StateFlow<String> = _adminSpecialPasscode.asStateFlow()

    fun setAdminSpecialPasscode(passcode: String) {
        if (passcode.isNotBlank()) {
            _adminSpecialPasscode.value = passcode.trim()
        }
    }

    // Admin authentication constant
    companion object {
        const val SUPER_ADMIN_EMAIL = "uddinsknijam102@gmail.com"
        const val SUPER_ADMIN_PASSWORD = "7996098760@3"
    }

    fun loginWithEmail(email: String, pass: String): Result<UserAccount> {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        // Check if user is blocked in the registry
        val existingUser = _registeredUsers.value.find { it.email.equals(trimmedEmail, ignoreCase = true) }
        if (existingUser != null && existingUser.isBlocked) {
            return Result.failure(Exception("Access Denied: This account has been blocked by Super Admin."))
        }

        return if (trimmedEmail.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) && trimmedPass == SUPER_ADMIN_PASSWORD) {
            val adminUser = existingUser ?: UserAccount(
                uid = "admin_sk_nijam",
                email = SUPER_ADMIN_EMAIL,
                displayName = "SK Nijam (Super Admin)",
                role = UserRole.ADMIN,
                password = SUPER_ADMIN_PASSWORD,
                hasFreeUpload = true
            )
            _currentUser.value = adminUser
            // Ensure admin is in the users list
            if (existingUser == null) {
                _registeredUsers.value = listOf(adminUser) + _registeredUsers.value
            }
            Result.success(adminUser)
        } else if (trimmedEmail.isNotBlank() && trimmedPass.length >= 6) {
            val role = if (trimmedEmail.contains("dev", ignoreCase = true)) UserRole.DEVELOPER else UserRole.USER
            val user = existingUser?.copy(password = trimmedPass) ?: UserAccount(
                uid = "user_${UUID.randomUUID().toString().take(8)}",
                email = trimmedEmail,
                displayName = trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = role,
                password = trimmedPass,
                hasFreeUpload = true
            )
            _currentUser.value = user
            if (existingUser == null) {
                _registeredUsers.value = listOf(user) + _registeredUsers.value
            }
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid email or password (minimum 6 characters required)."))
        }
    }

    fun loginWithGoogle(displayName: String, email: String): UserAccount {
        val role = if (email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true)) UserRole.ADMIN else UserRole.USER
        val existing = _registeredUsers.value.find { it.email.equals(email, ignoreCase = true) }
        val user = existing ?: UserAccount(
            uid = "google_${UUID.randomUUID().toString().take(8)}",
            email = email,
            displayName = displayName,
            role = role,
            password = "(Google OAuth)",
            hasFreeUpload = true
        )
        _currentUser.value = user
        if (existing == null) {
            _registeredUsers.value = listOf(user) + _registeredUsers.value
        }
        return user
    }

    fun loginWithPhone(phoneNumber: String): UserAccount {
        val email = "$phoneNumber@moonask.phone"
        val existing = _registeredUsers.value.find { it.email == email }
        val user = existing ?: UserAccount(
            uid = "phone_${UUID.randomUUID().toString().take(8)}",
            email = email,
            displayName = "User ($phoneNumber)",
            phoneNumber = phoneNumber,
            role = UserRole.USER,
            password = "OTP_VERIFIED",
            hasFreeUpload = true
        )
        _currentUser.value = user
        if (existing == null) {
            _registeredUsers.value = listOf(user) + _registeredUsers.value
        }
        return user
    }

    // User Management Actions for Super Admin
    fun blockUser(uid: String) {
        _registeredUsers.value = _registeredUsers.value.map { user ->
            if (user.uid == uid) user.copy(isBlocked = true) else user
        }
        // Force logout if the current logged-in user was blocked
        if (_currentUser.value?.uid == uid) {
            logout()
        }
    }

    fun unblockUser(uid: String) {
        _registeredUsers.value = _registeredUsers.value.map { user ->
            if (user.uid == uid) user.copy(isBlocked = false) else user
        }
    }

    fun forceOutUser(uid: String) {
        if (_currentUser.value?.uid == uid) {
            logout()
        }
    }

    fun toggleFreeUpload(uid: String) {
        _registeredUsers.value = _registeredUsers.value.map { user ->
            if (user.uid == uid) user.copy(hasFreeUpload = !user.hasFreeUpload) else user
        }
        if (_currentUser.value?.uid == uid) {
            val updated = _registeredUsers.value.find { it.uid == uid }
            if (updated != null) _currentUser.value = updated
        }
    }

    fun switchRoleForTesting(newRole: UserRole) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(role = newRole)
    }

    fun logout() {
        _currentUser.value = UserAccount(
            uid = "guest_001",
            email = "guest@moonask.store",
            displayName = "Guest Explorer",
            role = UserRole.GUEST,
            hasFreeUpload = false
        )
    }

    fun toggleFavorite(appId: String) {
        val current = _favorites.value
        _favorites.value = if (current.contains(appId)) current - appId else current + appId
    }

    fun addRecentSearch(query: String) {
        if (query.isBlank()) return
        val current = _recentSearches.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        _recentSearches.value = current.take(10)
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    // Store Display Mode: Show only real user-uploaded apps when available
    private val _showOnlyRealApps = MutableStateFlow(false)
    val showOnlyRealApps: StateFlow<Boolean> = _showOnlyRealApps.asStateFlow()

    fun toggleShowOnlyRealApps() {
        _showOnlyRealApps.value = !_showOnlyRealApps.value
    }

    fun setShowOnlyRealApps(onlyReal: Boolean) {
        _showOnlyRealApps.value = onlyReal
    }

    fun clearDemoApps() {
        _apps.value = _apps.value.filter { it.isUserUploaded || it.isAdminSpecial }
    }

    // Developer App Creation with real APK checksum, local storage path, and Admin Special App support
    fun submitApp(
        name: String,
        packageName: String,
        categoryId: String,
        shortDesc: String,
        fullDesc: String,
        iconUrl: String,
        versionName: String,
        versionCode: Int,
        sizeMb: Float,
        permissions: List<String>,
        whatsNew: String,
        apkUrl: String,
        sha256Checksum: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        isUserUploaded: Boolean = true,
        localApkPath: String = "",
        isAdminSpecial: Boolean = false,
        specialAccessPasscode: String = ""
    ): StoreApp {
        val user = _currentUser.value
        val effectivePasscode = if (isAdminSpecial) {
            specialAccessPasscode.ifBlank { _adminSpecialPasscode.value }
        } else ""

        val newApp = StoreApp(
            id = "app_${UUID.randomUUID().toString().take(8)}",
            name = name,
            packageName = packageName,
            categoryId = categoryId,
            shortDescription = shortDesc,
            fullDescription = fullDesc,
            iconUrl = iconUrl.ifBlank { "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200" },
            screenshots = listOf(
                "https://images.unsplash.com/photo-1551650975-87deedd944c3?w=600",
                "https://images.unsplash.com/photo-1526470608268-f674ce90ebd4?w=600"
            ),
            developerName = if (isAdminSpecial) "MoonAsk Super Admin" else (user?.displayName ?: "Independent Creator"),
            developerUid = user?.uid ?: "dev_unknown",
            sizeMb = sizeMb,
            versionName = versionName,
            versionCode = versionCode,
            whatsNew = whatsNew,
            permissions = permissions,
            apkDownloadUrl = apkUrl,
            sha256Checksum = sha256Checksum,
            isUserUploaded = isUserUploaded,
            localApkPath = localApkPath,
            isAdminSpecial = isAdminSpecial,
            specialAccessPasscode = effectivePasscode,
            publicationStatus = if (user?.role == UserRole.ADMIN) PublicationStatus.PUBLISHED else PublicationStatus.PENDING_REVIEW,
            submittedAt = System.currentTimeMillis()
        )

        _apps.value = listOf(newApp) + _apps.value
        return newApp
    }

    // Super Admin Actions
    fun approveAndPublishApp(appId: String) {
        _apps.value = _apps.value.map { app ->
            if (app.id == appId) app.copy(publicationStatus = PublicationStatus.PUBLISHED, rejectionReason = "") else app
        }
        // Auto-enable real apps mode if an uploaded app is published
        val app = _apps.value.find { it.id == appId }
        if (app?.isUserUploaded == true) {
            _showOnlyRealApps.value = true
        }
    }

    fun rejectApp(appId: String, reason: String) {
        _apps.value = _apps.value.map { app ->
            if (app.id == appId) app.copy(publicationStatus = PublicationStatus.REJECTED, rejectionReason = reason) else app
        }
    }

    fun requestChanges(appId: String, reason: String) {
        _apps.value = _apps.value.map { app ->
            if (app.id == appId) app.copy(publicationStatus = PublicationStatus.CHANGES_REQUESTED, rejectionReason = reason) else app
        }
    }

    fun toggleFeaturedApp(appId: String) {
        _apps.value = _apps.value.map { app ->
            if (app.id == appId) app.copy(isFeatured = !app.isFeatured) else app
        }
    }

    fun addReview(appId: String, rating: Int, comment: String) {
        val user = _currentUser.value ?: return
        val newReview = AppReview(
            id = "rev_${UUID.randomUUID().toString().take(8)}",
            appId = appId,
            userId = user.uid,
            authorName = user.displayName,
            rating = rating,
            comment = comment,
            date = "Just now"
        )
        val currentReviews = _reviews.value[appId] ?: emptyList()
        _reviews.value = _reviews.value + (appId to (listOf(newReview) + currentReviews))

        // Update rating in StoreApp
        val allAppReviews = _reviews.value[appId] ?: emptyList()
        val avgRating = allAppReviews.map { it.rating }.average().toFloat()
        _apps.value = _apps.value.map {
            if (it.id == appId) it.copy(rating = avgRating, reviewCount = allAppReviews.size) else it
        }
    }

    fun submitReport(appId: String, appName: String, reason: String, description: String) {
        val user = _currentUser.value
        val report = AppReport(
            id = "rep_${UUID.randomUUID().toString().take(8)}",
            appId = appId,
            appName = appName,
            reporterUid = user?.uid ?: "anonymous",
            reason = reason,
            description = description
        )
        _reports.value = listOf(report) + _reports.value
    }

    fun toggleLikeReel(reelId: String) {
        _reels.value = _reels.value.map { reel ->
            if (reel.id == reelId) {
                val nextIsLiked = !reel.isLiked
                val delta = if (nextIsLiked) 1 else -1
                reel.copy(isLiked = nextIsLiked, likesCount = (reel.likesCount + delta).coerceAtLeast(0))
            } else reel
        }
    }

    private fun initialApps(): List<StoreApp> {
        return listOf(
            StoreApp(
                id = "app_1",
                name = "Apex Notes & Journal",
                packageName = "com.moonask.apexnotes",
                categoryId = "productivity",
                shortDescription = "Private markdown notes, task checklists, and offline synchronization.",
                fullDescription = "Apex Notes is designed for seamless thought capture. Featuring full markdown formatting, bi-directional linking, end-to-end local encryption, tag filtering, and instant export to PDF and text. Zero tracking, works 100% offline.",
                iconUrl = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600",
                    "https://images.unsplash.com/photo-1584438784894-089d6a62b8fa?w=600"
                ),
                developerName = "Apex Labs",
                developerUid = "dev_apex",
                rating = 4.8f,
                reviewCount = 384,
                downloadCount = 18400,
                sizeMb = 11.4f,
                versionName = "2.4.1",
                versionCode = 24,
                whatsNew = "Added dark OLED theme, fast search indexing, and automated backup options.",
                permissions = listOf("INTERNET", "ACCESS_NETWORK_STATE", "POST_NOTIFICATIONS"),
                isFeatured = true,
                isEditorChoice = true
            ),
            StoreApp(
                id = "app_2",
                name = "Cipher VPN Shield",
                packageName = "com.moonask.ciphervpn",
                categoryId = "tools",
                shortDescription = "Zero-log encrypted VPN tunnel with split tunneling and kill switch.",
                fullDescription = "Protect your mobile privacy on public Wi-Fi networks with high-speed WireGuard protocols. No tracking, built-in DNS leak protection, and customizable split-tunneling per application.",
                iconUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=600",
                    "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600"
                ),
                developerName = "Cipher Networks",
                developerUid = "dev_cipher",
                rating = 4.7f,
                reviewCount = 920,
                downloadCount = 42000,
                sizeMb = 18.6f,
                versionName = "3.1.0",
                versionCode = 31,
                whatsNew = "Improved connection speed by 40% and updated server certificates.",
                permissions = listOf("INTERNET", "ACCESS_NETWORK_STATE", "BIND_VPN_SERVICE"),
                isFeatured = true
            ),
            StoreApp(
                id = "app_3",
                name = "Nova Media Studio",
                packageName = "com.moonask.novamedia",
                categoryId = "entertainment",
                shortDescription = "Lossless audio visualizer, 10-band equalizer, and universal video player.",
                fullDescription = "Nova Media Studio plays any media format including MKV, FLAC, MP4, and Opus. Features HW acceleration, subtitle styling, background audio playback, and floating picture-in-picture mode.",
                iconUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
                    "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600"
                ),
                developerName = "Aura Soundworks",
                developerUid = "dev_aura",
                rating = 4.6f,
                reviewCount = 215,
                downloadCount = 12900,
                sizeMb = 26.8f,
                versionName = "1.8.2",
                versionCode = 18,
                whatsNew = "Added support for FLAC 24-bit studio audio and Chromecast streaming.",
                permissions = listOf("INTERNET", "RECORD_AUDIO", "MODIFY_AUDIO_SETTINGS"),
                isFeatured = true
            ),
            StoreApp(
                id = "app_4",
                name = "Pulse Fitness Tracker",
                packageName = "com.moonask.pulsefit",
                categoryId = "health",
                shortDescription = "Real-time pedometer, interval workout coach, and hydration reminder.",
                fullDescription = "Track your daily steps, active workout calories, and heart zones with low battery consumption. Syncs with standard health sensors and provides motivating weekly milestones.",
                iconUrl = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=600"
                ),
                developerName = "Pulse Tech",
                developerUid = "dev_pulse",
                rating = 4.5f,
                reviewCount = 148,
                downloadCount = 8900,
                sizeMb = 15.2f,
                versionName = "2.0.0",
                versionCode = 20,
                whatsNew = "New HIIT timer module with voice cues and hydration counter.",
                permissions = listOf("ACTIVITY_RECOGNITION", "ACCESS_FINE_LOCATION"),
                isFeatured = false
            ),
            StoreApp(
                id = "app_5",
                name = "Quantum Chess Tactics",
                packageName = "com.moonask.quantumchess",
                categoryId = "games",
                shortDescription = "Next-gen tactical chess with puzzle trainer and offline AI grandmaster.",
                fullDescription = "Sharpen your mind with over 10,000 tactical puzzles, deep Stockfish engine analysis, customizable piece boards, and pass-and-play local multiplayer.",
                iconUrl = "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1580541832626-2a7131ee809f?w=600"
                ),
                developerName = "Mindspark Games",
                developerUid = "dev_mindspark",
                rating = 4.9f,
                reviewCount = 610,
                downloadCount = 31000,
                sizeMb = 34.5f,
                versionName = "4.2.0",
                versionCode = 42,
                whatsNew = "Added 500 new endgame tactical puzzles and improved neural AI speed.",
                permissions = listOf("INTERNET", "VIBRATE"),
                isFeatured = true
            ),
            StoreApp(
                id = "app_6",
                name = "Skyline Weather Radar",
                packageName = "com.moonask.skylineweather",
                categoryId = "utilities",
                shortDescription = "Hyper-local precipitation radar, hourly air quality index, and storm alerts.",
                fullDescription = "Stay ahead of storms with animated Doppler radar, UV index warnings, customizable home screen widgets, and severe weather push notifications.",
                iconUrl = "https://images.unsplash.com/photo-1504608524841-42fe6f032b4b?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1534088568595-a066f410bcda?w=600"
                ),
                developerName = "Atmosphere Labs",
                developerUid = "dev_atmo",
                rating = 4.4f,
                reviewCount = 95,
                downloadCount = 6700,
                sizeMb = 9.8f,
                versionName = "1.3.4",
                versionCode = 13,
                whatsNew = "Added dark widget styles and high-resolution wind velocity maps.",
                permissions = listOf("ACCESS_COARSE_LOCATION", "INTERNET"),
                isFeatured = false
            ),
            StoreApp(
                id = "app_7",
                name = "Echo Recorder Pro",
                packageName = "com.moonask.echorecorder",
                categoryId = "communication",
                shortDescription = "Noise-cancelling voice notes and automatic meeting transcription.",
                fullDescription = "Capture crystal-clear audio with intelligent background noise suppression, waveform visualizer, audio bookmarking, and on-device transcription.",
                iconUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600"
                ),
                developerName = "Echo Studio",
                developerUid = "dev_echo",
                rating = 4.3f,
                reviewCount = 74,
                downloadCount = 4100,
                sizeMb = 12.1f,
                versionName = "1.1.0",
                versionCode = 11,
                whatsNew = "Audio export to WAV and MP3 with custom bitrate control.",
                permissions = listOf("RECORD_AUDIO", "INTERNET"),
                publicationStatus = PublicationStatus.PENDING_REVIEW,
                submittedAt = System.currentTimeMillis() - 3600000
            ),
            StoreApp(
                id = "app_special_1",
                name = "MoonAsk Admin Terminal & Toolkit",
                packageName = "com.moonask.admintoolkit",
                categoryId = "tools",
                shortDescription = "Super Admin internal diagnostics, telemetry inspector, and kernel logger.",
                fullDescription = "Exclusive internal administration suite published by MoonAsk Store Super Admin. Requires the master Admin Special Passcode to unlock and install.",
                iconUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=200",
                screenshots = listOf(
                    "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=600"
                ),
                developerName = "MoonAsk Super Admin",
                developerUid = "admin_sk_nijam",
                rating = 5.0f,
                reviewCount = 12,
                downloadCount = 850,
                sizeMb = 8.4f,
                versionName = "1.0.0",
                versionCode = 1,
                whatsNew = "Internal release for authorized administration staff.",
                permissions = listOf("INTERNET", "ACCESS_NETWORK_STATE"),
                isAdminSpecial = true,
                specialAccessPasscode = "7996",
                publicationStatus = PublicationStatus.PUBLISHED,
                submittedAt = System.currentTimeMillis() - 7200000
            )
        )
    }

    private fun initialUsers(): List<UserAccount> {
        return listOf(
            UserAccount(
                uid = "admin_sk_nijam",
                email = SUPER_ADMIN_EMAIL,
                displayName = "SK Nijam (Super Admin)",
                role = UserRole.ADMIN,
                password = SUPER_ADMIN_PASSWORD,
                isBlocked = false,
                hasFreeUpload = true,
                registeredAt = System.currentTimeMillis() - 86400000L * 30
            ),
            UserAccount(
                uid = "user_dev_rahul",
                email = "rahul.dev@gmail.com",
                displayName = "Rahul Sharma (Developer)",
                role = UserRole.DEVELOPER,
                password = "Password@123",
                isBlocked = false,
                hasFreeUpload = true,
                registeredAt = System.currentTimeMillis() - 86400000L * 7
            ),
            UserAccount(
                uid = "user_priya_k",
                email = "priya.k@example.com",
                displayName = "Priya Kumar",
                role = UserRole.USER,
                password = "UserPriya@2024",
                isBlocked = false,
                hasFreeUpload = false,
                registeredAt = System.currentTimeMillis() - 86400000L * 2
            )
        )
    }

    private fun initialCategories(): List<AppCategory> {
        return listOf(
            AppCategory("tools", "Tools & Utilities", "build", "File managers, network testers, battery monitors", 18),
            AppCategory("productivity", "Productivity", "task_alt", "Note taking, planners, document editors", 24),
            AppCategory("games", "Games & Puzzles", "sports_esports", "Action, tactical chess, puzzle simulations", 32),
            AppCategory("entertainment", "Entertainment", "movie", "Media players, streaming players, podcasts", 15),
            AppCategory("communication", "Communication", "chat", "Messaging tools, video calls, voice recorders", 12),
            AppCategory("health", "Health & Fitness", "fitness_center", "Workout loggers, pedometers, health trackers", 9),
            AppCategory("education", "Education & Books", "school", "Flashcards, reference encyclopedias, tutorials", 14),
            AppCategory("photography", "Photography", "photo_camera", "Raw photo editors, filters, camera utilities", 11)
        )
    }

    private fun initialReels(): List<AppReel> {
        return listOf(
            AppReel(
                id = "reel_1",
                creatorUid = "dev_apex",
                creatorName = "Apex Studio",
                creatorAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                title = "Markdown Workflow in Apex Notes 📝",
                description = "Watch how fast you can create checklists and link knowledge notes with zero lag on MoonAsk Store!",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-young-woman-typing-on-a-laptop-4050-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=600",
                linkedAppId = "app_1",
                linkedAppName = "Apex Notes & Journal",
                likesCount = 524
            ),
            AppReel(
                id = "reel_2",
                creatorUid = "dev_cipher",
                creatorName = "Cyber Shield Guy",
                creatorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100",
                title = "Why you need Cipher VPN on Public Wi-Fi 🛡️",
                description = "Demonstrating how DNS leak protection prevents packet sniffing in coffee shops.",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-hands-typing-on-a-laptop-keyboard-4171-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1563986768609-322da13575f3?w=600",
                linkedAppId = "app_2",
                linkedAppName = "Cipher VPN Shield",
                likesCount = 890
            ),
            AppReel(
                id = "reel_3",
                creatorUid = "dev_mindspark",
                creatorName = "Grandmaster Tactician",
                creatorAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100",
                title = "Insane Queen Sacrifice Puzzle! ♟️",
                description = "Solve this endgame puzzle in Quantum Chess Tactics. Available now on MoonAsk!",
                videoUrl = "https://assets.mixkit.co/videos/preview/mixkit-top-view-of-a-chess-board-4122-large.mp4",
                thumbnailUrl = "https://images.unsplash.com/photo-1529699211952-734e80c4d42b?w=600",
                linkedAppId = "app_5",
                linkedAppName = "Quantum Chess Tactics",
                likesCount = 1240
            )
        )
    }

    private fun initialReviews(): Map<String, List<AppReview>> {
        return mapOf(
            "app_1" to listOf(
                AppReview("r1", "app_1", "u1", "Amina Patel", 5, "Best offline markdown editor I've tried. Synced instantly and uses virtually no battery.", "2 days ago"),
                AppReview("r2", "app_1", "u2", "David Vance", 5, "Love the bi-directional links and export to PDF. Very clean UI.", "1 week ago")
            ),
            "app_2" to listOf(
                AppReview("r3", "app_2", "u3", "Carlos Mendes", 5, "The kill switch works perfectly. Speeds are nearly identical to my raw connection.", "3 days ago")
            )
        )
    }

    private fun initialReports(): List<AppReport> {
        return listOf(
            AppReport("rep_1", "app_7", "Echo Recorder Pro", "user_992", "Misleading permissions", "Requests internet permission but claims 100% offline.")
        )
    }
}
