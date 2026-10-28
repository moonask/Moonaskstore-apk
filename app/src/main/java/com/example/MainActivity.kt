package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.ai.GeminiStoreAssistant
import com.example.data.installer.PackageInstallerManager
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

        setContent {
            MoonAskTheme {
                MoonAskApp(
                    repository = repository,
                    installerManager = installerManager,
                    aiAssistant = aiAssistant
                )
            }
        }
    }
}
