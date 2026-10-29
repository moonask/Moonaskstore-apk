package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.ai.GeminiStoreAssistant
import com.example.data.installer.PackageInstallerManager
import com.example.data.model.AppThemeMode
import com.example.data.repository.StoreRepository
import com.example.ui.navigation.MoonAskApp
import com.example.ui.theme.MoonAskTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { StoreRepository() }
    private val installerManager by lazy { PackageInstallerManager(applicationContext) }
    private val aiAssistant by lazy { GeminiStoreAssistant() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic background Worker checking Firestore for newer app version codes
        com.example.data.worker.AppUpdateWorkScheduler.schedulePeriodicUpdateCheck(applicationContext)

        val targetAppId = intent?.getStringExtra("EXTRA_APP_ID")

        setContent {
            val themeMode by repository.themeMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            MoonAskTheme(darkTheme = isDark) {
                MoonAskApp(
                    repository = repository,
                    installerManager = installerManager,
                    aiAssistant = aiAssistant,
                    initialAppId = targetAppId
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        installerManager.onActivityResume(repository.apps.value)
    }

    override fun onDestroy() {
        super.onDestroy()
        installerManager.unregisterPackageReceiver()
    }
}
