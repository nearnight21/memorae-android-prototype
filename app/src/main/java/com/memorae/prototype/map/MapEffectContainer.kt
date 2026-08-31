package com.memorae.prototype.map

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.amap.api.maps.TextureMapView

enum class MapEffectMode(val intentValue: String) {
    Identity("identity"),
    HalfScreenColor("half"),
    LocalDistortion("distortion"),
    ;

    companion object {
        fun fromIntent(value: String?): MapEffectMode =
            entries.firstOrNull { it.intentValue == value } ?: Identity
    }
}

/**
 * EXPERIMENT: verifies whether a RuntimeShader RenderEffect attached to a
 * parent ViewGroup receives the live pixels produced by AMap's TextureMapView.
 */
class MapEffectContainer(
    context: Context,
    private val mode: MapEffectMode,
) : FrameLayout(context) {
    private val shader = RuntimeShader(mode.shaderSource())

    init {
        isClickable = false
        isFocusable = false
        if (mode == MapEffectMode.Identity) {
            applyEffect()
        }
    }

    fun attachMap(mapView: TextureMapView) {
        addView(
            mapView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (width > 0 && height > 0 && mode != MapEffectMode.Identity) {
            shader.setFloatUniform(ResolutionUniform, width.toFloat(), height.toFloat())
            applyEffect()
        }
        Log.i(LogTag, "Size=${width}x$height mode=${mode.intentValue}")
    }

    private fun applyEffect() {
        setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, MapInputName))
        Log.i(LogTag, "Applied mode=${mode.intentValue}")
    }

    private companion object {
        const val LogTag = "MapRenderEffectTest"
        const val MapInputName = "mapContent"
        const val ResolutionUniform = "resolution"
    }
}

private fun MapEffectMode.shaderSource(): String = when (this) {
    MapEffectMode.Identity -> """
        uniform shader mapContent;

        half4 main(float2 coord) {
            return mapContent.eval(coord);
        }
    """.trimIndent()

    MapEffectMode.HalfScreenColor -> """
        uniform shader mapContent;
        uniform float2 resolution;

        half4 main(float2 coord) {
            half4 source = mapContent.eval(coord);
            if (coord.x < resolution.x * 0.5) {
                return source;
            }
            return half4(1.0 - source.rgb, source.a);
        }
    """.trimIndent()

    MapEffectMode.LocalDistortion -> """
        uniform shader mapContent;
        uniform float2 resolution;

        half4 main(float2 coord) {
            float2 center = resolution * 0.5;
            float radius = min(resolution.x, resolution.y) * 0.22;
            float distanceToCenter = length(coord - center);
            float inside = 1.0 - smoothstep(radius - 8.0, radius, distanceToCenter);
            float2 sampleCoord = coord + float2(16.0 * inside, 0.0);
            return mapContent.eval(sampleCoord);
        }
    """.trimIndent()
}
