package com.app.dinolearnerfree

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import com.google.android.gms.ads.*               // AdError, LoadAdError, etc.
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.ads.mediation.admob.AdMobAdapter // ✅ correct package
import android.util.Log
import android.content.pm.ApplicationInfo



// Find the hosting Activity from a Compose/Context
fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

object Ads {
    private const val TAG = "Ads"
    private const val TEST_INTERSTITIAL =
        "ca-app-pub-3940256099942544/1033173712"      // Google test ID
    private const val PROD_INTERSTITIAL =
        "ca-app-pub-7395663189084620/4053421267"      // your real unit id

    // Use test in debug builds, real in release
    private fun interstitialId(context: Context): String =
        if ((context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            TEST_INTERSTITIAL
        else
            PROD_INTERSTITIAL


    private var interstitial: InterstitialAd? = null
    private var lastShownAt = 0L
    private const val MIN_GAP_MS = 110_000L
    private var isLoading = false

    private fun npaRequest(): AdRequest {
        val extras = Bundle().apply { putString("npa", "1") } // non-personalized
        return AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    }

    fun preload(context: Context) {
        if (interstitial != null || isLoading) return
        isLoading = true
        InterstitialAd.load(
            context.applicationContext,
            interstitialId(context),
            npaRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    isLoading = false                // 👈 add this
                    Log.d(TAG, "Loaded interstitial for id=${interstitialId(context)}")
                }

                override fun onAdFailedToLoad(err: LoadAdError) {
                    interstitial = null
                    isLoading = false                // 👈 and this
                    Log.d(TAG, "Load failed: code=${err.code}, msg=${err.message}")
                }
            }
        )
    }

    private var showInProgress = false

    fun showThen(
        activity: Activity,
        pauseAudio: () -> Unit = {},
        resumeAudio: () -> Unit = {},
        after: () -> Unit
    ) {
        val now = System.currentTimeMillis()
        val canShow = interstitial != null &&
                now - lastShownAt >= MIN_GAP_MS &&
                !showInProgress


        if (!canShow) {
            Log.d(TAG, "Not showing. interstitial=${interstitial != null}, inProgress=$showInProgress, cooldownMs=${now - lastShownAt}/$MIN_GAP_MS")
            after()
            return
        }

        val ad = interstitial ?: run { after(); return }
        showInProgress = true

        interstitial?.apply {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Shown")
                    pauseAudio()
                    lastShownAt = System.currentTimeMillis()
                    interstitial = null
                }
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Dismissed")
                    resumeAudio()
                    showInProgress = false
                    after()
                    preload(activity.applicationContext)
                }
                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.d(TAG, "Show failed: code=${error.code}, msg=${error.message}")
                    showInProgress = false
                    interstitial = null
                    after()
                    preload(activity.applicationContext)
                }
            }
            Log.d(TAG, "Calling show()")
            ad.show(activity)
        }

    }
}
