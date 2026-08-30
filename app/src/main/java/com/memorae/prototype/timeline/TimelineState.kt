package com.memorae.prototype.timeline

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
class TimelineState internal constructor(
    initialIndex: Int,
    val years: List<Int>,
    private val spec: SmokeCrystalSpec,
) {
    private val positionState = mutableFloatStateOf(initialIndex.toFloat())
    private val deformationState = mutableFloatStateOf(0f)

    val position: Float get() = positionState.floatValue
    val deformation: Float get() = deformationState.floatValue
    val activeIndex: Int get() = position.roundToInt().coerceIn(years.indices)
    val activeYear: Int get() = years[activeIndex]

    fun beginDrag() = Unit

    fun dragBy(deltaX: Float, spacingPx: Float, velocityX: Float) {
        if (spacingPx <= 0f) return
        val next = (positionState.floatValue - deltaX / spacingPx)
            .coerceIn(0f, years.lastIndex.toFloat())
        positionState.floatValue = next
        deformationState.floatValue = (velocityX / 4200f).coerceIn(-1f, 1f)
    }

    suspend fun settle(velocityX: Float, spacingPx: Float) = coroutineScope {
        val positionAnimation = Animatable(positionState.floatValue)
        val deformationAnimation = Animatable(deformationState.floatValue)
        val timelineVelocity = if (spacingPx > 0f) -velocityX / spacingPx else 0f
        val target = (positionState.floatValue + timelineVelocity * VelocityProjectionSeconds)
            .roundToInt()
            .coerceIn(years.indices)
            .toFloat()

        positionAnimation.updateBounds(0f, years.lastIndex.toFloat())

        launch {
            positionAnimation.animateTo(
                targetValue = target,
                animationSpec = spring(
                    dampingRatio = spec.springDamping,
                    stiffness = spec.springStiffness,
                ),
                initialVelocity = timelineVelocity * 0.25f,
            ) { positionState.floatValue = value }
        }
        launch {
            deformationAnimation.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = SpringStiffnessForMaterial,
                ),
            ) { deformationState.floatValue = value }
        }
    }

    private companion object {
        const val SpringStiffnessForMaterial = 520f
        const val VelocityProjectionSeconds = 0.12f
    }
}

@Composable
fun rememberTimelineState(
    initialYear: Int,
    spec: SmokeCrystalSpec,
    years: List<Int> = (2020..2026).toList(),
): TimelineState {
    val initialIndex = years.indexOf(initialYear).coerceAtLeast(0)
    return remember(years, initialIndex, spec) {
        TimelineState(initialIndex, years, spec)
    }
}
