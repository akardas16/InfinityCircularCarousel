package com.akardas16.infinitycircularcarousel

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun gradientBlue(): Brush = Brush.verticalGradient(
    listOf(
        Color(0xFF0099FF),
        Color(0xFF00C6FF)
    )
)

fun gradientUnselect(): Brush = Brush.horizontalGradient(
    listOf(
        Color(0xFF171719),
        Color(0xFF171719)
    )
)