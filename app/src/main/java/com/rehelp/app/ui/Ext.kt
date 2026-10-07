package com.rehelp.app.ui

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Keeps content clear of the status bar, navigation bar and keyboard
fun View.applySystemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}
