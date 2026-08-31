package com.memorae.prototype.timeline

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memorae.prototype.map.BackdropGeometry
import com.memorae.prototype.map.BackdropSofteningController
import kotlin.math.abs

@Composable
fun SmokeCrystalTimeline(
    state: TimelineState,
    spec: SmokeCrystalSpec,
    shaderController: SmokeCrystalShaderController,
    softeningController: BackdropSofteningController,
    showLegacyMaterial: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var originInWindow by remember { mutableStateOf(Offset.Zero) }
    var measuredSize by remember { mutableStateOf(IntSize.Zero) }

    val bodyHeightPx = with(density) { spec.bodyHeight.toPx() }
    val radiusPx = with(density) { spec.bodyRadius.toPx() }
    val lensWidthPx = with(density) { spec.activeLensWidth.toPx() }
    val lensHeightPx = with(density) { spec.activeLensHeight.toPx() }
    val lensRadiusPx = with(density) { spec.activeLensRadius.toPx() }
    val yearSpacingPx = with(density) { spec.yearSpacing.toPx() }
    val inertialShiftPx = with(density) { 4.dp.toPx() }
    val lensCenterLocal = measuredSize.width * 0.5f + state.deformation * inertialShiftPx
    val bodyTopLocal = (measuredSize.height - bodyHeightPx) * 0.5f

    SideEffect {
        if (measuredSize.width > 0 && measuredSize.height > 0) {
            softeningController.updateGeometry(
                BackdropGeometry(
                    bodyLeftInWindow = originInWindow.x,
                    bodyTopInWindow = originInWindow.y + bodyTopLocal,
                    bodyWidth = measuredSize.width.toFloat(),
                    bodyHeight = bodyHeightPx,
                    bodyRadius = radiusPx,
                    lensCenterInWindow = originInWindow.x + lensCenterLocal,
                    lensWidth = lensWidthPx,
                    lensHeight = lensHeightPx,
                    lensRadius = lensRadiusPx,
                ),
            )
            shaderController.updateGeometry(
                bodyLeftInWindow = originInWindow.x,
                bodyTopInWindow = originInWindow.y + bodyTopLocal,
                bodyWidth = measuredSize.width.toFloat(),
                bodyHeight = bodyHeightPx,
                bodyRadius = radiusPx,
                lensCenterInWindow = originInWindow.x + lensCenterLocal,
                lensWidth = lensWidthPx,
                lensHeight = lensHeightPx,
                lensRadius = lensRadiusPx,
                velocity = state.deformation,
            )
        }
    }

    Canvas(
        modifier = modifier
            .height(spec.containerHeight)
            .onGloballyPositioned { coordinates ->
                originInWindow = coordinates.positionInWindow()
                measuredSize = coordinates.size
            }
            .timelineGesture(state) { yearSpacingPx }
            .semantics {
                contentDescription = "Memory timeline, ${state.activeYear} selected"
            },
    ) {
        val bodyTop = (size.height - bodyHeightPx) * 0.5f
        val bodyRect = Rect(0f, bodyTop, size.width, bodyTop + bodyHeightPx)
        val lensCenter = size.width * 0.5f + state.deformation * inertialShiftPx
        val lensLeft = lensCenter - lensWidthPx * 0.5f
        val lensTop = (size.height - lensHeightPx) * 0.5f

        if (showLegacyMaterial) {
            drawRoundRect(
                color = Color(0x2B090A09),
                topLeft = Offset(0f, bodyTop + 5.dp.toPx()),
                size = Size(size.width, bodyHeightPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x2DF4F1E7),
                        Color(0x0CF4F1E7),
                        Color(0x26141614),
                    ),
                    startY = bodyTop,
                    endY = bodyTop + bodyHeightPx,
                ),
                topLeft = Offset(bodyRect.left, bodyRect.top),
                size = bodyRect.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x70F2EEE4), Color(0x18E3DFD6), Color(0x6020221F)),
                    startY = bodyTop,
                    endY = bodyTop + bodyHeightPx,
                ),
                topLeft = Offset(bodyRect.left + 0.75.dp.toPx(), bodyRect.top + 0.75.dp.toPx()),
                size = Size(bodyRect.width - 1.5.dp.toPx(), bodyRect.height - 1.5.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
                style = Stroke(width = 1.dp.toPx()),
            )

            drawRoundRect(
                color = Color(0x2E10110F),
                topLeft = Offset(lensLeft, lensTop + 4.dp.toPx()),
                size = Size(lensWidthPx, lensHeightPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(lensRadiusPx),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to Color(0x4CF5F0E5),
                    0.22f to Color(0x1EF4EFE5),
                    0.72f to Color(0x16191B18),
                    1.0f to Color(0x4B111210),
                    startY = lensTop,
                    endY = lensTop + lensHeightPx,
                ),
                topLeft = Offset(lensLeft, lensTop),
                size = Size(lensWidthPx, lensHeightPx),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(lensRadiusPx),
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xA9FFF9EC), Color(0x32E4E0D7), Color(0x7A242623)),
                    startY = lensTop,
                    endY = lensTop + lensHeightPx,
                ),
                topLeft = Offset(lensLeft + 0.7.dp.toPx(), lensTop + 0.7.dp.toPx()),
                size = Size(lensWidthPx - 1.4.dp.toPx(), lensHeightPx - 1.4.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(lensRadiusPx),
                style = Stroke(width = 1.1.dp.toPx()),
            )
        }

        val yearBaseline = bodyTop + bodyHeightPx * 0.43f
        val tickTop = bodyTop + bodyHeightPx * 0.70f
        val activeIndex = state.activeIndex

        state.years.forEachIndexed { index, year ->
            val x = size.width * 0.5f + (index - state.position) * yearSpacingPx
            if (x < -yearSpacingPx || x > size.width + yearSpacingPx) return@forEachIndexed

            val distanceFromLens = abs(x - lensCenter)
            val lensOcclusion = (distanceFromLens / (lensWidthPx * 0.62f)).coerceIn(0f, 1f)
            val alpha = if (index == activeIndex) 0f else 0.48f + lensOcclusion * 0.34f
            drawYearLabel(year.toString(), x, yearBaseline, alpha)

            for (minor in 0 until 5) {
                val tickX = x + minor * (yearSpacingPx / 5f)
                if (tickX <= 0f || tickX >= size.width) continue
                val major = minor == 0
                drawLine(
                    color = Color.White.copy(alpha = if (major) 0.68f else 0.30f),
                    start = Offset(tickX, tickTop),
                    end = Offset(tickX, tickTop + if (major) 13.dp.toPx() else 6.dp.toPx()),
                    strokeWidth = if (major) 1.15.dp.toPx() else 0.7.dp.toPx(),
                )
            }
        }

        drawActiveLabel(
            year = state.activeYear.toString(),
            centerX = lensCenter,
            lensTop = lensTop,
        )

        val indicatorX = lensCenter
        drawLine(
            color = Color(0xCDE4D6BE),
            start = Offset(indicatorX, lensTop + lensHeightPx * 0.73f),
            end = Offset(indicatorX, lensTop + lensHeightPx * 0.88f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.cornerPathEffect(1.dp.toPx()),
        )
        drawCircle(
            color = Color(0xFFE8D8BA),
            radius = 3.dp.toPx(),
            center = Offset(indicatorX, lensTop + lensHeightPx * 0.90f),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawYearLabel(
    text: String,
    x: Float,
    baseline: Float,
    alpha: Float,
) {
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb((alpha * 255).toInt(), 244, 241, 233)
            textSize = 12.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            letterSpacing = 0.04f
        }
        canvas.nativeCanvas.drawText(text, x, baseline, paint)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawActiveLabel(
    year: String,
    centerX: Float,
    lensTop: Float,
) {
    drawIntoCanvas { canvas ->
        val yearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(247, 244, 237)
            textSize = 22.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setShadowLayer(5.dp.toPx(), 0f, 1.dp.toPx(), 0x5A000000)
        }
        val monthPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(218, 238, 234, 225)
            textSize = 11.sp.toPx()
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        canvas.nativeCanvas.drawText(year, centerX, lensTop + 43.dp.toPx(), yearPaint)
        canvas.nativeCanvas.drawText("8月", centerX, lensTop + 63.dp.toPx(), monthPaint)
    }
}
