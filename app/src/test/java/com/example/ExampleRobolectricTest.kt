package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PublicationStatus
import com.example.data.model.UserRole
import com.example.data.repository.StoreRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches MoonAsk Store`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MoonAsk Store", appName)
    }

    @Test
    fun `super admin credentials grant admin role`() {
        val repository = StoreRepository()
        val result = repository.loginWithEmail(
            StoreRepository.SUPER_ADMIN_EMAIL,
            StoreRepository.SUPER_ADMIN_PASSWORD
        )

        assertTrue(result.isSuccess)
        val admin = result.getOrNull()
        assertNotNull(admin)
        assertEquals(UserRole.ADMIN, admin?.role)
        assertEquals(StoreRepository.SUPER_ADMIN_EMAIL, admin?.email)
    }

    @Test
    fun `developer app submission and admin approval flow`() {
        val repository = StoreRepository()
        val testSha = "a1b2c3d4e5f678901234567890abcdef1234567890abcdef1234567890abcdef"
        val newApp = repository.submitApp(
            name = "Test Calculator",
            packageName = "com.moonask.testcalc",
            categoryId = "tools",
            shortDesc = "Fast simple calculator",
            fullDesc = "Full calculator description",
            iconUrl = "",
            versionName = "1.0.0",
            versionCode = 1,
            sizeMb = 5.4f,
            permissions = listOf("INTERNET"),
            whatsNew = "Initial release",
            apkUrl = "",
            sha256Checksum = testSha,
            isUserUploaded = true,
            localApkPath = "/dummy/test.apk"
        )

        assertEquals(PublicationStatus.PENDING_REVIEW, newApp.publicationStatus)
        assertEquals(testSha, newApp.sha256Checksum)
        assertTrue(newApp.isUserUploaded)

        // Super Admin approves app
        repository.approveAndPublishApp(newApp.id)
        val published = repository.apps.value.find { it.id == newApp.id }
        assertEquals(PublicationStatus.PUBLISHED, published?.publicationStatus)
        assertTrue(repository.showOnlyRealApps.value)
    }

    @Test
    fun `user management blocking prevents login and unblocking restores access`() {
        val repository = StoreRepository()
        val email = "john.developer@test.com"
        val pass = "Password@999"

        val regResult = repository.loginWithEmail(email, pass)
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()!!

        // Admin blocks user
        repository.blockUser(user.uid)
        assertTrue(repository.registeredUsers.value.find { it.uid == user.uid }?.isBlocked == true)

        // Attempt login while blocked -> must fail
        val blockedLogin = repository.loginWithEmail(email, pass)
        assertTrue(blockedLogin.isFailure)
        assertTrue(blockedLogin.exceptionOrNull()?.message?.contains("blocked", ignoreCase = true) == true)

        // Admin unblocks user
        repository.unblockUser(user.uid)
        val unblockedLogin = repository.loginWithEmail(email, pass)
        assertTrue(unblockedLogin.isSuccess)

        // Free upload toggle
        repository.toggleFreeUpload(user.uid)
        val updatedUser = repository.registeredUsers.value.find { it.uid == user.uid }
        assertNotNull(updatedUser)
    }

    @Test
    fun `admin special app passcode configuration and creation`() {
        val repository = StoreRepository()
        assertEquals("7996", repository.adminSpecialPasscode.value)

        // Super Admin updates passcode
        repository.setAdminSpecialPasscode("8844")
        assertEquals("8844", repository.adminSpecialPasscode.value)

        // Admin submits special app
        val specialApp = repository.submitApp(
            name = "Internal Diagnostics",
            packageName = "com.moonask.diagnostics",
            categoryId = "tools",
            shortDesc = "Special internal diagnostics app",
            fullDesc = "Only authorized admin staff",
            iconUrl = "",
            versionName = "1.0.0",
            versionCode = 1,
            sizeMb = 9.2f,
            permissions = listOf("INTERNET"),
            whatsNew = "Internal release",
            apkUrl = "",
            isAdminSpecial = true,
            specialAccessPasscode = "8844"
        )

        assertTrue(specialApp.isAdminSpecial)
        assertEquals("8844", specialApp.specialAccessPasscode)
    }
}
