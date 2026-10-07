package com.rehelp.app.ui

import android.graphics.Color

// One place for status colors. The status word is always shown too, so color is never the only cue.
object StatusColors {
    fun of(status: String): Int = Color.parseColor(
        when (status) {
            "Available" -> "#2E7D32"
            "Reserved" -> "#B26A00"
            "Picked Up" -> "#1565C0"
            "Cancelled" -> "#B3261E"
            else -> "#5F6368"
        }
    )
}
