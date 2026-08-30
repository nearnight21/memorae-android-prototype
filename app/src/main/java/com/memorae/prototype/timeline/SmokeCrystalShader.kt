package com.memorae.prototype.timeline

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RuntimeShader
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.memorae.prototype.R

@Composable
fun SmokeCrystalShaderLayer(
    controller: SmokeCrystalShaderController,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SmokeCrystalShaderView(context).also(controller::attach)
        },
        update = controller::attach,
    )
}

class SmokeCrystalShaderController(
    private val spec: SmokeCrystalSpec,
) {
    private var target: SmokeCrystalShaderView? = null

    internal fun attach(view: SmokeCrystalShaderView) {
        target = view
    }

    fun updateGeometry(
        bodyLeftInWindow: Float,
        bodyTopInWindow: Float,
        bodyWidth: Float,
        bodyHeight: Float,
        bodyRadius: Float,
        lensCenterInWindow: Float,
        lensWidth: Float,
        lensHeight: Float,
        lensRadius: Float,
        velocity: Float,
    ) {
        val view = target ?: return
        if (view.width <= 0 || view.height <= 0) return

        val location = IntArray(2)
        view.getLocationInWindow(location)
        val bodyLeft = bodyLeftInWindow - location[0]
        val bodyTop = bodyTopInWindow - location[1]
        val lensCenter = lensCenterInWindow - location[0]

        view.updateMaterial(
            bodyLeft = bodyLeft,
            bodyTop = bodyTop,
            bodyWidth = bodyWidth,
            bodyHeight = bodyHeight,
            bodyRadius = bodyRadius,
            lensCenter = lensCenter,
            lensWidth = lensWidth,
            lensHeight = lensHeight,
            lensRadius = lensRadius,
            smokeOpacity = spec.smokeOpacity,
            absorption = spec.absorption,
            edgeStrength = spec.edgeStrength,
            highlightStrength = spec.highlightStrength,
            velocity = velocity.coerceIn(-1f, 1f),
            deformationStrength = spec.deformationStrength,
        )
    }

    fun detach() {
        target = null
    }
}

internal class SmokeCrystalShaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var runtimeShader: RuntimeShader? = null
    private val materialPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var geometryReady = false

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        runCatching {
            resources.openRawResource(R.raw.smoke_crystal)
                .bufferedReader()
                .use { RuntimeShader(it.readText()) }
        }.onSuccess { shader ->
            runtimeShader = shader
            materialPaint.shader = shader
        }.onFailure { error ->
            Log.e(LogTag, "RuntimeShader compilation failed", error)
        }
    }

    fun updateMaterial(
        bodyLeft: Float,
        bodyTop: Float,
        bodyWidth: Float,
        bodyHeight: Float,
        bodyRadius: Float,
        lensCenter: Float,
        lensWidth: Float,
        lensHeight: Float,
        lensRadius: Float,
        smokeOpacity: Float,
        absorption: Float,
        edgeStrength: Float,
        highlightStrength: Float,
        velocity: Float,
        deformationStrength: Float,
    ) {
        val shader = runtimeShader ?: return
        shader.setFloatUniform("bodyRect", bodyLeft, bodyTop, bodyWidth, bodyHeight)
        shader.setFloatUniform("bodyRadius", bodyRadius)
        shader.setFloatUniform("lensCenterX", lensCenter)
        shader.setFloatUniform("lensWidth", lensWidth)
        shader.setFloatUniform("lensHeight", lensHeight)
        shader.setFloatUniform("lensRadius", lensRadius)
        shader.setFloatUniform("smokeOpacity", smokeOpacity)
        shader.setFloatUniform("absorption", absorption)
        shader.setFloatUniform("edgeStrength", edgeStrength)
        shader.setFloatUniform("highlightStrength", highlightStrength)
        shader.setFloatUniform("velocity", velocity)
        shader.setFloatUniform("deformationStrength", deformationStrength)
        geometryReady = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val shader = runtimeShader ?: return
        if (!geometryReady || width <= 0 || height <= 0) return
        shader.setFloatUniform("resolution", width.toFloat(), height.toFloat())
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), materialPaint)
    }

    private companion object {
        const val LogTag = "MemoraeSmokeCrystal"
    }
}
