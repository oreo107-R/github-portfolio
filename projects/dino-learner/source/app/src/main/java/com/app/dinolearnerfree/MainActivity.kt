package com.app.dinolearnerfree

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.min
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState





// ---------------- Progress helpers ----------------

suspend fun unlockNextLevel(context: Context, completedLevelId: Int) {
    val profileId = ProfilesStore.selectedIdFlow(context).first()
    val profile = ProfilesStore.profileFlow(context, profileId).first()
    val current = profile?.progress?.unlockedLevel ?: 1
    val next = (completedLevelId + 1).coerceAtMost(10)
    if (next > current) {
        ProfilesStore.setUnlockedLevel(context, next)
    }
}

// -------------- Levels registry --------------
data class LevelSpec(
    val id: Int,
    val titleRes: Int,       // drawable for the button image (or string if you prefer)
    val route: String,
    val requiresObjective: String? = null // optional: gate on an objective (e.g., "quiz_az_passed")
)

// Use your existing button art for L1–L3; reuse or add for others as you create them.
val Levels: List<LevelSpec> = listOf(
    LevelSpec(1, R.drawable.level_1_150, "level1"),
    LevelSpec(2, R.drawable.level_2_150, "level2"),
    LevelSpec(3, R.drawable.level_3_150, "level3"), // MissingLetter (already built)

    // === Stubs you can fill in as you build ===
    LevelSpec(4, R.drawable.level_4_150, "level4"),
    LevelSpec(5, R.drawable.level_5_150, "level5"),
    LevelSpec(6, R.drawable.level_6_150, "level6"),
    LevelSpec(7, R.drawable.level_7_150, "level7"),
    LevelSpec(8, R.drawable.level_8_150, "level8"),
    LevelSpec(9, R.drawable.level_9_150, "level9"),
    LevelSpec(10, R.drawable.level_10_150, "level10")
)








// -------------------------------------------
// Audio helpers
// -------------------------------------------
fun playSoundForFlash(audioResId: Int, context: Context) {
    val player = MediaPlayer.create(context, audioResId)
    player.setOnCompletionListener { it.release() }
    player.start()
}

// Flashcard model
data class DinoFlashcard(val letter: String, val frontImage: Int, val backImage: Int)

// Flashcard sets
val flashcardsAE = listOf(
    DinoFlashcard("A", R.drawable.letter_a_transparent, R.drawable.a_is_for_apple),
    DinoFlashcard("B", R.drawable.letter_b_transparent, R.drawable.b_is_for_banana),
    DinoFlashcard("C", R.drawable.letter_c_transparent, R.drawable.c_is_for_cat),
    DinoFlashcard("D", R.drawable.letter_d_transparent, R.drawable.d_is_for_dog),
    DinoFlashcard("E", R.drawable.letter_e_transparent, R.drawable.e_is_for_egg)
)

val flashcardsFJ = listOf(
    DinoFlashcard("F", R.drawable.letter_f_transparent, R.drawable.f_is_for_fish),
    DinoFlashcard("G", R.drawable.letter_g_transparent, R.drawable.g_is_for_grape),
    DinoFlashcard("H", R.drawable.letter_h_transparent, R.drawable.h_is_for_hat),
    DinoFlashcard("I", R.drawable.letter_i_transparent, R.drawable.i_is_for_ice_cream),
    DinoFlashcard("J", R.drawable.letter_j_transparent, R.drawable.j_is_for_jelly)
)

val flashcardsKO = listOf(
    DinoFlashcard("K", R.drawable.letter_k_transparent, R.drawable.k_is_for_kiwi),
    DinoFlashcard("L", R.drawable.letter_l_transparent, R.drawable.l_is_for_lemon),
    DinoFlashcard("M", R.drawable.letter_m_transparent, R.drawable.m_is_for_monkey),
    DinoFlashcard("N", R.drawable.letter_n_transparent, R.drawable.n_is_for_nest),
    DinoFlashcard("O", R.drawable.letter_o_transparent, R.drawable.o_is_for_owl)
)
val flashcardsPT = listOf(
    DinoFlashcard("P", R.drawable.letter_p_transparent, R.drawable.p_is_for_peach),
    DinoFlashcard("Q", R.drawable.letter_q_transparent, R.drawable.q_is_for_queen),
    DinoFlashcard("R", R.drawable.letter_r_transparent, R.drawable.r_is_for_robin),
    DinoFlashcard("S", R.drawable.letter_s_transparent, R.drawable.s_is_for_sailboat),
    DinoFlashcard("T", R.drawable.letter_t_transparent, R.drawable.t_is_for_turtle)
)
val flashcardsUZ = listOf(
    DinoFlashcard("U", R.drawable.letter_u_transparent, R.drawable.u_is_for_umbrella),
    DinoFlashcard("V", R.drawable.letter_v_transparent, R.drawable.v_is_for_violet),
    DinoFlashcard("W", R.drawable.letter_w_transparent, R.drawable.w_is_for_window),
    DinoFlashcard("X", R.drawable.letter_x_transparent, R.drawable.x_is_for_x_ray),
    DinoFlashcard("Y", R.drawable.letter_y_transparent, R.drawable.y_is_for_yo_yo),
    DinoFlashcard("Z", R.drawable.letter_z_transparent, R.drawable.z_is_for_zebra),
)

// Letter -> audio resource
private val letterAudio: Map<String, Int> = mapOf(
    "A" to R.raw.a,
    "B" to R.raw.b,
    "C" to R.raw.c,
    "D" to R.raw.d,
    "E" to R.raw.e,
    "F" to R.raw.f,
    "G" to R.raw.g,
    "H" to R.raw.h,
    "I" to R.raw.i_,
    "J" to R.raw.j,
    "K" to R.raw.k,
    "L" to R.raw.l,
    "M" to R.raw.m,
    "N" to R.raw.n,
    "O" to R.raw.o,
    "P" to R.raw.p,
    "Q" to R.raw.q,
    "R" to R.raw.r,
    "S" to R.raw.s,
    "T" to R.raw.t,
    "U" to R.raw.u,
    "V" to R.raw.v,
    "W" to R.raw.w,
    "X" to R.raw.x,
    "Y" to R.raw.y,
    "Z" to R.raw.z
)

// Map each BACK drawable to its audio clip in res/raw
private val backAudioByImage: Map<Int, Int> = mapOf(
    R.drawable.a_is_for_apple to R.raw.a_is_for_apple,
    R.drawable.b_is_for_banana to R.raw.b_is_for_banana,
    R.drawable.c_is_for_cat to R.raw.c_is_for_cat,
    R.drawable.d_is_for_dog to R.raw.d_is_for_dog,
    R.drawable.e_is_for_egg to R.raw.e_is_for_egg,

    R.drawable.f_is_for_fish to R.raw.f_is_for_fish,
    R.drawable.g_is_for_grape to R.raw.g_is_for_grape,
    R.drawable.h_is_for_hat to R.raw.h_is_for_hat,
    R.drawable.i_is_for_ice_cream to R.raw.i_is_for_ice_cream,
    R.drawable.j_is_for_jelly to R.raw.j_is_for_jelly,

    R.drawable.k_is_for_kiwi to R.raw.k_is_for_kiwi,
    R.drawable.l_is_for_lemon to R.raw.l_is_for_lemon,
    R.drawable.m_is_for_monkey to R.raw.m_is_for_monkey,
    R.drawable.n_is_for_nest to R.raw.n_is_for_nest,
    R.drawable.o_is_for_owl to R.raw.o_is_for_owl,

    R.drawable.p_is_for_peach to R.raw.p_is_for_peach,
    R.drawable.q_is_for_queen to R.raw.q_is_for_queen,
    R.drawable.r_is_for_robin to R.raw.r_is_for_robin,
    R.drawable.s_is_for_sailboat to R.raw.s_is_for_sailboat,
    R.drawable.t_is_for_turtle to R.raw.t_is_for_turtle,

    R.drawable.u_is_for_umbrella to R.raw.u_is_for_umbrella,
    R.drawable.v_is_for_violet to R.raw.v_is_for_violet,
    R.drawable.w_is_for_window to R.raw.w_is_for_window,
    R.drawable.x_is_for_x_ray to R.raw.x_is_for_x_ray,
    R.drawable.y_is_for_yo_yo to R.raw.y_is_for_yo_yo,
    R.drawable.z_is_for_zebra to R.raw.z_is_for_zebra
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Mobile Ads SDK
        MobileAds.initialize(this)
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()
        )

        // Preload the first interstitial
        Ads.preload(this)

        setContent {
            val navController = rememberNavController()

            Box(Modifier.fillMaxSize()) {

                // ---------- Navigation ----------
                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") { SplashScreen(navController) }



                    composable("main") {
                        MainMenu { navController.navigate("profilePicker") }
                    }

                    composable("profilePicker") { ProfilePickerScreen(navController) }

                    composable("levelSelect") { LevelSelectScreen(navController) }

                    // -------- Level 1 & learning flow (A–Z) --------
                    composable("level1") { LevelOneScreen(navController) }
                    composable("intro") { LearningIntroScreen(navController) }

                    composable("learnABCDE") { DinoFlashcardViewer(navController, flashcardsAE) }
                    composable("flashcardReview") {
                        FlashcardReviewScreen(
                            navController = navController,
                            onReplay = { navController.navigate("learnABCDE") },
                            onNextSet = { navController.navigate("learnFJ") }
                        )
                    }

                    composable("learnFJ") { DinoFlashcardViewer(navController, flashcardsFJ) }
                    composable("flashcardReviewFJ") {
                        FlashcardReviewFJScreen(
                            navController = navController,
                            onReplay = { navController.navigate("learnFJ") },
                            onNextSet = { navController.navigate("learnKO") }
                        )
                    }

                    composable("learnKO") { DinoFlashcardViewer(navController, flashcardsKO) }
                    composable("flashcardReviewKO") {
                        FlashcardReviewKOScreen(
                            navController = navController,
                            onReplay = { navController.navigate("learnKO") },
                            onNextSet = { navController.navigate("learnPT") }
                        )
                    }

                    composable("learnPT") { DinoFlashcardViewer(navController, flashcardsPT) }
                    composable("flashcardReviewPT") {
                        FlashcardReviewPTScreen(
                            navController = navController,
                            onReplay = { navController.navigate("learnPT") },
                            onNextSet = { navController.navigate("learnUZ") }
                        )
                    }

                    composable("learnUZ") { DinoFlashcardViewer(navController, flashcardsUZ) }
                    composable("flashcardReviewUZ") {
                        FlashcardReviewUZScreen(
                            navController = navController,
                            onReplay = { navController.navigate("learnUZ") },
                            onNextSet = { navController.navigate("level2") }
                        )
                    }

                    // -------- Level 2 (quiz) --------
                    composable("level2") { LevelTwoScreen(navController) }
                    composable("intro2") { LearningIntroTwoScreen(navController) }
                    composable("questionsA-Z") { QuestionsAZ(navController) }
                    // In your NavHost
                    composable(
                        route = "abcQuizReview/{score}",
                        arguments = listOf(navArgument("score") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val scoreArg = backStackEntry.arguments?.getInt("score") ?: 0
                        ABCQuizReviewScreen(
                            navController = navController,
                            onReplay = { /* your replay action */ },
                            score = scoreArg
                        )
                    }


                    // -------- Level 3 (Missing Letter) + Review --------
                    // -------- Level 3 (Missing Letter) + Review --------
                    composable("level3")       { LevelThreeScreen(navController) }
                    composable("level3Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 3,
                            stickerKey = "level3_cleared",
                            completedLevelId = 3,
                            headerImageRes = R.drawable.level_3_360,
                            message = "Great job! Missing Letter complete.",
                            replayRoute = "level3",
                            levelSelectRoute = "levelSelect",   // <— was nextRoute
                            nextLevelRoute = "level4"           // optional; auto-computes to level4 if omitted
                        )
                    }


                    // -------- Rewards --------
                    composable("rewards") { RewardsStickerScreen(navController) }

                    // -------- Levels 4–10 + Reviews (generic) --------
                    composable("level4") { LevelFourScreen(navController) }
                    composable("level4Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 4,
                            stickerKey = "level4_cleared",
                            completedLevelId = 4,
                            headerImageRes = R.drawable.level_4_360,
                            message = "Awesome! Case Match complete.",
                            replayRoute = "level4"
                        )
                    }

                    composable("level5") { LevelFiveScreen(navController) }
                    composable("level5Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 5,
                            stickerKey = "level5_cleared",
                            completedLevelId = 5,
                            headerImageRes = R.drawable.level_5_360,
                            message = "You built the word!",
                            replayRoute = "level5"
                        )
                    }

                    composable("level6") { LevelSixScreen(navController) }
                    composable("level6Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 6,
                            stickerKey = "level6_cleared",
                            completedLevelId = 6,
                            headerImageRes = R.drawable.level_6_360,
                            message = "Nice! Found all the letters.",
                            replayRoute = "level6"
                        )
                    }

                    composable("level7") { LevelSevenScreen(navController) }
                    composable("level7Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 7,
                            stickerKey = "level7_sorted_once", // or use a new "level7_cleared" if you add it
                            completedLevelId = 7,
                            headerImageRes = R.drawable.level_7_360,
                            message = "Alphabet sorted—great job!",
                            replayRoute = "level7"
                        )
                    }

                    composable("level8") { LevelEightScreen(navController) }
                    composable("level8Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 8,
                            stickerKey = "level8_cleared",
                            completedLevelId = 8,
                            headerImageRes = R.drawable.level_8_360,
                            message = "Speed Tap complete!",
                            replayRoute = "level8"
                        )
                    }

                    composable("level9") { LevelNineScreen(navController) }
                    composable("level9Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 9,
                            stickerKey = "level9_cleared",
                            completedLevelId = 9,
                            headerImageRes = R.drawable.level_9_360,
                            message = "Vowel vs Consonant mastered!",
                            replayRoute = "level9"
                        )
                    }

                    composable("level10") { LevelTenScreen(navController) }
                    composable("level10Review") {
                        LevelReviewScreen(
                            nav = navController,
                            levelNumber = 10,
                            stickerKey = "level10_cleared",
                            completedLevelId = 10,
                            headerImageRes = R.drawable.level_10_360,
                            message = "Final challenge complete—star student!",
                            replayRoute = "level10"
                        )
                    }
                }
                // ---------- One-time parental notice (overlay, not on splash) ----------
                val ctx = LocalContext.current
                var showNotice by rememberSaveable { mutableStateOf(FirstRunPrefs.shouldShowNotice(ctx)) }
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                if (showNotice && currentRoute != "splash") {
                    FirstLaunchNoticeDialog(
                        onContinue = {
                            FirstRunPrefs.markNoticeShown(ctx)
                            showNotice = false
                        },
                        privacyPolicyUrl = "https://www.levelupwithoneup.com/home/dino-learner/privacy-policy" // replace
                    )
                }
                // ---------- Pop-up achievement banner (overlay) ----------
                AchievementToastsHost()
            }
        }
    }
}

@Composable
fun SplashScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("main") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.dinolearnertitle),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            LoadingBar()
        }
    }
}

@Composable
fun LoadingBar() {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(50)
            progress += 0.02f
        }
    }
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier
            .width(200.dp)
            .height(8.dp),
        color = Color(0xFF7AC74F),
        trackColor = Color.LightGray
    )
}

@Composable
fun MainMenu(onStartClick: () -> Unit) {
    // Set once so all following screens inherit (no flicker)
    AllowAllSensorNoFlip()

    val context = LocalContext.current
    var titlePlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var bgPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var alive by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        alive = true
        titlePlayer = MediaPlayer.create(context, R.raw.dino_learner_title).apply {
            setOnCompletionListener { mp ->
                mp.release()
                titlePlayer = null
                if (alive) {
                    bgPlayer = MediaPlayer.create(context, R.raw.dino_fun_11).apply {
                        isLooping = true; start()
                    }
                }
            }
            start()
        }
        onDispose {
            alive = false
            try { titlePlayer?.stop() } catch (_: Throwable) {}
            titlePlayer?.release(); titlePlayer = null
            try { bgPlayer?.stop() } catch (_: Throwable) {}
            bgPlayer?.release(); bgPlayer = null
        }
    }

    fun stopAll() {
        try { titlePlayer?.stop() } catch (_: Throwable) {}
        titlePlayer?.release(); titlePlayer = null
        try { bgPlayer?.stop() } catch (_: Throwable) {}
        bgPlayer?.release(); bgPlayer = null
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        val titleWidth = shortSide * if (isPortrait) 0.80f else 0.80f
        val dinoSize   = shortSide * if (isPortrait) 0.55f else 0.40f
        val startSize  = shortSide * if (isPortrait) 0.28f else 0.28f
        val topPad     = if (isPortrait) 16.dp else 8.dp
        val sidePad    = if (isPortrait) 24.dp else 32.dp

        // Background
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = sidePad, vertical = topPad),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.dinolearnertitle),
                    contentDescription = "DinoLearner Title",
                    modifier = Modifier.width(titleWidth),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.weight(1f))

                Image(
                    painter = painterResource(R.drawable.trexpurple),
                    contentDescription = "Purple Dino",
                    modifier = Modifier.size(dinoSize),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.weight(1f))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Image(
                        painter = painterResource(R.drawable.dlstartbutton_140x70),
                        contentDescription = "Start",
                        modifier = Modifier
                            .size(startSize)
                            .noRippleClickable {
                                stopAll()
                                onStartClick()
                            },
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = sidePad, vertical = topPad)
            ) {
                // Row for spacing + Dino on right
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left spacer (still here to balance layout if needed)
                    Spacer(Modifier.weight(1f))

                    // Middle spacer (empty, Start will be overlaid instead)
                    Spacer(Modifier.weight(1f))

                    // Right: Dino
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Image(
                            painter = painterResource(R.drawable.trexpurple),
                            contentDescription = "Purple Dino",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 24.dp)
                                .size(dinoSize),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Overlaid Start button – true bottom center of screen
                Image(
                    painter = painterResource(R.drawable.dlstartbutton_140x70),
                    contentDescription = "Start",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)   // absolute bottom-center of screen
                        .padding(bottom = 24.dp)
                        .size(startSize)
                        .noRippleClickable {
                            stopAll()
                            onStartClick()
                        },
                    contentScale = ContentScale.Fit
                )

                // Overlaid Title – shifted right & up
                Image(
                    painter = painterResource(R.drawable.dinolearnertitle),
                    contentDescription = "DinoLearner Title",
                    modifier = Modifier
                        .align(Alignment.CenterStart)   // base position
                        .offset(x = 80.dp, y = (-60).dp) // ⬅️ move right + up (tweak values)
                        .width(shortSide * 0.95f)
                        .zIndex(2f),
                    contentScale = ContentScale.Fit
                )
            }
        }


    }
}





@Composable
fun ProfileOverlay(
    nav: NavController,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val profiles by ProfilesStore.profilesFlow(context).collectAsState(initial = emptyList())
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val currentProfile = profiles.firstOrNull { it.id == selectedId }

    var showConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {

            Image(
                painter = painterResource(R.drawable.menu_background_340x242),
                contentDescription = null,
                modifier = Modifier.size(340.dp, 242.dp),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .size(340.dp, 242.dp)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top   // ⬅️ was SpaceBetween
            ) {
                Spacer(Modifier.height(10.dp))         // ⬅️ push header down a bit
                Text(
                    text = currentProfile?.name ?: "Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2A2A2A)
                )
                Spacer(Modifier.height(12.dp))

                Image(
                    painter = painterResource(R.drawable.stickers_button), // <-- your sticker/trophy PNG
                    contentDescription = "Stickers",
                    modifier = Modifier
                        .width(120.dp)
                        .height(70.dp)
                        .noRippleClickable {
                            onClose()                  // close the dialog first
                            nav.navigate("rewards")    // then go to the sticker wall
                        },
                    contentScale = ContentScale.Fit
                )


                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DELETE
                    Image(
                        painter = painterResource(R.drawable.purple_delete_profile_button),
                        contentDescription = "Delete Profile",
                        modifier = Modifier
                            .width(120.dp)      // same width/height for both so they sit tight
                            .height(70.dp)
                            .noRippleClickable { showConfirm = true },
                        contentScale = ContentScale.Fit
                    )

                    // LOGOUT
                    LogoutImageButton(
                        nav = nav,
                        imageRes = R.drawable.logout_button,
                        modifier = Modifier
                            .width(120.dp)
                            .height(70.dp)
                    )
                }
            }
        }

    }

    if (showConfirm) {
        ConfirmDeleteDialog(
            profileName = currentProfile?.name ?: "this profile",
            onCancel = { showConfirm = false },
            onConfirm = {
                showConfirm = false
                scope.launch {
                    ProfilesStore.deleteCurrentProfile(context)
                    val remaining = ProfilesStore.profilesFlow(context).first()
                    if (remaining.isEmpty()) {
                        nav.navigate("profilePicker") {
                            popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                    onClose()
                }
            }
        )
    }
}



@Composable
private fun ConfirmDeleteDialog(
    profileName: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.menu_background_340x242),
                contentDescription = null,
                modifier = Modifier.size(300.dp, 180.dp),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .size(300.dp, 180.dp)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(12.dp)) // ⬅️ push the text down
                Text("Delete $profileName?", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("This can’t be undone.", fontSize = 14.sp)
                Spacer(Modifier.weight(1f)) // push buttons to bottom
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.purple_cancel_button), // use your gray "Cancel" art if you have it
                        contentDescription = "Cancel",
                        modifier = Modifier
                            .size(94.dp)
                            .noRippleClickable { onCancel() },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(R.drawable.purple_confirm_button),
                        contentDescription = "Confirm",
                        modifier = Modifier
                            .size(110.dp)
                            .noRippleClickable { onConfirm() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun LogoutImageButton(
    nav: NavController,
    imageRes: Int,                     // just Int is fine (drawable id)
    modifier: Modifier = Modifier,
    size: Dp = 110.dp
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Image(
        painter = painterResource(imageRes),
        contentDescription = "Logout",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .noRippleClickable {
                scope.launch {
                    // clear the selected profile
                    ProfilesStore.setSelected(context, null)

                    // navigate back to ProfilePickerScreen
                    nav.navigate("profilePicker") {
                        popUpTo(nav.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            }
    )
}



@Composable
fun PauseMenuOverlay(navController: NavController) {
    var menuOpen by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }

    val panelW = 340.dp
    val panelH = 242.dp

    // Pause button (top-right)
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.ic_pause_menu_copy),
            contentDescription = "Menu",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(50.dp)
                .noRippleClickable { menuOpen = true },
            contentScale = ContentScale.Fit
        )
    }

    if (menuOpen) {
        Dialog(
            onDismissRequest = { menuOpen = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            val isPortrait =
                LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

            val extraSize = 40.dp
            val gap = 8.dp
            val gutter = extraSize + gap + 8.dp   // icon size + spacing + buffer

            val containerW = if (isPortrait) panelW else panelW + gutter
            val containerH = if (isPortrait) panelH + gutter else panelH

            Box(
                modifier = Modifier
                    .size(containerW, containerH)
                    .padding(0.dp),
                contentAlignment = Alignment.Center
            ) {
                // Panel centered
                Image(
                    painter = painterResource(R.drawable.menu_background_340x242),
                    contentDescription = null,
                    modifier = Modifier.size(panelW, panelH),
                    contentScale = ContentScale.FillBounds
                )

                // Buttons ON the panel
                Column(
                    modifier = Modifier
                        .size(panelW, panelH)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.menu_resume_110x60),
                        contentDescription = "Resume",
                        modifier = Modifier
                            .size(110.dp)
                            .noRippleClickable { menuOpen = false },
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(2.dp))
                    Image(
                        painter = painterResource(R.drawable.menu_quit_110x60),
                        contentDescription = "Quit to main",
                        modifier = Modifier
                            .size(110.dp)
                            .noRippleClickable {
                                menuOpen = false
                                navController.navigate("main") {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                    restoreState = false
                                }
                            },
                        contentScale = ContentScale.Fit
                    )
                }

                // === Icons "outside" the panel but inside the larger container ===
                if (isPortrait) {
                    // PORTRAIT: horizontally above the panel
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //Later version of the app
                        /*Image(
                            painter = painterResource(R.drawable.setting_cog),
                            contentDescription = "Settings",
                            modifier = Modifier
                                .size(extraSize)
                                .noRippleClickable {
                                    menuOpen = false
                                    moreOpen = true
                                },
                            contentScale = ContentScale.Fit
                        )*/
                        Image(
                            painter = painterResource(R.drawable.profile_icon),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(extraSize)
                                .noRippleClickable {
                                    menuOpen = false
                                    moreOpen = true
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    // LANDSCAPE: vertically to the right of the panel
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        //THis for a later version of the app
                       /* Image(
                            painter = painterResource(R.drawable.setting_cog),
                            contentDescription = "Settings",
                            modifier = Modifier
                                .size(extraSize)
                                .noRippleClickable {
                                    menuOpen = false
                                    moreOpen = true
                                },
                            contentScale = ContentScale.Fit
                        )*/
                        Image(
                            painter = painterResource(R.drawable.profile_icon),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(extraSize)
                                .noRippleClickable {
                                    menuOpen = false
                                    moreOpen = true
                                },
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }

    // SECOND OVERLAY
    if (moreOpen) {
        ProfileOverlay(
            nav = navController,
            onClose = { moreOpen = false }
        )
    }

}


@Composable
fun LevelSelectScreen(navController: NavController) {
    val context = LocalContext.current
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val profile by ProfilesStore.profileFlow(context, selectedId).collectAsState(initial = null)
    val unlocked = profile?.progress?.unlockedLevel ?: 1
    PlayRawOnceSafe(R.raw.choose_a_level)

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Cache scope properties first
        val maxW = this.maxWidth
        val maxH = this.maxHeight

        val isPortrait = maxH > maxW
        val shortSide  = if (maxW < maxH) maxW else maxH
        val spacing    = if (isPortrait) 20.dp else 16.dp
        val buttonSize = shortSide * if (isPortrait) 0.30f else 0.28f
        val columns    = if (isPortrait) 2 else 3
        val gridState  = rememberLazyGridState()   // <-- add this

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
                .padding(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title sizing
            val aspect = 2f
            val maxWTitle = shortSide * if (isPortrait) 0.94f else 0.72f
            val maxHTitle = (maxH - spacing * 2).coerceAtLeast(0.dp)
            val hFromW = maxWTitle / aspect
            val (titleW, titleH) =
                if (hFromW <= maxHTitle || maxHTitle == 0.dp) maxWTitle to hFromW
                else (maxHTitle * aspect) to maxHTitle

            Image(
                painter = painterResource(R.drawable.choose_a_level),
                contentDescription = "Choose A Level",
                modifier = Modifier.size(titleW, titleH),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(spacing))

            // Scrollable grid of levels
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxSize()
            ) {
                items(Levels, key = { it.id }) { spec ->
                    Box(Modifier.size(buttonSize), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(spec.titleRes),
                            contentDescription = "Level ${spec.id}",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Fit
                        )

                        val objectiveMet = spec.requiresObjective?.let { req ->
                            profile?.progress?.objectives?.get(req) == true
                        } ?: true
                        val isUnlocked = (unlocked >= spec.id) && objectiveMet

                        if (!isUnlocked) {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .alpha(0.55f)
                                    .background(Color.Black, RoundedCornerShape(16.dp))
                            )
                            Image(
                                painter = painterResource(R.drawable.lock_icon),
                                contentDescription = "Locked",
                                modifier = Modifier.size(buttonSize * 0.45f),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .noRippleClickable { navController.navigate(spec.route) }
                            )
                        }
                    }
                }
            }
        }

        PauseMenuOverlay(navController)
    }
}




@Composable
fun LevelOneScreen(navController: NavController) {
    AllowPortraitAndLandscapeUpright()

    PlayRawOnceSafe(R.raw.level_1)
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("intro")
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.level_1_360),
            contentDescription = "Level 1",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun LevelTwoScreen(navController: NavController) {
    PlayRawOnceSafe(R.raw.level_2)
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("intro2")
    }
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.level_2_360),
            contentDescription = "Level 2",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun LevelThreeScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    // Play the "Level 3" VO and delay briefly
    PlayRawOnceSafe(R.raw.level_3)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (showGame) {
        // Your actual game
        MissingLetterScreen(navController)
    } else {
        // Splash art
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_3_360),
                contentDescription = "Level 3",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    }
}

@Composable
fun MissingLetterScreen(navController: NavController) {
    // Safely capture context for callbacks
    val context by rememberUpdatedState(LocalContext.current)
    val scope = rememberCoroutineScope()

    // ----- quiz state -----
    val total = 10
    var qIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var q by remember { mutableStateOf(buildMissingLetterQuestion()) }
    var accepting by remember { mutableStateOf(true) }

    // 🔒 Fire the 8/10 unlock/sticker exactly once
    var proAwarded by rememberSaveable { mutableStateOf(false) }

    fun nextQuestion() {
        if (qIndex + 1 >= total) {
            // Finished — go to the review screen
            navController.navigate("level3Review") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
                restoreState = false
            }
        } else {
            qIndex++
            q = buildMissingLetterQuestion()
            accepting = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        // Tunables by orientation
        val gap = if (isPortrait) 16.dp else 14.dp
        val pad = 16.dp
        val scoreTextSize = if (isPortrait) 26.sp else 24.sp
        val chipTextSize = if (isPortrait) 28.sp else 24.sp
        val gridCols = if (isPortrait) 2 else 4
        val optionRowGap = if (isPortrait) gap * 2f else gap * 1.75f

        // Background
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Compute a tile size that ALWAYS fits given the row column count & padding
        val innerW = maxWidth - pad * 2
        val optionSizeByWidth = (innerW - gap * (gridCols - 1)) / gridCols
        val optionSoftCap = shortSide * (if (isPortrait) 0.28f else 0.22f)
        val optionSize = min(optionSizeByWidth, optionSoftCap)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = pad, vertical = pad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score
            Text(
                text = "Score: $score / $total",
                fontSize = scoreTextSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(if (isPortrait) gap * 6 else gap * 3f))

            // Prompt row: A _ C
            Row(verticalAlignment = Alignment.CenterVertically) {
                @Composable
                fun Chip(txt: String) {
                    Box(
                        Modifier
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .background(Color(0xFFFFF0DC), RoundedCornerShape(12.dp))
                            .border(2.dp, Color(0xFFE91E63), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            txt,
                            fontSize = chipTextSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2A2A2A)
                        )
                    }
                }

                Chip(if (q.missingPos == 0) "_" else q.left)
                Chip(if (q.missingPos == 1) "_" else q.mid)
                Chip(if (q.missingPos == 2) "_" else q.right)
            }

            Spacer(Modifier.height(if (isPortrait) gap * 6 else gap * 3f))

            // Options: 2x2 (portrait) or 1x4 (landscape)
            Column(
                verticalArrangement = Arrangement.spacedBy(optionRowGap),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f, fill = true)
                    .fillMaxWidth()
            ) {
                q.options.chunked(gridCols).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { letter ->
                            OptionTile(
                                text = letter,
                                size = optionSize,
                                enabled = accepting,
                                onClick = {
                                    if (!accepting) return@OptionTile
                                    accepting = false
                                    val correct = letter == q.answer
                                    if (correct) {
                                        val newScore = score + 1
                                        score = newScore

                                        // 🔓 Unlock L4 + award "Missing-Letter Pro" (lvl3_first_round) at 8/10
                                        if (!proAwarded && newScore >= 8) {
                                            proAwarded = true
                                            scope.launch {
                                                ProfilesStore.markObjective(
                                                    context,
                                                    "lvl3_first_round" // <-- your Pro sticker objective
                                                )
                                                unlockNextLevel(context, completedLevelId = 3)
                                            }
                                        }

                                        playSoundForFlash(R.raw.small_dino_raspy, context)
                                    } else {
                                        playSoundForFlash(R.raw.buzzer, context)
                                    }
                                    nextQuestion()
                                }
                            )
                        }
                    }
                }
            }
        }

        PauseMenuOverlay(navController)
    }
}




/* ---------- helpers ---------- */

@Composable
private fun OptionTile(
    text: String,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp))
            .border(4.dp, Color(0xFF4F3F7A), RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.noRippleClickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class MissingQ(
    val left: String,
    val mid: String,
    val right: String,
    val missingPos: Int,      // 0 = left, 1 = mid, 2 = right
    val answer: String,
    val options: List<String>
)

private fun buildMissingLetterQuestion(): MissingQ {
    val alphabet = ('A'..'Z').map { it.toString() }
    val start = (0..23).random() // use start, start+1, start+2
    val a = alphabet[start]
    val b = alphabet[start + 1]
    val c = alphabet[start + 2]

    val missingPos = (0..2).random()
    val answer = when (missingPos) { 0 -> a; 1 -> b; else -> c }

    val distractors = alphabet.filter { it != answer }.shuffled().take(3)
    val options = (distractors + answer).shuffled()

    return MissingQ(
        left = a,
        mid = b,
        right = c,
        missingPos = missingPos,
        answer = answer,
        options = options
    )
}


@Composable
fun LevelFourScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    // VO + quick splash
    PlayRawOnceSafe(R.raw.level_4)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_4_360),
                contentDescription = "Level 4",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        // The real game. When it finishes, unlock + go back.
        LevelFourGame(
            onCompleted = {
                // Let the review screen do the mark + unlock
                navController.navigate("level4Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            },
            navController = navController
        )

    }
}

/* ---------- Level 4 helpers ---------- */
@Composable
private fun LevelFourGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // --- Game state ---
    val totalPairs = 6
    val cards = remember {
        val letters = ('A'..'Z').shuffled().take(totalPairs)
        mutableStateListOf<UCItem>().apply {
            addAll(letters.flatMap { listOf(UCItem(it, true), UCItem(it, false)) }.shuffled())
        }
    }
    var first by rememberSaveable { mutableIntStateOf(-1) }
    var second by rememberSaveable { mutableIntStateOf(-1) }
    var foundPairs by rememberSaveable { mutableIntStateOf(0) }
    var accepting by remember { mutableStateOf(true) }

    fun onCardClick(i: Int) {
        if (!accepting) return
        if (cards[i].matched) return
        if (first == i) return

        if (first == -1) first = i else second = i
    }

    // When two picks exist, evaluate
    LaunchedEffect(first, second) {
        if (first != -1 && second != -1) {
            accepting = false
            delay(600)

            val a = cards[first]
            val b = cards[second]
            val match = a.letter == b.letter && a.isUpper != b.isUpper
            if (match) {
                cards[first] = a.copy(matched = true)
                cards[second] = b.copy(matched = true)
                foundPairs++
                playSoundForFlash(R.raw.small_dino_raspy, context)
                if (foundPairs >= totalPairs) {
                    delay(500)
                    onCompleted() // <-- hand control back to wrapper
                }
            } else {
                playSoundForFlash(R.raw.buzzer, context)
            }
            first = -1; second = -1; accepting = true
        }
    }

    // --- UI ---
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val gap = if (isPortrait) 16.dp else 12.dp
        val columns = if (isPortrait) 3 else 4
        val tileSize = shortSide * (if (isPortrait) 0.24f else 0.20f)

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
                .padding(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Matches: $foundPairs / $totalPairs",
                fontSize = if (isPortrait) 26.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(gap * 1.5f))

            cards.chunked(columns).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { card ->
                        val idx = cards.indexOf(card)
                        val revealed = card.matched || idx == first || idx == second
                        LetterCard(
                            letter = if (card.isUpper)
                                card.letter.toString()
                            else
                                card.letter.lowercaseChar().toString(),
                            isUpper = card.isUpper,
                            revealed = revealed,
                            size = tileSize,
                            enabled = accepting && !card.matched && idx != first,
                            onClick = { onCardClick(idx) }
                        )
                    }
                }
                Spacer(Modifier.height(gap))
            }
        }

        PauseMenuOverlay(navController)
    }
}

/* helpers */
private data class UCItem(val letter: Char, val isUpper: Boolean, val matched: Boolean = false)

@Composable
private fun LetterCard(
    letter: String,
    isUpper: Boolean,
    revealed: Boolean,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (revealed) Color(0xFF7C6EB0) else Color(0xFFB8AEE0)
    val border = if (revealed) Color(0xFF4F3F7A) else Color(0xFF8E83C2)
    Box(
        modifier = Modifier
            .size(size)
            .background(bg, RoundedCornerShape(16.dp))
            .border(4.dp, border, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.noRippleClickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (revealed) letter else "?",
            color = Color.White,
            fontSize = if (revealed) 42.sp else 34.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// ---------------- Level 5: Initial-sound phonics ----------------

private data class PhonicsItem(val letter: Char, val imageRes: Int)
private data class PhonicsRound(val letter: Char, val options: List<Int>, val correctRes: Int)

// Map a picture for each starting letter (using your existing drawables)
private val PHONICS_DATA = listOf(
    PhonicsItem('A', R.drawable.a_is_for_apple_250),
    PhonicsItem('B', R.drawable.b_is_for_banana_250),
    PhonicsItem('C', R.drawable.c_is_for_cat_250),
    PhonicsItem('D', R.drawable.d_is_for_dog_250),
    PhonicsItem('E', R.drawable.e_is_for_egg_250),
    PhonicsItem('F', R.drawable.f_is_for_fish_250),
    PhonicsItem('G', R.drawable.g_is_for_grape_250),
    PhonicsItem('H', R.drawable.h_is_for_hat_250),
    PhonicsItem('I', R.drawable.i_is_for_ice_cream_250),
    PhonicsItem('J', R.drawable.j_is_for_jelly_250),
    PhonicsItem('K', R.drawable.k_is_for_kiwi_250),
    PhonicsItem('L', R.drawable.l_is_for_lemon_250),
    PhonicsItem('M', R.drawable.m_is_for_monkey_250),
    PhonicsItem('N', R.drawable.n_is_for_nest_250),
    PhonicsItem('O', R.drawable.o_is_for_owl_250),
    PhonicsItem('P', R.drawable.p_is_for_peach_250),
    PhonicsItem('Q', R.drawable.q_is_for_queen_250),
    PhonicsItem('R', R.drawable.r_is_for_robin_250),
    PhonicsItem('S', R.drawable.s_is_for_sailboat_250),
    PhonicsItem('T', R.drawable.t_is_for_turtle_250),
    PhonicsItem('U', R.drawable.u_is_for_umbrella_250),
    PhonicsItem('V', R.drawable.v_is_for_violet_250),
    PhonicsItem('W', R.drawable.w_is_for_window_250),
    PhonicsItem('X', R.drawable.x_is_for_x_ray_250),
    PhonicsItem('Y', R.drawable.y_is_for_yo_yo_250),
    PhonicsItem('Z', R.drawable.z_is_for_zebra_250)
)

private fun buildPhonicsRound(used: Set<Char>): PhonicsRound {
    val pool = PHONICS_DATA.filter { it.letter !in used }.ifEmpty { PHONICS_DATA }
    val target = pool.random()
    val distractors = PHONICS_DATA.filter { it.letter != target.letter }.shuffled().take(3)
    val options = (listOf(target) + distractors).shuffled()
    return PhonicsRound(
        letter = target.letter,
        options = options.map { it.imageRes },
        correctRes = target.imageRes
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LevelFiveGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // --- game state ---
    val rounds = 8
    var index by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val usedLetters = remember { mutableStateListOf<Char>() }
    var round by remember { mutableStateOf(buildPhonicsRound(usedLetters.toSet())) }
    var selected by remember { mutableStateOf<Int?>(null) }
    var showNext by remember { mutableStateOf(false) }

    LaunchedEffect(round.letter) {
        letterAudio[round.letter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun next() {
        selected = null
        showNext = false
        usedLetters += round.letter
        if (index + 1 >= rounds) onCompleted()
        else {
            index++
            round = buildPhonicsRound(usedLetters.toSet())
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val boxMaxW = maxWidth
        val boxMaxH = maxHeight
        val isPortrait = boxMaxH > boxMaxW
        val shortSide  = if (boxMaxW < boxMaxH) boxMaxW else boxMaxH

        // Layout knobs
        val spacing       = if (isPortrait) 16.dp else 12.dp
        val rowGap        = if (isPortrait) spacing * 2 else spacing
        val scoreTextSize = if (isPortrait) 26.sp else 22.sp
        val gridCols      = if (isPortrait) 2 else 4          // 2×2 vs 1×4
        val nextButtonSz  = shortSide * (if (isPortrait) 0.28f else 0.20f)

        // BG
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
                .padding(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score: $score / $rounds",
                fontSize = scoreTextSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(spacing))

            Box(
                Modifier
                    .background(Color(0xFFFFF0DC), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = round.letter.toString(),
                    fontSize = if (isPortrait) 34.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2A2A2A)
                )
            }

            Spacer(Modifier.height(spacing * 2))

            // ---- Bigger tiles: give tiles more width and raise the soft cap ----
            val tilesSidePad = if (isPortrait) spacing else spacing / 2     // smaller side pad just for tiles
            val tilesHGap    = if (isPortrait) spacing else spacing * 0.6f  // tighter gap in landscape

            val innerW    = boxMaxW - tilesSidePad * 2
            val tileWidth = (innerW - tilesHGap * (gridCols - 1)) / gridCols
            val softCap   = shortSide * (if (isPortrait) 0.35f else 0.33f)  // ↑ bigger in both modes
            val optionSz  = tileWidth.coerceAtMost(softCap)

            val corner = 14.dp

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
                    .padding(horizontal = tilesSidePad),
                horizontalArrangement = Arrangement.spacedBy(tilesHGap, Alignment.CenterHorizontally),
                verticalArrangement   = Arrangement.spacedBy(rowGap),
                maxItemsInEachRow     = gridCols
            ) {
                round.options.forEach { imgRes ->
                    val borderColor = when {
                        selected == null            -> Color.Transparent
                        imgRes == round.correctRes  -> Color(0xFF00FF08)
                        imgRes == selected          -> Color(0xFFFF0000)
                        else                        -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .size(optionSz)
                            .clip(RoundedCornerShape(corner))
                            .background(Color.White)
                            .border(3.dp, borderColor, RoundedCornerShape(corner))
                            .noRippleClickable(enabled = selected == null) {
                                selected = imgRes
                                val correct = imgRes == round.correctRes
                                if (correct) {
                                    score++
                                    playSoundForFlash(R.raw.small_dino_raspy, context)
                                    backAudioByImage[imgRes]?.let { playSoundForFlash(it, context) }
                                } else {
                                    playSoundForFlash(R.raw.buzzer, context)
                                }
                                showNext = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(imgRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            if (showNext) {
                Spacer(Modifier.height(spacing))
                Image(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(nextButtonSz)
                        .noRippleClickable { next() },
                    contentScale = ContentScale.Fit
                )
            }
        }

        PauseMenuOverlay(navController)
    }
}






@Composable
fun LevelFiveScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_5)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_5_360),
                contentDescription = "Level 5",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelFiveGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level5Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}

// ---------------- Level 6: Find the letter inside a word ----------------

private data class FindRound(val letter: Char, val word: String)

private val FIND_WORDS: Map<Char, String> = mapOf(
    'A' to "CAT",  'B' to "BUS",   'C' to "CORN", 'D' to "DOG",  'E' to "WEB",
    'F' to "FAN",  'G' to "GOAT",  'H' to "HAT",  'I' to "LION", 'J' to "JAM",
    'K' to "KITE", 'L' to "LAMP",  'M' to "MOON", 'N' to "NEST", 'O' to "FROG",
    'P' to "PEN",  'Q' to "QUIT", 'R' to "BIRD", 'S' to "SUN",  'T' to "TOY",
    'U' to "MUG",  'V' to "VAN",   'W' to "WON",'X' to "BOX",  'Y' to "YARN",
    'Z' to "ZOOM"
)

private fun buildFindRound(used: Set<Char>): FindRound {
    val pool = ('A'..'Z').filter { it !in used }
    val letter = (pool.ifEmpty { ('A'..'Z').toList() }).random()
    val word = FIND_WORDS[letter] ?: "CAT"
    return FindRound(letter, word)
}

@Composable
fun LevelSixGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // --- game state ---
    val rounds = 8
    var index by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val used = remember { mutableStateListOf<Char>() }
    var round by remember { mutableStateOf(buildFindRound(used.toSet())) }
    var pickedIndex by remember { mutableStateOf<Int?>(null) }
    var showNext by remember { mutableStateOf(false) }

    // Play the target letter each round
    LaunchedEffect(round.letter) {
        letterAudio[round.letter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun replayAudio() {
        letterAudio[round.letter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun nextRound() {
        pickedIndex = null
        showNext = false
        used += round.letter
        if (index + 1 >= rounds) onCompleted()
        else {
            index++
            round = buildFindRound(used.toSet())
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val boxW = maxWidth
        val boxH = maxHeight
        val isPortrait = boxH > boxW
        val shortSide  = if (boxW < boxH) boxW else boxH

        val gap       = if (isPortrait) 16.dp else 14.dp
        val scoreSize = if (isPortrait) 26.sp else 22.sp
        val nextSize  = shortSide * (if (isPortrait) 0.28f else 0.22f)
        val replaySz  = shortSide * (if (isPortrait) 0.22f else 0.18f)

        // Background full-bleed
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        if (isPortrait) {
            // ---------- PORTRAIT ----------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(gap),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Score
                Text(
                    text = "Score: $score / $rounds",
                    fontSize = scoreSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(gap))

                // Replay audio image (no letter shown)
                Image(
                    painter = painterResource(R.drawable.btn_replay_audio),
                    contentDescription = "Replay Audio",
                    modifier = Modifier
                        .size(replaySz)
                        .noRippleClickable { replayAudio() },
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(gap * 2))

                // Word as centered row of tiles
                WordRow(
                    word = round.word,
                    outerWidth = boxW,
                    gap = gap,
                    shortSide = shortSide,
                    onPick = { idx, correct ->
                        if (pickedIndex != null) return@WordRow
                        pickedIndex = idx
                        if (correct) {
                            score++
                            playSoundForFlash(R.raw.small_dino_raspy, context)
                        } else {
                            playSoundForFlash(R.raw.buzzer, context)
                        }
                        showNext = true
                    },
                    pickedIndex = pickedIndex,
                    correctIndex = round.word.indexOf(round.letter)
                )

                if (showNext) {
                    Spacer(Modifier.height(gap * 2))
                    Image(
                        painter = painterResource(R.drawable.next_button),
                        contentDescription = "Next",
                        modifier = Modifier
                            .size(nextSize)
                            .noRippleClickable { nextRound() },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else {
            // ---------- LANDSCAPE (two-pane) ----------
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: score + replay button
                Column(
                    modifier = Modifier
                        .weight(0.40f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Score: $score / $rounds",
                        fontSize = scoreSize,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(gap))

                    Image(
                        painter = painterResource(R.drawable.btn_replay_audio),
                        contentDescription = "Replay Audio",
                        modifier = Modifier
                            .size(replaySz)
                            .noRippleClickable { replayAudio() },
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.width(gap))

                // Right: word row + next (sizes from pane width)
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(0.60f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val paneW = maxWidth
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        WordRow(
                            word = round.word,
                            outerWidth = paneW,
                            gap = gap,
                            shortSide = shortSide,
                            onPick = { idx, correct ->
                                if (pickedIndex != null) return@WordRow
                                pickedIndex = idx
                                if (correct) {
                                    score++
                                    playSoundForFlash(R.raw.small_dino_raspy, context)
                                } else {
                                    playSoundForFlash(R.raw.buzzer, context)
                                }
                                showNext = true
                            },
                            pickedIndex = pickedIndex,
                            correctIndex = round.word.indexOf(round.letter)
                        )

                        if (showNext) {
                            Spacer(Modifier.height(gap * 2))
                            Image(
                                painter = painterResource(R.drawable.next_button),
                                contentDescription = "Next",
                                modifier = Modifier
                                    .size(nextSize)
                                    .noRippleClickable { nextRound() },
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }

        PauseMenuOverlay(navController)
    }
}


@Composable
private fun WordRow(
    word: String,
    outerWidth: Dp,
    gap: Dp,
    shortSide: Dp,
    onPick: (index: Int, correct: Boolean) -> Unit,
    pickedIndex: Int?,
    correctIndex: Int
) {
    val tiles = word.toList()
    // Chunky base size + fit by available width, with padding on both sides
    val baseSize = shortSide * 0.18f
    val byWidth  = (outerWidth - gap * (tiles.size - 1) - gap * 2) / tiles.size.toFloat()
    val tileSize = minOf(baseSize, byWidth)

    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = gap)
        ) {
            tiles.forEachIndexed { i, ch ->
                val isPicked = pickedIndex == i
                val isCorrect = i == correctIndex
                val borderColor =
                    if (pickedIndex == null) Color.Transparent
                    else if (isCorrect) Color(0xFF00FF08)
                    else if (isPicked) Color(0xFFFF0000)
                    else Color.Transparent

                WordLetterTile(
                    letter = ch.toString(),
                    size = tileSize,
                    borderColor = borderColor,
                    enabled = pickedIndex == null
                ) {
                    onPick(i, isCorrect)
                }
            }
        }
    }
}

@Composable
private fun WordLetterTile(
    letter: String,
    size: Dp,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp)) // tile background
            .border(4.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.noRippleClickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}



@Composable
fun LevelSixScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_6)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_6_360),
                contentDescription = "Level 6",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelSixGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level6Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}




// ------------------------ Level 7: Alphabet ordering (drag to sort) ------------------------

@Composable
fun LevelSevenScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_7)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_7_360),
                contentDescription = "Level 7",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelSevenGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level7Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}

@Composable
fun LevelSevenGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Round state ---
    val totalRounds = 3
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    var round by remember { mutableStateOf(buildOrderRound()) }

    // Shuffled working list for the current round
    val current = remember(round) {
        mutableStateListOf<Char>().apply {
            var shuffled: List<Char>
            do { shuffled = round.target.shuffled() } while (shuffled == round.target)
            addAll(shuffled)
        }
    }

    // Drag state
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) } // px offset (X in landscape, Y in portrait)

    // playful cue: say the first letter of the run (e.g., "M")
    LaunchedEffect(round.startLetter) {
        letterAudio[round.startLetter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun advanceOrFinish() {
        scope.launch {
            delay(600)
            if (roundIndex + 1 >= totalRounds) onCompleted()
            else {
                roundIndex++
                round = buildOrderRound()
            }
        }
    }

    fun checkSolved() {
        if (current.toList() == round.target) {
            score++
            playSoundForFlash(R.raw.small_dino_raspy, context)
            advanceOrFinish()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = this.maxWidth
        val maxH = this.maxHeight
        val isPortrait = maxH > maxW
        val shortSide = if (maxW < maxH) maxW else maxH
        val gap = if (isPortrait) 16.dp else 12.dp
        val scoreSize = if (isPortrait) 26.sp else 22.sp

        val tileSize = shortSide * (if (isPortrait) 0.16f else 0.14f)
        val density = LocalDensity.current
        val tilePx = with(density) { tileSize.toPx() }
        val gapPx  = with(density) { gap.toPx() }
        val stepPx = tilePx + gapPx // distance between two slots (vertical in portrait, horizontal in landscape)

        // BG
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
                .padding(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${roundIndex + 1} / $totalRounds   Score: $score",
                fontSize = scoreSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(gap * 3))

            // Instruction chip
            Box(
                Modifier
                    .background(Color(0xFFFFF0DC), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    "Drag to order A → Z",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2A2A2A)
                )
            }

            // EXTRA space between the instruction and the letters
            val instructionGap = if (isPortrait) gap * 6 else gap * 5
            Spacer(Modifier.height(instructionGap))

            if (isPortrait) {
                // ------------------ PORTRAIT: vertical column of tiles ------------------
                Column(
                    verticalArrangement = Arrangement.spacedBy(gap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = gap)
                ) {
                    current.forEachIndexed { index, ch ->
                        val isDragging = index == draggingIndex
                        val offsetY = if (isDragging) dragOffset else 0f

                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .offset { IntOffset(0, offsetY.roundToInt()) }
                                .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp))
                                .border(4.dp, Color(0xFF4F3F7A), RoundedCornerShape(16.dp))
                                .pointerInput(index, current.hashCode()) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffset = 0f
                                        },
                                        onDrag = onDrag@ { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.y // vertical drag

                                            val from = draggingIndex ?: return@onDrag
                                            val moved = (dragOffset / stepPx).toInt()
                                            val to = (from + moved).coerceIn(0, current.lastIndex)

                                            if (to != from) {
                                                current.move(from, to)
                                                draggingIndex = to
                                                dragOffset -= moved * stepPx // keep offset relative to new slot
                                            }
                                        },
                                        onDragEnd = {
                                            draggingIndex = null
                                            dragOffset = 0f
                                            checkSolved()
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragOffset = 0f
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            } else {
                // ------------------ LANDSCAPE: single horizontal row of tiles ------------------
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = gap)
                ) {
                    current.forEachIndexed { index, ch ->
                        val isDragging = index == draggingIndex
                        val offsetX = if (isDragging) dragOffset else 0f

                        Box(
                            modifier = Modifier
                                .size(tileSize)
                                .offset { IntOffset(offsetX.roundToInt(), 0) }
                                .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp))
                                .border(4.dp, Color(0xFF4F3F7A), RoundedCornerShape(16.dp))
                                .pointerInput(index, current.hashCode()) {
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingIndex = index
                                            dragOffset = 0f
                                        },
                                        onDrag = onDrag@ { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount.x // horizontal drag

                                            val from = draggingIndex ?: return@onDrag
                                            val moved = (dragOffset / stepPx).toInt()
                                            val to = (from + moved).coerceIn(0, current.lastIndex)

                                            if (to != from) {
                                                current.move(from, to)
                                                draggingIndex = to
                                                dragOffset -= moved * stepPx
                                            }
                                        },
                                        onDragEnd = {
                                            draggingIndex = null
                                            dragOffset = 0f
                                            checkSolved()
                                        },
                                        onDragCancel = {
                                            draggingIndex = null
                                            dragOffset = 0f
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ch.toString(),
                                color = Color.White,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        PauseMenuOverlay(navController)
    }
}


/* ---------- Helpers for Level 7 ---------- */

private data class OrderRound(
    val target: List<Char>,   // e.g., listOf('M','N','O','P','Q')
    val startLetter: Char
)

private fun buildOrderRound(): OrderRound {
    val length = 5
    val startIdx = (0..(26 - length)).random()
    val letters = (0 until length).map { ('A'.code + startIdx + it).toChar() }
    return OrderRound(target = letters, startLetter = letters.first())
}

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val item = removeAt(from)
    add(to, item)
}



// ------------------------ Level 8: Speed Tap (audio says letter → tap in time) ------------------------

@Composable
fun LevelEightScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_8)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_8_360),
                contentDescription = "Level 8",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelEightGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level8Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}

/* ------------ GAME ------------ */

private data class SpeedRound(val target: Char, val options: List<Char>)

private fun buildSpeedRound(used: Set<Char>, optionCount: Int = 6): SpeedRound {
    val letters = ('A'..'Z').toList()
    val pool = letters.filter { it !in used }.ifEmpty { letters }
    val target = pool.random()
    val distractors = letters.filter { it != target }.shuffled().take(optionCount - 1)
    return SpeedRound(target = target, options = (distractors + target).shuffled())
}

@Composable
fun LevelEightGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // ---- game state ----
    val totalRounds = 8
    val roundTimeMs = 3500
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val usedLetters = remember { mutableStateListOf<Char>() }
    var round by remember { mutableStateOf(buildSpeedRound(usedLetters.toSet())) }

    var selected by remember { mutableStateOf<Char?>(null) }
    var showNext by remember { mutableStateOf(false) }

    // timer
    var remainingMs by remember { mutableIntStateOf(roundTimeMs) }
    var timerRunning by remember { mutableStateOf(true) }

    // play the target letter, reset timer each round
    LaunchedEffect(round.target) {
        selected = null
        showNext = false
        remainingMs = roundTimeMs
        timerRunning = true
        letterAudio[round.target.toString()]?.let { playSoundForFlash(it, context) }

        // countdown
        while (timerRunning && remainingMs > 0 && selected == null) {
            delay(50)
            remainingMs -= 50
        }
        if (remainingMs <= 0 && selected == null) {
            // time out
            playSoundForFlash(R.raw.buzzer, context)
            showNext = true
            timerRunning = false
        }
    }

    fun nextRound() {
        usedLetters += round.target
        if (roundIndex + 1 >= totalRounds) {
            onCompleted()
        } else {
            roundIndex++
            round = buildSpeedRound(usedLetters.toSet())
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // cache scope props so they are visible inside children
        val maxW = this.maxWidth
        val maxH = this.maxHeight

        val isPortrait = maxH > maxW
        val shortSide = if (maxW < maxH) maxW else maxH
        val gap = if (isPortrait) 16.dp else 12.dp
        val scoreSize = if (isPortrait) 26.sp else 22.sp

        val columns = if (isPortrait) 3 else 6
        val baseTile = shortSide * (if (isPortrait) 0.22f else 0.16f)
        val byWidth = (maxW - gap * (columns - 1) - gap * 2) / columns.toFloat()
        val tileSize = minOf(baseTile, byWidth)

        val progress = (remainingMs / roundTimeMs.toFloat()).coerceIn(0f, 1f)

        // background
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
                .padding(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // score + round
            Text(
                text = "Round ${roundIndex + 1} / $totalRounds   Score: $score",
                fontSize = scoreSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(gap / 2))

            // timer bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = Color(0xFF7AC74F),
                trackColor = Color(0x33000000)
            )

            Spacer(Modifier.height(gap))

            // target chip
            Box(
                Modifier
                    .background(Color(0xFFFFF0DC), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF6B5B95), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Find: ${round.target}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2A2A2A)
                )
            }

            Spacer(Modifier.height(gap * 2))

            // options grid: 2x3 in portrait, 1x6 in landscape
            round.options.chunked(if (isPortrait) 3 else 6).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = gap)
                ) {
                    row.forEach { letter ->
                        val isCorrect = letter == round.target
                        val borderColor = when {
                            selected == null -> Color.Transparent
                            isCorrect -> Color(0xFF00FF08)
                            selected == letter -> Color(0xFFFF0000)
                            else -> Color.Transparent
                        }
                        SpeedLetterTile(
                            letter = letter.toString(),
                            size = tileSize,
                            borderColor = borderColor,
                            enabled = selected == null && remainingMs > 0,
                            onClick = {
                                if (selected != null) return@SpeedLetterTile
                                timerRunning = false
                                selected = letter
                                if (isCorrect) {
                                    score++
                                    playSoundForFlash(R.raw.small_dino_raspy, context)
                                } else {
                                    playSoundForFlash(R.raw.buzzer, context)
                                }
                                showNext = true
                            }
                        )
                    }
                }
                Spacer(Modifier.height(gap))
            }

            if (showNext) {
                Spacer(Modifier.height(gap))
                Image(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(shortSide * 0.28f)
                        .noRippleClickable { nextRound() },
                    contentScale = ContentScale.Fit
                )
            }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
private fun SpeedLetterTile(
    letter: String,
    size: Dp,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp))
            .border(4.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (enabled) Modifier.noRippleClickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


// ------------------------ Level 9: Vowels vs Consonants ------------------------

@Composable
fun LevelNineScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_9)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_9_360),
                contentDescription = "Level 9",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelNineGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level9Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}

/* ------------ GAME ------------ */

private val VOWELS: Set<Char> = setOf('A','E','I','O','U')

private data class VCStep(val letter: Char)

private fun nextVCStep(used: Set<Char>): VCStep {
    val letters = ('A'..'Z').toList()
    val pool = letters.filter { it !in used }.ifEmpty { letters }
    return VCStep(pool.random())
}

@Composable
fun LevelNineGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // --- state ---
    val totalRounds = 10
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val used = remember { mutableStateListOf<Char>() }
    var step by remember { mutableStateOf(nextVCStep(used.toSet())) }

    var picked: Boolean? by remember { mutableStateOf(null) } // true=vowel, false=consonant, null=not picked
    var showNext by remember { mutableStateOf(false) }

    // Say the letter at the start of each round
    LaunchedEffect(step.letter) {
        picked = null
        showNext = false
        letterAudio[step.letter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun choose(isVowelPick: Boolean) {
        if (picked != null) return
        val isVowel = step.letter in VOWELS
        picked = isVowelPick
        val correct = isVowelPick == isVowel
        if (correct) {
            score++
            playSoundForFlash(R.raw.small_dino_raspy, context)
        } else {
            playSoundForFlash(R.raw.buzzer, context)
        }
        showNext = true
    }

    fun nextRound() {
        used += step.letter
        if (roundIndex + 1 >= totalRounds) onCompleted()
        else {
            roundIndex++
            step = nextVCStep(used.toSet())
        }
    }

    // --- UI ---
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = this.maxWidth
        val maxH = this.maxHeight
        val isPortrait = maxH > maxW
        val shortSide = if (maxW < maxH) maxW else maxH
        val gap = if (isPortrait) 16.dp else 12.dp
        val scoreSize = if (isPortrait) 26.sp else 22.sp

        val letterTile = shortSide * (if (isPortrait) 0.28f else 0.22f)
        val binSize = shortSide * (if (isPortrait) 0.34f else 0.28f)

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
                .padding(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Round ${roundIndex + 1} / $totalRounds   Score: $score",
                fontSize = scoreSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(gap))

            // Big target letter tile
            Box(
                modifier = Modifier
                    .size(letterTile)
                    .background(Color(0xFF7C6EB0), RoundedCornerShape(20.dp))
                    .border(6.dp, Color(0xFF4F3F7A), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.letter.toString(),
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(Modifier.height(gap * 2))

            // Two big bins
            val vowelBorder =
                if (picked == null) Color.Transparent
                else if ((step.letter in VOWELS)) Color(0xFF00FF08) else Color.Transparent
            val consBorder =
                if (picked == null) Color.Transparent
                else if ((step.letter !in VOWELS)) Color(0xFF00FF08) else Color.Transparent
            val wrongBorder = Color(0xFFFF0000)

            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vowel bin
                VCBox(
                    title = "Vowel",
                    size = binSize,
                    borderColor = when (picked) {
                        null -> vowelBorder
                        true -> if (step.letter in VOWELS) Color(0xFF00FF08) else wrongBorder
                        false -> vowelBorder
                    },
                    enabled = picked == null,
                    onClick = { choose(true) }
                )
                // Consonant bin
                VCBox(
                    title = "Consonant",
                    size = binSize,
                    borderColor = when (picked) {
                        null -> consBorder
                        false -> if (step.letter !in VOWELS) Color(0xFF00FF08) else wrongBorder
                        true -> consBorder
                    },
                    enabled = picked == null,
                    onClick = { choose(false) }
                )
            }

            if (showNext) {
                Spacer(Modifier.height(gap))
                Image(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(shortSide * 0.28f)
                        .noRippleClickable { nextRound() },
                    contentScale = ContentScale.Fit
                )
            }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
private fun VCBox(
    title: String,
    size: Dp,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Color(0xFFFFF0DC), RoundedCornerShape(20.dp))
            .border(6.dp, if (borderColor == Color.Transparent) Color(0xFF6B5B95) else borderColor, RoundedCornerShape(20.dp))
            .then(if (enabled) Modifier.noRippleClickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color(0xFF2A2A2A),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


// ------------------------ Level 10: Final Mixed Challenge ------------------------

@Composable
fun LevelTenScreen(navController: NavController) {
    var showGame by remember { mutableStateOf(false) }

    PlayRawOnceSafe(R.raw.level_10)
    LaunchedEffect(Unit) { delay(1500); showGame = true }

    if (!showGame) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.dlmainbackground),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Image(
                painter = painterResource(R.drawable.level_10_360),
                contentDescription = "Level 10",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            PauseMenuOverlay(navController)
        }
    } else {
        LevelTenGame(
            navController = navController,
            onCompleted = {
                navController.navigate("level10Review") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        )

    }
}

/* ---------------- GAME ---------------- */

private sealed interface TenRound {
    val letter: Char
}
private data class TapLetterRound(
    override val letter: Char,
    val options: List<Char>
) : TenRound
private data class PhonicsRound10(
    override val letter: Char,
    val options: List<Int>,   // drawable ids
    val correctRes: Int
) : TenRound
private data class FindInWordRound10(
    override val letter: Char,
    val word: String
) : TenRound

// Local copies (prefixed to avoid name clashes with earlier levels)
private data class TenPhonicsItem(val letter: Char, val imageRes: Int)
private val TEN_PHONICS_DATA = listOf(
    TenPhonicsItem('A', R.drawable.a_is_for_apple),
    TenPhonicsItem('B', R.drawable.b_is_for_banana),
    TenPhonicsItem('C', R.drawable.c_is_for_cat),
    TenPhonicsItem('D', R.drawable.d_is_for_dog),
    TenPhonicsItem('E', R.drawable.e_is_for_egg),
    TenPhonicsItem('F', R.drawable.f_is_for_fish),
    TenPhonicsItem('G', R.drawable.g_is_for_grape),
    TenPhonicsItem('H', R.drawable.h_is_for_hat),
    TenPhonicsItem('I', R.drawable.i_is_for_ice_cream),
    TenPhonicsItem('J', R.drawable.j_is_for_jelly),
    TenPhonicsItem('K', R.drawable.k_is_for_kiwi),
    TenPhonicsItem('L', R.drawable.l_is_for_lemon),
    TenPhonicsItem('M', R.drawable.m_is_for_monkey),
    TenPhonicsItem('N', R.drawable.n_is_for_nest),
    TenPhonicsItem('O', R.drawable.o_is_for_owl),
    TenPhonicsItem('P', R.drawable.p_is_for_peach),
    TenPhonicsItem('Q', R.drawable.q_is_for_queen),
    TenPhonicsItem('R', R.drawable.r_is_for_robin),
    TenPhonicsItem('S', R.drawable.s_is_for_sailboat),
    TenPhonicsItem('T', R.drawable.t_is_for_turtle),
    TenPhonicsItem('U', R.drawable.u_is_for_umbrella),
    TenPhonicsItem('V', R.drawable.v_is_for_violet),
    TenPhonicsItem('W', R.drawable.w_is_for_window),
    TenPhonicsItem('X', R.drawable.x_is_for_x_ray),
    TenPhonicsItem('Y', R.drawable.y_is_for_yo_yo),
    TenPhonicsItem('Z', R.drawable.z_is_for_zebra)
)

private val TEN_FIND_WORDS: Map<Char, String> = mapOf(
    'A' to "CAT",
    'B' to "BUS",
    'C' to "CORN",
    'D' to "DOG",
    'E' to "WEB",
    'F' to "FAN",
    'G' to "GOAT",
    'H' to "HAT",
    'I' to "LION",
    'J' to "JAM",
    'K' to "KITE",
    'L' to "LAMP",
    'M' to "MOON",
    'N' to "NEST",
    'O' to "FROG",
    'P' to "PEN",
    'Q' to "QUEEN",
    'R' to "BIRD",
    'S' to "SUN",
    'T' to "TOY",
    'U' to "MUG",
    'V' to "VAN",
    'W' to "WAGON",
    'X' to "BOX",
    'Y' to "YARN",
    'Z' to "ZEBRA"
)

private fun buildTenRound(used: Set<Char>): TenRound {
    val letters = ('A'..'Z').toList()
    val pool = letters.filter { it !in used }.ifEmpty { letters }
    val target = pool.random()
    return when ((0..2).random()) {
        0 -> { // Tap the letter among letters
            val opts = letters.filter { it != target }.shuffled().take(5) + target
            TapLetterRound(letter = target, options = opts.shuffled())
        }
        1 -> { // Phonics picture pick
            val t = TEN_PHONICS_DATA.first { it.letter == target }
            val distractors = TEN_PHONICS_DATA.filter { it.letter != target }.shuffled().take(3)
            val options = (listOf(t) + distractors).map { it.imageRes }.shuffled()
            PhonicsRound10(letter = target, options = options, correctRes = t.imageRes)
        }
        else -> { // Find letter in word
            val word = TEN_FIND_WORDS[target] ?: "CAT"
            FindInWordRound10(letter = target, word = word)
        }
    }
}

@Composable
fun LevelTenGame(
    onCompleted: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    // --- state ---
    val totalRounds = 10
    var roundIndex by rememberSaveable { mutableIntStateOf(0) }
    var score by rememberSaveable { mutableIntStateOf(0) }
    val used = remember { mutableStateListOf<Char>() }
    var round by remember { mutableStateOf(buildTenRound(used.toSet())) }

    var showNext by remember { mutableStateOf(false) }

    // Speak the target letter each round
    LaunchedEffect(round.letter) {
        showNext = false
        letterAudio[round.letter.toString()]?.let { playSoundForFlash(it, context) }
    }

    fun nextRound() {
        used += round.letter
        if (roundIndex + 1 >= totalRounds) onCompleted()
        else {
            roundIndex++
            round = buildTenRound(used.toSet())
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = this.maxWidth
        val maxH = this.maxHeight
        val isPortrait = maxH > maxW
        val shortSide = if (maxW < maxH) maxW else maxH

        val gap = if (isPortrait) 16.dp else 12.dp
        val scoreSize = if (isPortrait) 26.sp else 22.sp

        // Background full-bleed
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
                .padding(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score / round line
            Text(
                text = "Round ${roundIndex + 1} / $totalRounds   Score: $score",
                fontSize = scoreSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(gap))

            // Replay audio button (repeats the letter prompt)
            val replaySize = shortSide * (if (isPortrait) 0.20f else 0.18f)
            Image(
                painter = painterResource(R.drawable.btn_replay_audio),
                contentDescription = "Replay Audio",
                modifier = Modifier
                    .size(replaySize)
                    .noRippleClickable {
                        letterAudio[round.letter.toString()]?.let { playSoundForFlash(it, context) }
                    },
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(gap * 2))

            // --- Round content ---
            when (round) {
                // 1) Tap the target letter among letters
                is TapLetterRound -> {
                    val r = round as TapLetterRound
                    val columns = if (isPortrait) 3 else 6
                    val baseTile = shortSide * (if (isPortrait) 0.22f else 0.16f)
                    val byWidth = (maxW - gap * (columns - 1) - gap * 2) / columns.toFloat()
                    val tileSize = minOf(baseTile, byWidth)

                    var selected by remember(round) { mutableStateOf<Char?>(null) }

                    // 3x2 (portrait) or 1x6 (landscape)
                    r.options.chunked(columns).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = gap)
                        ) {
                            row.forEach { letter ->
                                val isCorrect = letter == r.letter
                                val borderColor = when {
                                    selected == null -> Color.Transparent
                                    isCorrect -> Color(0xFF00FF08)
                                    selected == letter -> Color(0xFFFF0000)
                                    else -> Color.Transparent
                                }

                                // white letter tile
                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White)
                                        .border(3.dp, borderColor, RoundedCornerShape(14.dp))
                                        .noRippleClickable(enabled = selected == null) {
                                            selected = letter
                                            if (isCorrect) {
                                                score++
                                                playSoundForFlash(R.raw.small_dino_raspy, context)
                                            } else {
                                                playSoundForFlash(R.raw.buzzer, context)
                                            }
                                            showNext = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter.toString(),
                                        color = Color(0xFF2A2A2A),
                                        fontSize = if (isPortrait) 38.sp else 34.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(gap))
                    }
                }

                // 2) Phonics picture pick
                is PhonicsRound10 -> {
                    val r = round as PhonicsRound10
                    val gridCols = if (isPortrait) 2 else 4
                    val optionSize = run {
                        val innerW = maxW - gap * 2
                        val byCols = (innerW - gap * (gridCols - 1)) / gridCols
                        val softCap = shortSide * (if (isPortrait) 0.32f else 0.24f)
                        byCols.coerceAtMost(softCap)
                    }
                    var picked by remember(round) { mutableStateOf<Int?>(null) }
                    val corner = 14.dp

                    r.options.chunked(gridCols).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { imgRes ->
                                val borderColor = when {
                                    picked == null -> Color.Transparent
                                    imgRes == r.correctRes -> Color(0xFF00FF08)
                                    picked == imgRes -> Color(0xFFFF0000)
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .size(optionSize)
                                        .clip(RoundedCornerShape(corner))
                                        .background(Color.White) // white tile
                                        .border(3.dp, borderColor, RoundedCornerShape(corner))
                                        .noRippleClickable(enabled = picked == null) {
                                            picked = imgRes
                                            val correct = imgRes == r.correctRes
                                            if (correct) {
                                                score++
                                                playSoundForFlash(R.raw.small_dino_raspy, context)
                                                backAudioByImage[imgRes]?.let { playSoundForFlash(it, context) }
                                            } else {
                                                playSoundForFlash(R.raw.buzzer, context)
                                            }
                                            showNext = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(imgRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(gap))
                    }
                }

                // 3) Find the letter inside a word
                is FindInWordRound10 -> {
                    val r = round as FindInWordRound10
                    val tiles = r.word.toCharArray().toList()

                    val baseSize = shortSide * (if (isPortrait) 0.18f else 0.16f)
                    val byWidth = (maxW - gap * (tiles.size - 1) - gap * 2) / tiles.size.toFloat()
                    val tileSize = minOf(baseSize, byWidth)

                    var pickedIndex by remember(round) { mutableStateOf<Int?>(null) }
                    val correctIndex = tiles.indexOf(r.letter)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(gap),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = gap)
                    ) {
                        tiles.forEachIndexed { i, ch ->
                            val borderColor = when {
                                pickedIndex == null -> Color.Transparent
                                i == correctIndex -> Color(0xFF00FF08)
                                i == pickedIndex -> Color(0xFFFF0000)
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(Color(0xFF7C6EB0), RoundedCornerShape(16.dp))
                                    .border(4.dp, borderColor, RoundedCornerShape(16.dp))
                                    .then(
                                        if (pickedIndex == null) Modifier.noRippleClickable {
                                            pickedIndex = i
                                            if (i == correctIndex) {
                                                score++
                                                playSoundForFlash(R.raw.small_dino_raspy, context)
                                            } else {
                                                playSoundForFlash(R.raw.buzzer, context)
                                            }
                                            showNext = true
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ch.toString(),
                                    color = Color.White,
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Next button
            if (showNext) {
                Spacer(Modifier.height(gap * 2))
                Image(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(shortSide * (if (isPortrait) 0.28f else 0.20f))
                        .noRippleClickable { nextRound() },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Pause overlay
        PauseMenuOverlay(navController)
    }
}



@Composable
fun LearningIntroScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("learnABCDE")
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val cardSize = shortSide * if (isPortrait) 6f else 26f

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.a_b_c_flashcard),
                contentDescription = "ABC Flashcard",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(cardSize)
            )
        }
    }
}

@Composable
fun LearningIntroTwoScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000)
        navController.navigate("questionsA-Z")
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val signSize = shortSide * if (isPortrait) 6f else 26f

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.letter_matching_sign),
                contentDescription = "Letter Matching Sign",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(signSize)
            )
        }
    }
}

@Composable
fun DinoFlashcardViewer(navController: NavController, flashcards: List<DinoFlashcard>) {
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val aspect = 4f / 3f // whiteboard width:height

        // Preload big, static painters to avoid their own decode flicker
        val bgPainter    = painterResource(R.drawable.dlmainbackground)
        val boardPainter = painterResource(R.drawable.whiteboard)



        // background
        Image(
            painter = bgPainter,
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        // Layout padding (safe + screen padding)
        val outerPad = if (isPortrait) 20.dp else 25.dp      // was 40.dp in portrait → gives you more room

        val availW = maxWidth - outerPad * 2
        val availH = maxHeight - outerPad * 2

// How much of the available width/height we allow the board to use
        val targetFracW = if (isPortrait) 2f else 0.82f   // was 0.80f in portrait → grows board
        val targetFracH = if (isPortrait) 0.98f else 1.00f   // optional: allow ~98% of available height

// Largest board that fits with 4:3 aspect
        val widthByW = availW * targetFracW
        val widthByH = (availH * targetFracH) * aspect       // apply height fraction to the height cap
        val boardW   = widthByW.coerceAtMost(widthByH)
        val boardH   = boardW / aspect


        // Whiteboard layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPad),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = boardPainter,
                contentDescription = "Whiteboard",
                modifier = Modifier.size(boardW, boardH),
                contentScale = ContentScale.Fit
            )
        }

        // Foreground (flashcard)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(outerPad),
            contentAlignment = Alignment.Center
        ) {
            if (currentIndex < flashcards.size) {
                val currentCard = flashcards[currentIndex]

                // play letter when card appears
                LaunchedEffect(currentCard) {
                    letterAudio[currentCard.letter]?.let { playSoundForFlash(it, context) }
                }

                val cardSize = boardW * (if (isPortrait) 0.46f else 0.40f)

                DinoFlashcardCardNoFlicker(
                    card = currentCard,
                    onNext = { currentIndex++ },
                    sizeHint = cardSize
                )
            } else {
                LaunchedEffect(Unit) {
                    delay(1000)
                    val reviewRoute = when (flashcards.firstOrNull()?.letter) {
                        "A" -> "flashcardReview"
                        "F" -> "flashcardReviewFJ"
                        "K" -> "flashcardReviewKO"
                        "P" -> "flashcardReviewPT"
                        "U" -> "flashcardReviewUZ"
                        else -> "flashcardReview"
                    }
                    navController.navigate(reviewRoute)
                }
            }
        }
    }
}

@Composable
fun DinoFlashcardCardNoFlicker(
    card: DinoFlashcard,
    onNext: () -> Unit,
    sizeHint: Dp = 260.dp
) {
    val context = LocalContext.current
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var showingFront by remember(card) { mutableStateOf(true) }

    // Per-card painters (belongs HERE, not in the viewer)
    val frontPainter = painterResource(card.frontImage)
    val backPainter  = painterResource(card.backImage)

    val frontAlpha = remember { Animatable(1f) }
    val backAlpha  = remember { Animatable(0f) }

    LaunchedEffect(card) {
        frontAlpha.snapTo(1f)
        backAlpha.snapTo(0f)
        showingFront = true
    }

    suspend fun flipToBack() {
        backAudioByImage[card.backImage]?.let { playSoundForFlash(it, context) }
        backAlpha.snapTo(0f)
        frontAlpha.snapTo(1f)
        backAlpha.animateTo(1f, tween(120))
        frontAlpha.snapTo(0f)
        showingFront = false
    }

    Box(
        modifier = Modifier
            .size(sizeHint)
            .noRippleClickable(enabled = !busy) {
                if (busy) return@noRippleClickable
                busy = true
                scope.launch {
                    if (showingFront) flipToBack() else onNext()
                    delay(160)
                    busy = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Front below, back above; one stays opaque while the other fades in
        Image(
            painter = frontPainter,
            contentDescription = "${card.letter} card front",
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = frontAlpha.value },
            contentScale = ContentScale.Fit
        )
        Image(
            painter = backPainter,
            contentDescription = "${card.letter} card back",
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = backAlpha.value },
            contentScale = ContentScale.Fit
        )
    }
}



@Composable
fun FlashcardReviewScreen(
    onReplay: () -> Unit,
    onNextSet: () -> Unit,
    navController: NavController
) {
    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val titleWidth = shortSide * 0.90f
        val buttonSize = shortSide * 0.35f
        val pad = 16.dp

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        val content: @Composable () -> Unit = {
            Image(
                painter = painterResource(R.drawable.great_job_a_e),
                contentDescription = "Great job A–E",
                modifier = Modifier
                    .width(titleWidth)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.replay_button_150),
                    contentDescription = "Replay A–E",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable { onReplay() },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.learn_f_j_150),
                    contentDescription = "Learn F–J",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable {
                            activity?.let {
                                Ads.showThen(
                                    activity = it,
                                    pauseAudio = { },
                                    resumeAudio = { },
                                    after = onNextSet
                                )
                            } ?: onNextSet()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { content() }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
fun FlashcardReviewFJScreen(
    onReplay: () -> Unit,
    onNextSet: () -> Unit,
    navController: NavController
) {
    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val titleWidth = shortSide * 0.90f
        val buttonSize = shortSide * 0.35f
        val pad = 16.dp

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        val content: @Composable () -> Unit = {
            Image(
                painter = painterResource(R.drawable.great_job_f_j),
                contentDescription = "Great job F–J",
                modifier = Modifier
                    .width(titleWidth)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.replay_button_150),
                    contentDescription = "Replay F–J",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable { onReplay() },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.learn_k_o_150),
                    contentDescription = "Learn K–O",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable {
                            activity?.let {
                                Ads.showThen(
                                    activity = it,
                                    pauseAudio = { },
                                    resumeAudio = { },
                                    after = onNextSet
                                )
                            } ?: onNextSet()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { content() }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
fun FlashcardReviewKOScreen(
    onReplay: () -> Unit,
    onNextSet: () -> Unit,
    navController: NavController
) {
    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val titleWidth = shortSide * 0.90f
        val buttonSize = shortSide * 0.35f
        val pad = 16.dp

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        val content: @Composable () -> Unit = {
            Image(
                painter = painterResource(R.drawable.great_job_k_o),
                contentDescription = "Great job K–O",
                modifier = Modifier
                    .width(titleWidth)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.replay_button_150),
                    contentDescription = "Replay K–O",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable { onReplay() },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.learn_p_t_150),
                    contentDescription = "Learn P–T",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable {
                            activity?.let {
                                Ads.showThen(
                                    activity = it,
                                    pauseAudio = { },
                                    resumeAudio = { },
                                    after = onNextSet
                                )
                            } ?: onNextSet()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { content() }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
fun FlashcardReviewPTScreen(
    onReplay: () -> Unit,
    onNextSet: () -> Unit,
    navController: NavController
) {
    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val titleWidth = shortSide * 0.90f
        val buttonSize = shortSide * 0.35f
        val pad = 16.dp

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        val content: @Composable () -> Unit = {
            Image(
                painter = painterResource(R.drawable.great_job_p_t),
                contentDescription = "Great job P–T",
                modifier = Modifier
                    .width(titleWidth)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.replay_button_150),
                    contentDescription = "Replay",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable { onReplay() },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.learn_u_z_150),
                    contentDescription = "Learn U–Z",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable {
                            activity?.let { Ads.showThen(activity = it, after = onNextSet) }
                                ?: onNextSet()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { content() }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
fun FlashcardReviewUZScreen(
    onReplay: () -> Unit,
    onNextSet: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ProfilesStore.markObjective(context, "quiz_az_passed")
    }

    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val titleWidth = shortSide * 0.90f
        val buttonSize = shortSide * 0.35f
        val pad = 16.dp

        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = "Main Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        val content: @Composable () -> Unit = {
            Image(
                painter = painterResource(R.drawable.great_job_u_z),
                contentDescription = "Great job U–Z",
                modifier = Modifier
                    .width(titleWidth)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.replay_button_150),
                    contentDescription = "Replay U–Z",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable { onReplay() },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(R.drawable.level_2_150),
                    contentDescription = "Level 2",
                    modifier = Modifier
                        .size(buttonSize)
                        .noRippleClickable {
                            activity?.let {
                                Ads.showThen(
                                    activity = it,
                                    pauseAudio = { },
                                    resumeAudio = { },
                                    after = onNextSet
                                )
                            } ?: onNextSet()
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (isPortrait) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { content() }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) { content() }
        }

        PauseMenuOverlay(navController)
    }
}

@Composable
fun ABCQuizReviewScreen(
    navController: NavController,
    onReplay: () -> Unit,
    score: Int
) {
    val context = LocalContext.current

    // -------- Unlock Level 3 unconditionally on first entry --------
    var didUnlock by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!didUnlock) {
            unlockNextLevel(context, completedLevelId = 2)  // always bump to Level 3
            didUnlock = true
        }
    }

    // -------- Award stickers conditionally --------
    var awarded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(score) {
        if (!awarded) {
            when {
                score >= 26 -> {
                    // perfect score gets both stickers
                    ProfilesStore.markObjective(context, "quiz_az_passed")
                    ProfilesStore.markObjective(context, "quiz_az_20plus")
                    awarded = true
                }
                score >= 20 -> {
                    ProfilesStore.markObjective(context, "quiz_az_20plus")
                    awarded = true
                }
            }
        }
    }


    val activity = LocalContext.current.findActivity()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        // Sizing knobs
        val titleWidth = shortSide * if (isPortrait) 0.75f else 0.60f
        val textSize   = if (isPortrait) 40.sp else 28.sp
        val pad        = if (isPortrait) 24.dp else 16.dp
        val gap        = if (isPortrait) 24.dp else 16.dp

        // Base target button size
        val targetButtonSize = shortSide * if (isPortrait) 0.28f else 0.22f

        // Portrait: ensure 3 buttons + 2 gaps fit in a single row
        val buttonSizePortrait = minOf(
            targetButtonSize,
            (maxWidth - pad * 2 - gap * 2) / 3
        )
        // Landscape can keep the target size
        val buttonSizeLandscape = targetButtonSize

        // Background
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
                    .padding(pad),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.great_job_score),
                    contentDescription = null,
                    modifier = Modifier.width(titleWidth),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(gap))

                Text(
                    text = "Score: $score / ${quizAtoZ.size}",
                    fontSize = textSize,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(gap))

                // Buttons row: Next Level • Level Select • Replay
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.next_level_button),
                        contentDescription = "Next Level",
                        modifier = Modifier
                            .size(buttonSizePortrait)
                            .noRippleClickable { navController.navigate("level3") },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(R.drawable.level_select_button),
                        contentDescription = "Level Select",
                        modifier = Modifier
                            .size(buttonSizePortrait)
                            .noRippleClickable { navController.navigate("levelSelect") },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(R.drawable.replay_button_150),
                        contentDescription = "Replay",
                        modifier = Modifier
                            .size(buttonSizePortrait)
                            .noRippleClickable {
                                activity?.let { Ads.showThen(activity = it, after = onReplay) } ?: onReplay()
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
                    .padding(pad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: header
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.great_job_score),
                            contentDescription = null,
                            modifier = Modifier.width(titleWidth),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(gap))
                        Text(
                            text = "Score: $score / ${quizAtoZ.size}",
                            fontSize = textSize,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(gap))

                // Right: vertical stack of buttons
                Column(
                    modifier = Modifier
                        .weight(0.8f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(gap)
                ) {
                    Image(
                        painter = painterResource(R.drawable.next_level_button),
                        contentDescription = "Next Level",
                        modifier = Modifier
                            .size(buttonSizeLandscape)
                            .noRippleClickable { navController.navigate("level3") },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(R.drawable.level_select_button),
                        contentDescription = "Level Select",
                        modifier = Modifier
                            .size(buttonSizeLandscape)
                            .noRippleClickable { navController.navigate("levelSelect") },
                        contentScale = ContentScale.Fit
                    )
                    Image(
                        painter = painterResource(R.drawable.replay_button_150),
                        contentDescription = "Replay",
                        modifier = Modifier
                            .size(buttonSizeLandscape)
                            .noRippleClickable {
                                activity?.let { Ads.showThen(activity = it, after = onReplay) } ?: onReplay()
                            },
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // Pause overlay
        PauseMenuOverlay(navController)
    }
}











// --- Data model ---
data class QuizItem(
    val questionAudioRes: Int,
    val correctAnswer: String,
    val options: List<String>,
    val imageMap: Map<String, Int>
)

// --- Sample Questions for A–Z ---
val quizAtoZ = listOf(
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_a,
        correctAnswer = "A",
        options = listOf("A", "B", "C", "D", "E"),
        imageMap = mapOf(
            "A" to R.drawable.a_square_card,
            "B" to R.drawable.b_square_card,
            "C" to R.drawable.c_square_card,
            "D" to R.drawable.d_square_card,
            "E" to R.drawable.e_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_c,
        correctAnswer = "C",
        options = listOf("A", "B", "C", "D", "E"),
        imageMap = mapOf(
            "A" to R.drawable.a_square_card,
            "B" to R.drawable.b_square_card,
            "C" to R.drawable.c_square_card,
            "D" to R.drawable.d_square_card,
            "E" to R.drawable.e_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_b,
        correctAnswer = "B",
        options = listOf("A", "B", "C", "D", "E"),
        imageMap = mapOf(
            "A" to R.drawable.a_square_card,
            "B" to R.drawable.b_square_card,
            "C" to R.drawable.c_square_card,
            "D" to R.drawable.d_square_card,
            "E" to R.drawable.e_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_e,
        correctAnswer = "E",
        options = listOf("A", "B", "C", "D", "E"),
        imageMap = mapOf(
            "A" to R.drawable.a_square_card,
            "B" to R.drawable.b_square_card,
            "C" to R.drawable.c_square_card,
            "D" to R.drawable.d_square_card,
            "E" to R.drawable.e_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_d,
        correctAnswer = "D",
        options = listOf("A", "B", "C", "D", "E"),
        imageMap = mapOf(
            "A" to R.drawable.a_square_card,
            "B" to R.drawable.b_square_card,
            "C" to R.drawable.c_square_card,
            "D" to R.drawable.d_square_card,
            "E" to R.drawable.e_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_g,
        correctAnswer = "G",
        options = listOf("F", "G", "H", "I", "J"),
        imageMap = mapOf(
            "F" to R.drawable.f_square_card,
            "G" to R.drawable.g_square_card,
            "H" to R.drawable.h_square_card,
            "I" to R.drawable.i_square_card,
            "J" to R.drawable.j_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_f,
        correctAnswer = "F",
        options = listOf("F", "G", "H", "I", "J"),
        imageMap = mapOf(
            "F" to R.drawable.f_square_card,
            "G" to R.drawable.g_square_card,
            "H" to R.drawable.h_square_card,
            "I" to R.drawable.i_square_card,
            "J" to R.drawable.j_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_j,
        correctAnswer = "J",
        options = listOf("F", "G", "H", "I", "J"),
        imageMap = mapOf(
            "F" to R.drawable.f_square_card,
            "G" to R.drawable.g_square_card,
            "H" to R.drawable.h_square_card,
            "I" to R.drawable.i_square_card,
            "J" to R.drawable.j_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_i,
        correctAnswer = "I",
        options = listOf("F", "G", "H", "I", "J"),
        imageMap = mapOf(
            "F" to R.drawable.f_square_card,
            "G" to R.drawable.g_square_card,
            "H" to R.drawable.h_square_card,
            "I" to R.drawable.i_square_card,
            "J" to R.drawable.j_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_h,
        correctAnswer = "H",
        options = listOf("F", "G", "H", "I", "J"),
        imageMap = mapOf(
            "F" to R.drawable.f_square_card,
            "G" to R.drawable.g_square_card,
            "H" to R.drawable.h_square_card,
            "I" to R.drawable.i_square_card,
            "J" to R.drawable.j_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_o,
        correctAnswer = "O",
        options = listOf("K", "L", "M", "N", "O"),
        imageMap = mapOf(
            "K" to R.drawable.k_square_card,
            "L" to R.drawable.l_square_card,
            "M" to R.drawable.m_square_card,
            "N" to R.drawable.n_square_card,
            "O" to R.drawable.o_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_l,
        correctAnswer = "L",
        options = listOf("K", "L", "M", "N", "O"),
        imageMap = mapOf(
            "K" to R.drawable.k_square_card,
            "L" to R.drawable.l_square_card,
            "M" to R.drawable.m_square_card,
            "N" to R.drawable.n_square_card,
            "O" to R.drawable.o_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_k,
        correctAnswer = "K",
        options = listOf("K", "L", "M", "N", "O"),
        imageMap = mapOf(
            "K" to R.drawable.k_square_card,
            "L" to R.drawable.l_square_card,
            "M" to R.drawable.m_square_card,
            "N" to R.drawable.n_square_card,
            "O" to R.drawable.o_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_m,
        correctAnswer = "M",
        options = listOf("K", "L", "M", "N", "O"),
        imageMap = mapOf(
            "K" to R.drawable.k_square_card,
            "L" to R.drawable.l_square_card,
            "M" to R.drawable.m_square_card,
            "N" to R.drawable.n_square_card,
            "O" to R.drawable.o_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_n,
        correctAnswer = "N",
        options = listOf("K", "L", "M", "N", "O"),
        imageMap = mapOf(
            "K" to R.drawable.k_square_card,
            "L" to R.drawable.l_square_card,
            "M" to R.drawable.m_square_card,
            "N" to R.drawable.n_square_card,
            "O" to R.drawable.o_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_r,
        correctAnswer = "R",
        options = listOf("P", "Q", "R", "S", "T"),
        imageMap = mapOf(
            "P" to R.drawable.p_square_card,
            "Q" to R.drawable.q_square_card,
            "R" to R.drawable.r_square_card,
            "S" to R.drawable.s_square_card,
            "T" to R.drawable.t_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_t,
        correctAnswer = "T",
        options = listOf("P", "Q", "R", "S", "T"),
        imageMap = mapOf(
            "P" to R.drawable.p_square_card,
            "Q" to R.drawable.q_square_card,
            "R" to R.drawable.r_square_card,
            "S" to R.drawable.s_square_card,
            "T" to R.drawable.t_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_s,
        correctAnswer = "S",
        options = listOf("P", "Q", "R", "S", "T"),
        imageMap = mapOf(
            "P" to R.drawable.p_square_card,
            "Q" to R.drawable.q_square_card,
            "R" to R.drawable.r_square_card,
            "S" to R.drawable.s_square_card,
            "T" to R.drawable.t_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_q,
        correctAnswer = "Q",
        options = listOf("P", "Q", "R", "S", "T"),
        imageMap = mapOf(
            "P" to R.drawable.p_square_card,
            "Q" to R.drawable.q_square_card,
            "R" to R.drawable.r_square_card,
            "S" to R.drawable.s_square_card,
            "T" to R.drawable.t_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_p,
        correctAnswer = "P",
        options = listOf("P", "Q", "R", "S", "T"),
        imageMap = mapOf(
            "P" to R.drawable.p_square_card,
            "Q" to R.drawable.q_square_card,
            "R" to R.drawable.r_square_card,
            "S" to R.drawable.s_square_card,
            "T" to R.drawable.t_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_u,
        correctAnswer = "U",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_y,
        correctAnswer = "Y",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_x,
        correctAnswer = "X",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_v,
        correctAnswer = "V",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_z,
        correctAnswer = "Z",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
    QuizItem(
        questionAudioRes = R.raw.which_letter_is_the_letter_w,
        correctAnswer = "W",
        options = listOf("U", "V", "W", "X", "Y", "Z"),
        imageMap = mapOf(
            "U" to R.drawable.u_square_card,
            "V" to R.drawable.v_square_card,
            "W" to R.drawable.w_square_card,
            "X" to R.drawable.x_square_card,
            "Y" to R.drawable.y_square_card,
            "Z" to R.drawable.z_square_card
        )
    ),
)

@Composable
fun PlayRawOnceSafe(resId: Int) {
    val context = LocalContext.current
    DisposableEffect(resId) {
        val player = MediaPlayer.create(context, resId)
        player.setOnCompletionListener { /* no-op */ }
        player.start()
        onDispose {
            try { player.stop() } catch (_: Throwable) {}
            player.release()
        }
    }
}

@Composable
fun PlaySoundOnce(resId: Int) {
    val context = LocalContext.current
    DisposableEffect(resId) {
        val player = MediaPlayer.create(context, resId)
        player.start()
        onDispose {
            try { player.stop() } catch (_: Throwable) {}
            player.release()
        }
    }
}

@Composable
fun QuestionsAZ(navController: NavController) {
    val context = LocalContext.current
    var questionIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showNext by remember { mutableStateOf(false) }
    var score by rememberSaveable { mutableIntStateOf(0) }

    val currentQuestion = quizAtoZ[questionIndex]

    // auto-play the question prompt once on each question
    PlaySoundOnce(currentQuestion.questionAudioRes)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        val scoreTextSize = if (isPortrait) 26.sp else 22.sp
        val spacing = if (isPortrait) 16.dp else 12.dp
        val gridCols = if (isPortrait) 2 else minOf(5, currentQuestion.options.size)
        val optionSize = shortSide * if (isPortrait) 0.28f else 0.32f
        val borderWidth = 3.dp
        val nextButtonSize = shortSide * if (isPortrait) 0.28f else 0.20f
        val replaySize = shortSide * if (isPortrait) 0.20f else 0.20f   // <— NEW

        // background
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
                .padding(horizontal = spacing, vertical = spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // score
            Text(
                text = "Score: $score",
                fontSize = scoreTextSize,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(spacing))

            // --- Replay Audio image button (uses your drawable) ---
            Image(
                painter = painterResource(R.drawable.btn_replay_audio), // swap to your replay asset id
                contentDescription = "Replay audio",
                modifier = Modifier
                    .size(replaySize)
                    .noRippleClickable {
                        // replay the current question's audio
                        playSoundForFlash(currentQuestion.questionAudioRes, context)
                    },
                contentScale = ContentScale.Fit
            )

            // keep overall spacing similar to before (score -> big gap -> grid)
            Spacer(Modifier.height(spacing * 1))

            // options grid
            val rows = currentQuestion.options.chunked(gridCols)
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = true)
            ) {
                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        row.forEach { letter ->
                            val imageRes = currentQuestion.imageMap[letter] ?: return@forEach
                            val borderColor = when {
                                selectedAnswer == null -> Color.Transparent
                                letter == currentQuestion.correctAnswer -> Color(0xFF00FF08) // green
                                letter == selectedAnswer -> Color(0xFFFF0000)                // red
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .size(optionSize)
                                    .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
                                    .noRippleClickable(enabled = selectedAnswer == null) {
                                        selectedAnswer = letter
                                        val isCorrect = letter == currentQuestion.correctAnswer
                                        if (isCorrect) {
                                            score++
                                            playSoundForFlash(R.raw.small_dino_raspy, context)
                                        } else {
                                            playSoundForFlash(R.raw.buzzer, context)
                                        }
                                        showNext = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(imageRes),
                                    contentDescription = letter,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing))

            // Next button (only visible after an answer)
            if (showNext) {
                Image(
                    painter = painterResource(R.drawable.next_button),
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(nextButtonSize)
                        .noRippleClickable {
                            selectedAnswer = null
                            showNext = false
                            if (questionIndex < quizAtoZ.lastIndex) {
                                questionIndex++
                            } else {
                                navController.navigate("abcQuizReview/$score")
                            }
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }

        // pause menu overlay on top
        PauseMenuOverlay(navController)
    }
}