package com.app

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dinolearnerfree.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class AppLaunchInstrumentedTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun packageName_isCorrect() {
        // Verifies we’re running the right package (fixes the old template mismatch)
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.app.dinolearnerfree", context.packageName)
    }

    @Test
    fun app_starts_without_crashing() {
        // If the Activity can open and move to RESUMED state, this passes.
        activityRule.scenario.onActivity { /* no-op */ }
    }
}
