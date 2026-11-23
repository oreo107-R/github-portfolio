package com.app.dinolearnerfree

import android.content.pm.ActivityInfo
import android.view.OrientationEventListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/**
 * Keeps the Activity in sensor mode across screens (no restore on dispose).
 * Allows 0° portrait, 90° landscape, 270° landscape, and 180° reverse-portrait.
 */
@Composable
fun AllowPortraitAndLandscapeUpright() {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity) {
        // Set once per composition; leave it when we navigate away to avoid flicker.
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            // Intentionally DO NOT restore the old orientation to prevent snap/flicker.
        }
    }
}

/**
 * Allows portrait and both landscapes, but forces the screen
 * back to upright portrait if the user rotates the device upside-down (180°).
 *
 * We don’t restore the old orientation on dispose, so navigation between
 * screens won’t cause flicker (portrait → landscape snap).
 */
@Composable
fun AllowPortraitAndLandscapeForceUpright() {
    val activity = LocalContext.current.findActivity() ?: return

    DisposableEffect(activity) {
        // Start in upright portrait
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val listener = object : OrientationEventListener(activity) {
            override fun onOrientationChanged(angle: Int) {
                if (angle == ORIENTATION_UNKNOWN) return

                val newMode = when {
                    // Allow both landscapes (≈90° and 270°)
                    angle in 60..120 || angle in 240..300 ->
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

                    // If upside-down (≈180°), force upright portrait
                    angle in 150..210 ->
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                    // Normal portrait (≈0° or 360°)
                    angle in 330..359 || angle in 0..30 ->
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                    else -> return // ignore in-between zones
                }

                if (activity.requestedOrientation != newMode) {
                    activity.requestedOrientation = newMode
                }
            }
        }

        listener.enable()
        onDispose {
            listener.disable()
            // Don’t restore orientation → avoids flicker on navigation
        }
    }
}

@Composable
fun AllowAllSensorNoFlip() {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        onDispose {
            // keep sensor; do not restore to avoid flicker
        }
    }
}
