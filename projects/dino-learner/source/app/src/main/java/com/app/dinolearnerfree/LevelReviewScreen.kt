// LevelReviewScreen.kt
package com.app.dinolearnerfree

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LevelReviewScreen(
    nav: NavController,
    levelNumber: Int,                  // e.g., 3
    stickerKey: String,                // e.g., "level3_cleared"
    completedLevelId: Int,             // e.g., 3
    @DrawableRes headerImageRes: Int,  // e.g., R.drawable.level_3_360
    message: String,                   // e.g., "Great job! Missing Letter complete."
    replayRoute: String,               // e.g., "level3"

    // Optional routes + button art
    levelSelectRoute: String = "levelSelect",
    nextLevelRoute: String? = null,    // if null -> "level${levelNumber+1}"
    @DrawableRes nextButtonRes: Int = R.drawable.next_level_button,
    @DrawableRes levelSelectButtonRes: Int = R.drawable.level_select_button,
    @DrawableRes replayButtonRes: Int = R.drawable.replay_button_150
) {
    val context = LocalContext.current
    val activity = context.findActivity() // from Ads.kt

    // --- Always unlock the next level once on entry (independent of stickers)
    var didUnlock by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!didUnlock) {
            unlockNextLevel(context, completedLevelId = completedLevelId)
            didUnlock = true
        }
    }

    // --- Mark the sticker once if not already marked
    var markedSticker by rememberSaveable { mutableStateOf(false) }
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val profile  by ProfilesStore.profileFlow(context, selectedId).collectAsState(initial = null)
    val alreadyMarked = profile?.progress?.objectives?.get(stickerKey) == true
    LaunchedEffect(alreadyMarked) {
        if (!alreadyMarked && !markedSticker) {
            ProfilesStore.markObjective(context, stickerKey)
            markedSticker = true
        }
    }

    // --- Ad checkpoint: preload and try to show once when this screen opens
    var adTried by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        Ads.preload(context) // safe no-op if already loading/loaded
        if (!adTried) {
            activity?.let { Ads.showThen(activity = it, after = {}) }
            adTried = true
        }
    }

    // Next-level routing
    val MAX_LEVEL = 10
    val hasNextLevel = levelNumber < MAX_LEVEL
    val computedNextRoute = remember(levelNumber, nextLevelRoute) {
        nextLevelRoute ?: "level${(levelNumber + 1).coerceAtMost(MAX_LEVEL)}"
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize() // edge-to-edge background
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide  = if (maxWidth < maxHeight) maxWidth else maxHeight

        val gap         = if (isPortrait) 24.dp else 16.dp
        val titleWidth  = shortSide * (if (isPortrait) 0.95f else 0.80f)
        val buttonSize  = shortSide * 0.25f
        val messageSize = if (isPortrait) 35.sp else 20.sp

        // Background (fills whole screen; prevents white strip at bottom)
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        if (isPortrait) {
            // -------- PORTRAIT --------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(gap),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(headerImageRes),
                    contentDescription = "Level $levelNumber",
                    modifier = Modifier
                        .width(titleWidth)
                        .graphicsLayer {
                            scaleX = 1.15f
                            scaleY = 1.15f
                        },
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(gap * 1.2f))

                Text(
                    text = message,
                    fontSize = messageSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E2E2E),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(gap))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasNextLevel) {
                        Image(
                            painter = painterResource(nextButtonRes),
                            contentDescription = "Next Level",
                            modifier = Modifier
                                .size(buttonSize)
                                .clickable {
                                    val act = activity
                                    if (act != null)
                                        Ads.showThen(activity = act) { nav.navigate(computedNextRoute) }
                                    else
                                        nav.navigate(computedNextRoute)
                                },
                            contentScale = ContentScale.Fit
                        )
                    }

                    Image(
                        painter = painterResource(levelSelectButtonRes),
                        contentDescription = "Level Select",
                        modifier = Modifier
                            .size(buttonSize)
                            .clickable {
                                val act = activity
                                if (act != null)
                                    Ads.showThen(activity = act) { nav.navigate(levelSelectRoute) }
                                else
                                    nav.navigate(levelSelectRoute)
                            },
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(replayButtonRes),
                        contentDescription = "Replay",
                        modifier = Modifier
                            .size(buttonSize)
                            .clickable {
                                val act = activity
                                if (act != null)
                                    Ads.showThen(activity = act) { nav.navigate(replayRoute) }
                                else
                                    nav.navigate(replayRoute)
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else {
            // -------- LANDSCAPE --------
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: header + message
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(headerImageRes),
                        contentDescription = "Level $levelNumber",
                        modifier = Modifier.width(titleWidth),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(gap))
                    Text(
                        text = message,
                        fontSize = messageSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2E2E2E),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.width(gap))

                // Right: vertical buttons
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    if (hasNextLevel) {
                        Image(
                            painter = painterResource(nextButtonRes),
                            contentDescription = "Next Level",
                            modifier = Modifier
                                .size(buttonSize)
                                .clickable {
                                    val act = activity
                                    if (act != null)
                                        Ads.showThen(activity = act) { nav.navigate(computedNextRoute) }
                                    else
                                        nav.navigate(computedNextRoute)
                                },
                            contentScale = ContentScale.Fit
                        )
                    }

                    Image(
                        painter = painterResource(levelSelectButtonRes),
                        contentDescription = "Level Select",
                        modifier = Modifier
                            .size(buttonSize)
                            .clickable {
                                val act = activity
                                if (act != null)
                                    Ads.showThen(activity = act) { nav.navigate(levelSelectRoute) }
                                else
                                    nav.navigate(levelSelectRoute)
                            },
                        contentScale = ContentScale.Fit
                    )

                    Image(
                        painter = painterResource(replayButtonRes),
                        contentDescription = "Replay",
                        modifier = Modifier
                            .size(buttonSize)
                            .clickable {
                                val act = activity
                                if (act != null)
                                    Ads.showThen(activity = act) { nav.navigate(replayRoute) }
                                else
                                    nav.navigate(replayRoute)
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Pause overlay
        PauseMenuOverlay(nav)
    }
}




