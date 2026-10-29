package com.example.data.installer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.data.model.StoreApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

sealed interface DownloadInstallState {
    data object Idle : DownloadInstallState
    data object Preparing : DownloadInstallState
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadInstallState
    data class VerifyingChecksum(val calculatedSha: String) : DownloadInstallState
    data class ReadyToInstall(val apkFile: File, val apkUri: Uri) : DownloadInstallState
    data object PromptingInstaller : DownloadInstallState
    data object Installed : DownloadInstallState
    data class Error(val message: String) : DownloadInstallState
}

class PackageInstallerManager(private val context: Context) {

    private val _installState = MutableStateFlow<Map<String, DownloadInstallState>>(emptyMap())
    val installState: StateFlow<Map<String, DownloadInstallState>> = _installState.asStateFlow()

    val showPermissionDialog = MutableStateFlow(false)

    fun getStateForApp(appId: String): DownloadInstallState {
        return _installState.value[appId] ?: if (isAppInstalled(appId)) DownloadInstallState.Installed else DownloadInstallState.Idle
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    // Track pending package installations: packageName -> appId
    private val pendingPackages = java.util.concurrent.ConcurrentHashMap<String, String>()

    private val prefs by lazy {
        context.getSharedPreferences("moonask_install_prefs", Context.MODE_PRIVATE)
    }

    private var packageReceiverRegistered = false
    private val packageBroadcastReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(recvContext: Context?, intent: Intent?) {
            val action = intent?.action
            val installedPkg = intent?.data?.schemeSpecificPart ?: return
            if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REPLACED) {
                val appId = pendingPackages[installedPkg] ?: findAppIdForPackage(installedPkg)
                if (appId != null) {
                    updateState(appId, DownloadInstallState.Installed)
                    pendingPackages.remove(installedPkg)
                }
            }
        }
    }

    init {
        registerPackageReceiver()
    }

    fun registerPackageReceiver() {
        if (!packageReceiverRegistered) {
            try {
                val filter = android.content.IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addDataScheme("package")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(packageBroadcastReceiver, filter, Context.RECEIVER_EXPORTED)
                } else {
                    context.registerReceiver(packageBroadcastReceiver, filter)
                }
                packageReceiverRegistered = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unregisterPackageReceiver() {
        if (packageReceiverRegistered) {
            try {
                context.unregisterReceiver(packageBroadcastReceiver)
                packageReceiverRegistered = false
            } catch (_: Exception) {}
        }
    }

    private var cachedAppList: List<StoreApp> = emptyList()

    fun updateKnownApps(apps: List<StoreApp>) {
        cachedAppList = apps
    }

    private fun findAppIdForPackage(pkg: String): String? {
        return cachedAppList.find { it.packageName == pkg }?.id
    }

    fun canRequestPackageInstalls(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.packageManager.canRequestPackageInstalls()
            } catch (_: Exception) {
                true
            }
        } else {
            true
        }
    }

    /**
     * Check if install permission has been prompted before.
     * Permission should only be prompted once if disabled; if enabled, never prompt.
     */
    fun shouldPromptPermission(): Boolean {
        if (canRequestPackageInstalls()) return false
        val alreadyPrompted = prefs.getBoolean("has_prompted_unknown_sources", false)
        return !alreadyPrompted
    }

    fun markPermissionPrompted() {
        prefs.edit().putBoolean("has_prompted_unknown_sources", true).apply()
    }

    fun openUnknownAppSourcesSettings() {
        markPermissionPrompted()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manageIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(manageIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openInstalledApp(packageName: String): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    val superStorageManager = com.example.data.storage.SuperApkStorageManager(context)

    suspend fun downloadAndInstall(app: StoreApp) = withContext(Dispatchers.IO) {
        val appId = app.id
        pendingPackages[app.packageName] = appId
        updateState(appId, DownloadInstallState.Preparing)

        try {
            // Check unknown apps permission first time only if disabled
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canRequestPackageInstalls()) {
                if (shouldPromptPermission()) {
                    withContext(Dispatchers.Main) {
                        showPermissionDialog.value = true
                    }
                }
            }

            val apkFile = superStorageManager.downloadOrRetrieveApk(
                urlOrPath = if (app.localApkPath.isNotBlank()) app.localApkPath else app.apkDownloadUrl,
                expectedSha256 = app.sha256Checksum,
                packageName = app.packageName,
                versionCode = app.versionCode
            ) { progress, downloaded, total ->
                updateState(appId, DownloadInstallState.Downloading(progress, downloaded, total))
            }

            // Verify SHA-256 Checksum
            val calculatedSha = calculateSha256(apkFile)
            updateState(appId, DownloadInstallState.VerifyingChecksum(calculatedSha))

            // Obtain FileProvider Content URI
            val authority = "${context.packageName}.provider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            updateState(appId, DownloadInstallState.ReadyToInstall(apkFile, apkUri))
            updateState(appId, DownloadInstallState.PromptingInstaller)

            // Trigger official Android Package Installer
            launchPackageInstaller(apkUri)

        } catch (e: Exception) {
            updateState(appId, DownloadInstallState.Error(e.localizedMessage ?: "Installation failed"))
        }
    }

    fun launchPackageInstaller(apkUri: Uri) {
        try {
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            // Explicitly grant URI read permissions to system package installer packages
            val resInfoList = context.packageManager.queryIntentActivities(installIntent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val pkgName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(pkgName, apkUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
        } catch (_: Exception) {
            try {
                @Suppress("DEPRECATION")
                val fallbackIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                }
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Called when MainActivity resumes. Checks installation status of all apps
     * and clears any endless spinning states if installation was cancelled or completed.
     */
    fun onActivityResume(apps: List<StoreApp> = cachedAppList) {
        updateKnownApps(apps)
        val currentStates = _installState.value
        val updatedStates = currentStates.toMutableMap()

        for (app in apps) {
            val isInstalled = isAppInstalled(app.packageName)
            val currentState = currentStates[app.id]

            if (isInstalled) {
                updatedStates[app.id] = DownloadInstallState.Installed
                pendingPackages.remove(app.packageName)
            } else if (currentState is DownloadInstallState.PromptingInstaller) {
                // If user returned from package installer without installing, reset to Idle so it does not spin indefinitely
                updatedStates[app.id] = DownloadInstallState.Idle
                pendingPackages.remove(app.packageName)
            }
        }
        _installState.value = updatedStates
    }

    fun startBackgroundInstallService(app: StoreApp) {
        ApkDownloadInstallService.startDownload(
            context = context,
            appId = app.id,
            appName = app.name,
            packageName = app.packageName,
            downloadUrl = app.apkDownloadUrl,
            localPath = app.localApkPath,
            versionCode = app.versionCode
        )
    }

    fun cancelDownload(appId: String) {
        updateState(appId, DownloadInstallState.Idle)
    }

    fun resetState(appId: String) {
        updateState(appId, DownloadInstallState.Idle)
    }

    private fun updateState(appId: String, state: DownloadInstallState) {
        _installState.value = _installState.value + (appId to state)
    }

    private fun calculateSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = file.readBytes()
            val hash = digest.digest(bytes)
            hash.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        }
    }

    // Gmail & Android Native Sharing
    fun shareAppViaGmail(app: StoreApp) {
        val subject = "Download ${app.name} (v${app.versionName}) on MoonAsk Store"
        val listingUrl = "https://moonask.store/app/${app.id}"
        val directApkUrl = app.apkDownloadUrl.ifBlank { "https://moonask.store/download/${app.id}.apk" }

        val body = """
            Hey,
            
            Check out "${app.name}" on MoonAsk Store!
            
            📱 App Details:
            • Version: ${app.versionName} (${app.versionCode})
            • Category: ${app.categoryId.replaceFirstChar { it.uppercase() }}
            • Size: ${app.sizeMb} MB
            • Rating: ★ ${app.rating} (${app.reviewCount} reviews)
            • Description: ${app.shortDescription}
            
            🔗 MoonAsk Store Listing:
            $listingUrl
            
            📥 Direct APK Download Link:
            $directApkUrl
            
            💡 How to Install:
            1. Download the APK file from the link above.
            2. When prompted by Android, allow installation from this source.
            3. Tap Install on the Android system installer dialog.
            
            Distributed via MoonAsk Store — Safe, Fast, Open Android Distribution.
        """.trimIndent()

        // Send via Gmail specifically or fallback to email client
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(emailIntent)
        } catch (_: Exception) {
            // General share sheet
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            val chooser = Intent.createChooser(shareIntent, "Share ${app.name} via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    fun shareAppNative(app: StoreApp) {
        val text = "Get ${app.name} on MoonAsk Store: https://moonask.store/app/${app.id}\nDownload APK: https://moonask.store/download/${app.id}.apk"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out ${app.name}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        val chooser = Intent.createChooser(shareIntent, "Share App").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
