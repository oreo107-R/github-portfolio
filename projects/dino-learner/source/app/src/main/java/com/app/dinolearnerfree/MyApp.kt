package com.app.dinolearnerfree

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Mark this app as child-directed (COPPA/Google Play Families compliance)
        val requestConfig = RequestConfiguration.Builder()
            .setTagForChildDirectedTreatment(
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
            )
            .build()

        MobileAds.setRequestConfiguration(requestConfig)
        MobileAds.initialize(this)
    }
}
