package com.example.data.model

data class StoreApp(
    val id: String,
    val name: String,
    val packageName: String,
    val categoryId: String,
    val shortDescription: String,
    val fullDescription: String,
    val iconUrl: String,
    val screenshots: List<String>,
    val developerName: String,
    val developerUid: String,
    val rating: Float = 4.5f,
    val reviewCount: Int = 120,
    val downloadCount: Int = 5400,
    val sizeMb: Float = 14.2f,
    val versionName: String = "1.0.0",
    val versionCode: Int = 1,
    val whatsNew: String = "Initial stable release on MoonAsk Store.",
    val minAndroidVersion: String = "Android 8.0 (API 26)",
    val targetAndroidVersion: String = "Android 14 (API 34)",
    val permissions: List<String> = listOf("INTERNET", "ACCESS_NETWORK_STATE"),
    val apkDownloadUrl: String = "",
    val sha256Checksum: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val isFeatured: Boolean = false,
    val isEditorChoice: Boolean = false,
    val publicationStatus: PublicationStatus = PublicationStatus.PUBLISHED,
    val rejectionReason: String = "",
    val privacyPolicyUrl: String = "https://moonask.store/privacy",
    val supportEmail: String = "support@moonask.store",
    val submittedAt: Long = System.currentTimeMillis(),
    val isUserUploaded: Boolean = false,
    val localApkPath: String = "",
    val isAdminSpecial: Boolean = false,
    val specialAccessPasscode: String = ""
)

enum class PublicationStatus {
    DRAFT,
    PENDING_REVIEW,
    CHANGES_REQUESTED,
    APPROVED,
    PUBLISHED,
    REJECTED,
    SUSPENDED
}

data class AppCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val description: String,
    val appCount: Int = 0
)

data class AppReview(
    val id: String,
    val appId: String,
    val userId: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

data class AppReel(
    val id: String,
    val creatorUid: String,
    val creatorName: String,
    val creatorAvatar: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val linkedAppId: String,
    val linkedAppName: String,
    val likesCount: Int = 342,
    val isLiked: Boolean = false
)

enum class UserRole {
    GUEST,
    USER,
    DEVELOPER,
    ADMIN
}

data class UserAccount(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val password: String = "",
    val isBlocked: Boolean = false,
    val hasFreeUpload: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
)

data class AppReport(
    val id: String,
    val appId: String,
    val appName: String,
    val reporterUid: String,
    val reason: String,
    val description: String,
    val status: String = "PENDING",
    val date: Long = System.currentTimeMillis()
)
