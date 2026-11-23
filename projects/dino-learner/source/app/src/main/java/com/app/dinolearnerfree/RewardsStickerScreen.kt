package com.app.dinolearnerfree

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.composed
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing

/**
 * Stickers correspond to objective keys in ProfilesStore.progress.objectives
 *
 * Mark these from your level screens at the right moments, e.g.:
 *   ProfilesStore.markObjective(context, "<objective_key>")
 *
 * Already used:
 *  - "quiz_az_passed"  (Level 1 full pass)
 *  - "quiz_az_20plus"  (Level 2 score >= 20/26)
 *
 * Added below (examples):
 *  - "level3_cleared"        → after finishing Missing Letter
 *  - "level4_cleared"        → after finishing Upper/Lower Match
 *  - "level5_cleared"        → after finishing Build-a-Word
 *  - "level6_cleared"        → after finishing Find-the-Letter-in-Word
 *  - "level7_sorted_once"    → after solving first drag-to-order round (or use ..._cleared)
 *  - "level8_cleared"        → after finishing Speed Tap
 *  - "level9_cleared"        → after finishing Vowel/Consonant
 *  - "level10_cleared"       → already set in LevelTenScreen
 */

sealed interface StickerIcon {
    data class Emoji(val value: String) : StickerIcon
    data class Res(@DrawableRes val id: Int) : StickerIcon
}

data class StickerDef(
    val objectiveKey: String,
    val title: String,
    val hint: String,
    val icon: StickerIcon,
)

val StickerCatalog = listOf(
    // Level 1
    StickerDef(
        objectiveKey = "quiz_az_passed",
        title = "ABC Finisher",
        hint  = "Finish the full A–Z flashcards and review.",
        icon  = StickerIcon.Res(R.drawable.sticker_abc_finisher)
    ),
    // Level 2
    StickerDef(
        objectiveKey = "quiz_az_20plus",
        title = "ABC Ace",
        hint  = "Score 20/26 in the letter quiz (Level 2).",
        icon  = StickerIcon.Res(R.drawable.sticker_abc_ace)
    ),
    // Level 3
    // In StickerCatalog
    StickerDef(
        objectiveKey = "lvl3_first_round",
        title = "Missing-Letter Pro",
        hint  = "Score 8/10 in Missing Letter (Level 3).",  // <- updated hint
        icon  = StickerIcon.Res(R.drawable.sticker_lvl3_missing_letter_pro)
    ),

    StickerDef(
        objectiveKey = "level3_cleared",
        title = "Missing Letters Champ",
        hint  = "Finish the Missing Letter game (Level 3).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl3_missing_letter_champ) // all lowercase
    ),
    // Level 4
    StickerDef(
        objectiveKey = "level4_cleared",
        title = "Case Matcher",
        hint  = "Match upper & lower case letters (Level 4).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl4_case_matcher)
    ),
    // Level 5
    StickerDef(
        objectiveKey = "level5_cleared",
        title = "Word Builder",
        hint  = "Build the word from letter tiles (Level 5).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl5_word_builder)
    ),
    // Level 6
    StickerDef(
        objectiveKey = "level6_cleared",
        title = "Letter Hunter",
        hint  = "Find the target letter in a word (Level 6).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl6_letter_hunter)
    ),
    // Level 7
    StickerDef(
        objectiveKey = "level7_sorted_once",
        title = "Alphabet Sorter",
        hint  = "Drag letters into A→Z order (Level 7).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl7_alpha_sorter)
    ),
    // Level 8
    StickerDef(
        objectiveKey = "level8_cleared",
        title = "Quick Tapper",
        hint  = "Tap the letter before time runs out (Level 8).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl8_quick_tapper)
    ),
    // Level 9
    StickerDef(
        objectiveKey = "level9_cleared",
        title = "Vowel Master",
        hint  = "Tell vowels from consonants (Level 9).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl9_vowel_master)
    ),
    // Level 10
    StickerDef(
        objectiveKey = "level10_cleared",
        title = "Dino Learner Star",
        hint  = "Complete the final mixed challenge (Level 10).",
        icon  = StickerIcon.Res(R.drawable.sticker_lvl10_trophie) // ensure name matches your drawable
    ),
)

fun stickerForObjective(key: String): StickerDef? =
    StickerCatalog.firstOrNull { it.objectiveKey == key }

@Composable
fun RewardsStickerScreen(nav: NavController) {
    val context = LocalContext.current
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val profile by ProfilesStore.profileFlow(context, selectedId).collectAsState(initial = null)

    val objectives = profile?.progress?.objectives ?: emptyMap()

    val unlockedCount = remember(objectives) {
        StickerCatalog.count { objectives[it.objectiveKey] == true }
    }

    var infoSticker by remember { mutableStateOf<StickerDef?>(null) } // dialog state

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = this.maxWidth
        val maxH = this.maxHeight
        val isPortrait = maxH > maxW
        val shortSide = if (maxW < maxH) maxW else maxH
        val gap = if (isPortrait) 16.dp else 12.dp
        val minCell = shortSide * (if (isPortrait) 0.34f else 0.28f)

        // Background
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = gap, vertical = gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Stickers",
                fontSize = if (isPortrait) 28.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2A2A2A)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Unlocked: $unlockedCount / ${StickerCatalog.size}",
                fontSize = if (isPortrait) 18.sp else 16.sp,
                color = Color(0xFF3A3A3A),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(gap))

            // Grid of stickers
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minCell),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = gap),
                verticalArrangement = Arrangement.spacedBy(gap),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                items(StickerCatalog, key = { it.objectiveKey }) { s ->
                    val unlocked = objectives[s.objectiveKey] == true
                    StickerTile(
                        title = s.title,
                        icon = s.icon,
                        unlocked = unlocked,
                        onClick = { infoSticker = s }
                    )
                }
            }
        }

        // Small close/back button (top-right)
        Image(
            painter = painterResource(R.drawable.red_x_square),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(44.dp)
                .noRippleClickable { nav.popBackStack() },
            contentScale = ContentScale.Fit
        )
    }

    // Sticker info dialog
    val show = infoSticker
    if (show != null) {
        val unlocked = profile?.progress?.objectives?.get(show.objectiveKey) == true
        StickerInfoDialog(
            sticker = show,
            unlocked = unlocked,
            onDismiss = { infoSticker = null }
        )
    }
}

@Composable
private fun StickerTile(
    title: String,
    icon: StickerIcon,
    unlocked: Boolean,
    onClick: () -> Unit
) {
    val panelColor = if (unlocked) Color(0xFFFFFFFF) else Color(0xFFF4F4F4)
    val borderColor = if (unlocked) Color(0xFF7AC74F) else Color(0xFFB9B9B9)
    val alpha = if (unlocked) 1f else 0.55f

    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = if (unlocked) 6.dp else 1.dp,
        shadowElevation = if (unlocked) 6.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .noRippleClickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(panelColor)
                .border(3.dp, borderColor, RoundedCornerShape(18.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Big circular sticker area
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(
                        brush = if (unlocked) {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFF2C7),
                                    Color(0xFFFFD48A),
                                    Color(0xFFFFC062)
                                )
                            )
                        } else {
                            SolidColor(Color(0xFFEEEDED))
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (icon) {
                    is StickerIcon.Emoji -> {
                        Text(
                            text = icon.value,
                            fontSize = 40.sp,
                            modifier = Modifier.alpha(alpha)
                        )
                    }
                    is StickerIcon.Res -> {
                        Image(
                            painter = painterResource(icon.id),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize(0.8f) // inset so it fits the circle
                                .alpha(alpha),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                if (!unlocked) {
                    Image(
                        painter = painterResource(R.drawable.lock_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .offset(x = 2.dp, y = 2.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E2E2E),
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun StickerInfoDialog(
    sticker: StickerDef,
    unlocked: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = if (unlocked) "Sticker Unlocked!" else "Locked Sticker",
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(if (unlocked) Color(0xFFFFE6A6) else Color(0xFFEAEAEA)),
                    contentAlignment = Alignment.Center
                ) {
                    when (val icon = sticker.icon) {
                        is StickerIcon.Emoji -> Text(icon.value, fontSize = 56.sp)
                        is StickerIcon.Res -> Image(
                            painter = painterResource(icon.id),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.85f),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = sticker.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(6.dp))
                if (!unlocked) {
                    Text(
                        text = "How to unlock: ${sticker.hint}",
                        fontSize = 14.sp,
                        color = Color(0xFF444444)
                    )
                } else {
                    Text(
                        text = "Great job! You’ve earned this sticker.",
                        fontSize = 14.sp,
                        color = Color(0xFF3A3A3A)
                    )
                }
            }
        }
    )
}

/** --- Utilities --- */

/** Clickable with no ripple; safe to use from non-composable call sites. */
private fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}


