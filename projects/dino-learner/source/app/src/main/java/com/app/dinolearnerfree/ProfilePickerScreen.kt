package com.app.dinolearnerfree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.annotation.DrawableRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape



@Composable
fun ProfilePickerScreen(nav: NavController) {
    val context = LocalContext.current
    val profiles by ProfilesStore.profilesFlow(context).collectAsState(initial = emptyList())
    val selectedId by ProfilesStore.selectedIdFlow(context).collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    // NEW: survive rotation
    var showNewDialog by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isPortrait = maxHeight > maxWidth
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight

        // Tunables per orientation
        val sidePad   = if (isPortrait) 20.dp else 24.dp
        val topPad    = if (isPortrait) 20.dp else 16.dp
        val gridGap   = if (isPortrait) 18.dp else 14.dp
        val titleSize = if (isPortrait) 30.sp else 24.sp

        // Create button size (keeps your image aspect ~150x80 ≈ 0.53)
        val createW = shortSide * (if (isPortrait) 0.38f else 0.26f)
        val createH = createW * 0.53f

        // Background
        Image(
            painter = painterResource(R.drawable.dlmainbackground),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )

        if (isPortrait) {
            // ----- PORTRAIT -----
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = sidePad, vertical = topPad),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Choose a Player", fontSize = titleSize, fontWeight = FontWeight.Black, color = Color(0xFF2E2E2E))
                Spacer(Modifier.height(16.dp))

                if (profiles.isEmpty()) {
                    Text("No players yet. Create one below!", color = Color(0xFF444444))
                    Spacer(Modifier.height(8.dp))
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(gridGap),
                    verticalArrangement = Arrangement.spacedBy(gridGap),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(profiles, key = { it.id }) { p ->
                        ProfileCard(
                            name = p.name,
                            hue = p.avatarHue,
                            isSelected = p.id == selectedId
                        ) {
                            scope.launch {
                                ProfilesStore.setSelected(context, p.id)
                                nav.navigate("levelSelect")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // OPEN the dialog from parent
                CreateProfileImageButton(
                    imageRes = R.drawable.create_player,
                    contentDescription = "Create Player",
                    width = createW,
                    height = createH
                ) {
                    showNewDialog = true
                }
            }
        } else {
            // ----- LANDSCAPE -----
            val createW = shortSide * 0.26f
            val createH = createW * 0.53f
            val topReserve = 56.dp
            val bottomReserve = createH + 20.dp

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = sidePad, vertical = topPad)
            ) {
                Text(
                    "Choose a Player",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E2E2E),
                    modifier = Modifier.align(Alignment.TopCenter)
                )

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(top = topReserve, bottom = bottomReserve),
                    horizontalArrangement = Arrangement.spacedBy(gridGap),
                    verticalArrangement = Arrangement.spacedBy(gridGap)
                ) {
                    profiles.forEach { p ->
                        ProfileCard(
                            name = p.name,
                            hue = p.avatarHue,
                            isSelected = p.id == selectedId
                        ) {
                            scope.launch {
                                ProfilesStore.setSelected(context, p.id)
                                nav.navigate("levelSelect")
                            }
                        }
                    }
                }

                CreateProfileImageButton(
                    imageRes = R.drawable.create_player,
                    contentDescription = "Create Player",
                    width = createW,
                    height = createH,
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    showNewDialog = true
                }
            }
        }

        // Floating pause menu on top
        PauseMenuOverlay(nav)
    }

    // ---- Dialog lives OUTSIDE the orientation branches ----
    if (showNewDialog) {
        NewPlayerDialog(
            onDismiss = { showNewDialog = false },
            onCreate = { name ->
                // Create, then go to level select
                if (name.isNotBlank()) {
                    scope.launch {
                        ProfilesStore.createProfile(context, name.trim())
                        showNewDialog = false
                        nav.navigate("levelSelect")
                    }
                }
            }
        )
    }
}



@Composable
private fun ProfileCard(name: String, hue: Float, isSelected: Boolean, onClick: () -> Unit) {
    val color = Color.hsl(hue, 0.55f, 0.55f)
    Surface(
        tonalElevation = if (isSelected) 6.dp else 1.dp,
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .width(160.dp)
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.SemiBold)
        }
    }
}



@Composable
fun CreateProfileImageButton(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    width: Dp = 150.dp,
    height: Dp = 80.dp,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(width = width, height = height)
            .clickable(
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    )
}








@Composable
fun NewPlayerDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    @DrawableRes panelRes: Int = R.drawable.new_player_temp,
    @DrawableRes createRes: Int = R.drawable.btn_create,
    @DrawableRes cancelRes: Int = R.drawable.btn_cancel,
) {
    var name by rememberSaveable { mutableStateOf("") }


    val cfg = LocalConfiguration.current
    val isLandscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Panel size a bit smaller in portrait so there’s room for buttons underneath
    val panelAspect        = 1015f / 728f
    val panelWidthFraction = if (isLandscape) 0.40f else 0.80f
    val buttonHeight       = if (isLandscape) 56.dp else 64.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // We’ll stack: [Panel Box] then [Buttons Row] underneath it
        Column(
            modifier = Modifier
                .fillMaxWidth(panelWidthFraction)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Panel (stone image) with the text field inside it ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(panelAspect),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(panelRes),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                // --- Name text field positioned by % of panel image ---
                BoxWithConstraints(
                    modifier = Modifier
                        .matchParentSize()  // same size as the panel image
                ) {
                    val isLandscape = LocalConfiguration.current.orientation ==
                            Configuration.ORIENTATION_LANDSCAPE

                    // Tweak these three knobs if your art changes:
                    val fieldWidthFrac   = if (isLandscape) 0.58f else 0.60f   // width relative to panel width
                    val fieldBottomFrac  = if (isLandscape) 0.12f else 0.11f   // distance from panel bottom (as % of panel height)
                    val fieldXShift      = if (isLandscape) 37.dp else 37.dp    // slight nudge to the right to center in white area

                    val panelW = maxWidth      // Dp
                    val panelH = maxHeight     // Dp

                    val fieldWidth  = panelW * fieldWidthFrac
                    val fieldBottom = panelH * fieldBottomFrac

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(20) },
                        singleLine = true,
                        placeholder = { Text("Type name") },
                        textStyle = LocalTextStyle.current.copy(fontSize = 22.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val n = name.trim()
                                if (n.isNotEmpty()) { onCreate(n); onDismiss() }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            cursorColor = Color.Black
                        )
                        ,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = fieldBottom)
                            .offset(x = fieldXShift)
                            .width(fieldWidth)
                            .height(60.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                }

            }

            // A little space between the panel and the buttons
            Spacer(Modifier.height(if (isLandscape) 3.dp else 12.dp))

            // --- Buttons OUTSIDE the panel, centered under it ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(createRes),
                    contentDescription = "Create",
                    modifier = Modifier
                        .size(width = 140.dp, height = buttonHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // disables ripple & pressed shadow
                        ) {
                            val n = name.trim()
                            if (n.isNotEmpty()) { onCreate(n); onDismiss() }
                        },
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(cancelRes),
                    contentDescription = "Cancel",
                    modifier = Modifier
                        .size(width = 140.dp, height = buttonHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // disables ripple & pressed shadow
                        ) {
                            onDismiss() // just close the dialog
                        },
                    contentScale = ContentScale.Fit
                )


            }
        }
    }
}







