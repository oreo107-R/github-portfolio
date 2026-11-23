package com.app.dinolearnerfree

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "first_run_prefs"
private const val KEY_NOTICE_SHOWN = "first_launch_notice_shown"

object FirstRunPrefs {
    fun shouldShowNotice(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default true = show on very first launch
        return !sp.getBoolean(KEY_NOTICE_SHOWN, false)
    }

    fun markNoticeShown(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit { putBoolean(KEY_NOTICE_SHOWN, true) }
    }
}
