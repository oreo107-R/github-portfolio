package com.app.dinolearnerfree

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

/**
 * One-time parental info dialog for child-directed, ad-supported app.
 * Shows a short message + a link to the Privacy Policy.
 */
@Composable
fun FirstLaunchNoticeDialog(
    onContinue: () -> Unit,
    privacyPolicyUrl: String = "https://www.levelupwithoneup.com/home/dino-learner/privacy-policy"
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { /* block outside dismiss on first run */ },
        title = { Text("Welcome, Parents & Guardians") },
        text = {
            Text(
                "DinoLearner Free is designed for children.\n\n" +
                        "This app shows ads from Google AdMob, a certified ad network in the " +
                        "Google Play Families Ads Program.\n\n" +
                        "• Ads are non-personalized (no tracking)\n" +
                        "• Ads are filtered to show only G-rated, child-appropriate content\n\n" +
                        "Please review our Privacy Policy before your child continues."
            )
        }
        ,
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, privacyPolicyUrl.toUri())
                    context.startActivity(intent)
                }
            ) {
                Text("View Privacy Policy")
            }
        }
    )
}
