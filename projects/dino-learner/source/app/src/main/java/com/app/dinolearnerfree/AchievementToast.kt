// AchievementToast.kt
package com.app.dinolearnerfree

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Listens to the active profile's objectives and shows a 3s "Achievement Unlocked" banner
 * the first time an objective flips to true.
 */
@SuppressLint("MutableCollectionMutableState")
@Composable
fun AchievementToastsHost() {
    val context = LocalContext.current
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val profile by ProfilesStore.profileFlow(context, selectedId).collectAsState(initial = null)

    var previousUnlocked by remember(selectedId) { mutableStateOf<Set<String>>(emptySet()) }
    var showing by remember { mutableStateOf<StickerDef?>(null) }
    val alreadyToasted by remember(selectedId) { mutableStateOf(mutableSetOf<String>()) }

    // NEW: Priming flag so we don't toast for historical unlocks on first mount
    var primed by remember(selectedId) { mutableStateOf(false) }

    LaunchedEffect(profile?.progress?.objectives) {
        val nowUnlocked = profile?.progress?.objectives?.filterValues { it }?.keys ?: emptySet()

        if (!primed) {
            // First time we see data for this profile: treat existing unlocks as "old"
            previousUnlocked = nowUnlocked
            primed = true
            return@LaunchedEffect
        }

        val newlyUnlocked = nowUnlocked - previousUnlocked - alreadyToasted
        val nextKey = newlyUnlocked.firstOrNull()
        if (nextKey != null) {
            stickerForObjective(nextKey)?.let { def ->
                showing = def
                alreadyToasted += nextKey
                kotlinx.coroutines.delay(3000)
                showing = null
            }
        }
        previousUnlocked = nowUnlocked
    }

    // UI banner
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showing != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            showing?.let { def ->
                Row(
                    modifier = Modifier
                        .background(Color(0xFF2A2A2A).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (val icon = def.icon) {
                        is StickerIcon.Emoji -> {
                            Text(icon.value, fontSize = 28.sp, modifier = Modifier.alpha(0.95f))
                            Spacer(Modifier.width(10.dp))
                        }
                        is StickerIcon.Res -> {
                            Image(
                                painter = painterResource(icon.id),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                    }
                    Column {
                        Text("Sticker Unlocked!", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Text(def.title, color = Color(0xFFCCE7FF), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
