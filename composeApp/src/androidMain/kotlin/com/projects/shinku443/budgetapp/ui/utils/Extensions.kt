package com.projects.shinku443.budgetapp.ui.utils

import androidx.compose.ui.graphics.Color


fun Color.toLong(): Long {
    return (alpha * 255).toInt().toLong().shl(24) or
            (red * 255).toInt().toLong().shl(16) or
            (green * 255).toInt().toLong().shl(8) or
            (blue * 255).toInt().toLong()
}

fun Long.toColor(): Color {
    return Color(this.toInt())
}
