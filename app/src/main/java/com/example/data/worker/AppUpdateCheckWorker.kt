package com.example.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.data.model.PublicationStatus
import com.example.data.model.StoreApp
import com.example.data.repository.StoreRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background CoroutineWorker that queries Firestore (and repository catalog)
 * for newer version codes of apps installed on the user's device and triggers
 * system notifications to inform the user to update.
 */
class AppUpdateCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TAG = "AppUpdateCheckWorker"
        const val CHANNEL_ID = "moonask_app_updates"
        const val CHANNEL_NAME = "MoonAsk App Updates"
        const val UNIQUE_PERIODIC_WORK_NAME = "moonask_periodic_update_checker"
        const val UNIQUE_ONE_TIME_WORK_NAME = "moonask_instant_update_checker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "Starting background check for installed app updates in Firestore...")

        createNotificationChannel()

        // 1. Retrieve all packages installed on this device
        val installedPackagesMap = getInstalledPackagesMap()
        Log.d(TAG, "Found ${installedPackagesMap.size} installed packages on device.")

        // 2. Fetch published applications from Firestore (with repository fallback)
        val publishedStoreApps = fetchStoreAppsFromFirestore()
        Log.d(TAG, "Retrieved ${publishedStoreApps.size} published apps to check against installed versions.")

        var updatesFoundCount = 0

        for (storeApp in publishedStoreApps) {
            val installedVersionCode = installedPackagesMap[storeApp.packageName]
            if (installedVersionCode != null) {
                // If the store version code is strictly greater than the on-device installed version code
                if (storeApp.versionCode > installedVersionCode) {
                    updatesFoundCount++
                    Log.i(TAG, "Update detected for ${storeApp.name} (${storeApp.packageName}): Installed=$installedVersionCode, Store=${storeApp.versionCode}")
                    postUpdateNotification(storeApp, installedVersionCode)
                }
            }
        }

        Log.i(TAG, "App update check completed. Discovered $updatesFoundCount update(s).")
        Result.success()
    }

    /**
     * Map of installed packages to their integer version codes.
     */
    private fun getInstalledPackagesMap(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        try {
            val packages: List<PackageInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstalledPackages(0)
            }

            for (pkg in packages) {
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkg.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    pkg.versionCode
                }
                map[pkg.packageName] = code
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enumerating installed packages: ${e.message}")
        }
        return map
    }

    /**
     * Query Firestore for published applications; falls back to StoreRepository default catalog.
     */
    private suspend fun fetchStoreAppsFromFirestore(): List<StoreApp> {
        val appsList = mutableListOf<StoreApp>()
        try {
            val firestore = Firebase.firestore
            val snapshot = firestore.collection("apps")
                .whereEqualTo("publicationStatus", PublicationStatus.PUBLISHED.name)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val id = doc.getString("id") ?: doc.id
                val name = doc.getString("name") ?: ""
                val packageName = doc.getString("packageName") ?: ""
                val versionCode = (doc.getLong("versionCode") ?: 1L).toInt()
                val versionName = doc.getString("versionName") ?: "1.0.0"
                val categoryId = doc.getString("categoryId") ?: "tools"
                val shortDesc = doc.getString("shortDescription") ?: ""
                val fullDesc = doc.getString("fullDescription") ?: ""
                val iconUrl = doc.getString("iconUrl") ?: ""
                val sizeMb = (doc.getDouble("sizeMb") ?: 12.0).toFloat()
                val apkUrl = doc.getString("apkDownloadUrl") ?: ""

                if (packageName.isNotBlank()) {
                    appsList.add(
                        StoreApp(
                            id = id,
                            name = name,
                            packageName = packageName,
                            categoryId = categoryId,
                            shortDescription = shortDesc,
                            fullDescription = fullDesc,
                            iconUrl = iconUrl,
                            screenshots = emptyList(),
                            developerName = doc.getString("developerName") ?: "Developer",
                            developerUid = doc.getString("developerUid") ?: "uid",
                            versionName = versionName,
                            versionCode = versionCode,
                            sizeMb = sizeMb,
                            apkDownloadUrl = apkUrl
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore query failed or offline (${e.message}). Using repository catalogue fallback.")
        }

        // If Firestore had no records (e.g., initial local deployment), use StoreRepository catalog
        if (appsList.isEmpty()) {
            val repo = StoreRepository()
            appsList.addAll(repo.apps.value)
        }

        return appsList
    }

    /**
     * Post a rich, clickable system notification to update the app.
     */
    private fun postUpdateNotification(app: StoreApp, installedVersionCode: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to launch MainActivity with routing directly to the app's details
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_TARGET", "DETAILS")
            putExtra("EXTRA_APP_ID", app.id)
            putExtra("EXTRA_UPDATE_AVAILABLE", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            app.packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Update Available: ${app.name} v${app.versionName}")
            .setContentText("Version ${app.versionCode} is now live in MoonAsk Store (installed: v$installedVersionCode). Tap to update now.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${app.name} has a newer version (${app.versionCode} > $installedVersionCode) available on MoonAsk Store.\n\nWhat's new: ${app.whatsNew}\n\nTap to download and install with package installer.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_upload,
                "Update Now",
                pendingIntent
            )
            .build()

        notificationManager.notify(app.packageName.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when newer version codes are published on MoonAsk Store for installed applications."
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

/**
 * Helper to schedule and trigger the background update check Worker.
 */
object AppUpdateWorkScheduler {

    /**
     * Schedule periodic background check every 6 hours with network connectivity requirement.
     */
    fun schedulePeriodicUpdateCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<AppUpdateCheckWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            AppUpdateCheckWorker.UNIQUE_PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        Log.i("AppUpdateScheduler", "Scheduled periodic 6-hour update check worker with WorkManager.")
    }

    /**
     * Trigger an immediate one-time check for updates on demand.
     */
    fun triggerImmediateCheck(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<AppUpdateCheckWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            AppUpdateCheckWorker.UNIQUE_ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
        Log.i("AppUpdateScheduler", "Enqueued one-time update check worker with WorkManager.")
    }
}
