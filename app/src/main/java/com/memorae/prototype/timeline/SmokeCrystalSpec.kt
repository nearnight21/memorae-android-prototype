package com.memorae.prototype.timeline

import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SmokeCrystalSpec(
    val horizontalInset: Dp,
    val bottomSpacing: Dp,
    val containerHeight: Dp,
    val bodyHeight: Dp,
    val bodyRadius: Dp,
    val activeLensWidth: Dp,
    val activeLensHeight: Dp,
    val activeLensRadius: Dp,
    val yearSpacing: Dp,
    val smokeOpacity: Float,
    val blurStrengthPx: Float,
    val absorption: Float,
    val edgeStrength: Float,
    val highlightStrength: Float,
    val refractionStrengthPx: Float,
    val deformationStrength: Float,
    val springStiffness: Float,
    val springDamping: Float,
) {
    companion object {
        val Experimental = SmokeCrystalSpec(
            horizontalInset = 14.dp,
            bottomSpacing = 18.dp,
            containerHeight = 118.dp,
            bodyHeight = 92.dp,
            bodyRadius = 46.dp,
            activeLensWidth = 112.dp,
            activeLensHeight = 112.dp,
            activeLensRadius = 32.dp,
            yearSpacing = 86.dp,
            smokeOpacity = 0.24f,
            blurStrengthPx = 4.6f,
            absorption = 0.22f,
            edgeStrength = 0.82f,
            highlightStrength = 0.72f,
            refractionStrengthPx = 3.2f,
            deformationStrength = 0.035f,
            springStiffness = Spring.StiffnessMedium,
            springDamping = 0.88f,
        )
    }
}
