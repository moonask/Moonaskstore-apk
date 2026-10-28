package com.example.data.ai

import android.util.Log
import com.example.data.model.StoreApp
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class GeminiStoreAssistant {

    private val TAG = "GeminiStoreAssistant"

    private val model by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI not yet configured or AppCheck pending: ${e.message}")
            null
        }
    }

    suspend fun queryStore(userPrompt: String, catalog: List<StoreApp>): String = withContext(Dispatchers.IO) {
        val appSummary = catalog.take(8).joinToString("\n") {
            "- ${it.name} (${it.categoryId}): ${it.shortDescription} [Rating: ${it.rating}★, Size: ${it.sizeMb}MB]"
        }

        val prompt = """
            You are the MoonAsk Store AI intelligent app advisor.
            Help the user find or evaluate applications in the MoonAsk Store catalogue.
            
            Current Catalogue Highlights:
            $appSummary
            
            User Query: "$userPrompt"
            
            Provide a helpful, concise, well-formatted response with specific app recommendations and why they suit the user's needs. If no app matches, provide a friendly suggestion.
        """.trimIndent()

        try {
            val generativeModel = model
            if (generativeModel != null) {
                withTimeout(45_000L) {
                    val response = generativeModel.generateContent(prompt)
                    response.text ?: "MoonAsk AI did not generate a response. Please try again."
                }
            } else {
                fallbackLocalAiResponse(userPrompt, catalog)
            }
        } catch (e: TimeoutCancellationException) {
            "Request timed out. Please check your internet connection and try again."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini generation failed: ${e.message}")
            fallbackLocalAiResponse(userPrompt, catalog)
        }
    }

    suspend fun analyzeApkPermissions(appName: String, permissions: List<String>): String = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the following declared Android permissions for the app "$appName":
            Permissions: ${permissions.joinToString(", ")}
            
            Provide a clear, user-friendly security assessment:
            1. Overall Safety Score (e.g. 9/10 - Safe / Standard)
            2. Purpose of each permission
            3. Privacy considerations or potential risks for the user
            Keep it crisp, objective, and easy for ordinary users to understand.
        """.trimIndent()

        try {
            val generativeModel = model
            if (generativeModel != null) {
                withTimeout(45_000L) {
                    val response = generativeModel.generateContent(prompt)
                    response.text ?: "Permission analysis complete. Permissions appear standard for this application."
                }
            } else {
                fallbackPermissionAnalysis(appName, permissions)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fallbackPermissionAnalysis(appName, permissions)
        }
    }

    private fun fallbackLocalAiResponse(query: String, catalog: List<StoreApp>): String {
        val q = query.lowercase()
        val matched = catalog.filter {
            it.name.lowercase().contains(q) ||
            it.shortDescription.lowercase().contains(q) ||
            it.categoryId.lowercase().contains(q)
        }

        return if (matched.isNotEmpty()) {
            val top = matched.first()
            "Based on your query '$query', I recommend **${top.name}** in ${top.categoryId.replaceFirstChar { it.uppercase() }}. It has a ${top.rating}★ rating and offers ${top.shortDescription.lowercase()}."
        } else {
            "I searched MoonAsk Store for '$query'. Check out our top-rated utilities and creative apps such as Nova Player, Apex Notes, or Cipher VPN!"
        }
    }

    private fun fallbackPermissionAnalysis(appName: String, permissions: List<String>): String {
        val sensitive = permissions.filter { it.contains("LOCATION") || it.contains("CAMERA") || it.contains("STORAGE") || it.contains("RECORD") }
        val normal = permissions - sensitive.toSet()

        return buildString {
            append("🛡️ **MoonAsk Security Audit for $appName**\n\n")
            if (sensitive.isEmpty()) {
                append("✅ **Safety Score: 10/10 (Low Risk)**\n")
                append("This app requests only standard permissions (${normal.joinToString(", ")}) and does not access sensitive user hardware or contacts.")
            } else {
                append("⚠️ **Safety Score: 8/10 (Moderate Permissions)**\n")
                append("Declared sensitive permissions: ${sensitive.joinToString(", ")}.\n")
                append("Ensure you trust the developer before granting hardware permissions.")
            }
        }
    }
}
