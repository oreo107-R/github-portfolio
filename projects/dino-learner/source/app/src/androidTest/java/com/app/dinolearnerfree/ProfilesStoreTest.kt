package com.app.dinolearnerfree

import androidx.compose.ui.res.painterResource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilesStoreInstrumentedTest {

    @Test
    fun profilesFlow_emits_without_crashing() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Basic smoke test: we can read current profiles list
        val profiles = ProfilesStore.profilesFlow(context).first()
        assertNotNull(profiles)
    }

    @Test
    fun createProfile_thenAppearsInFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "TestPlayer_" + System.currentTimeMillis()

        // If your API uses a different call, adjust the line below:
        ProfilesStore.createProfile(context, name)

        val profiles = ProfilesStore.profilesFlow(context).first()
        assertTrue(profiles.any { it.name == name })
    }
}



