package com.memorae.prototype.map

import android.content.Context
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.amap.api.maps.TextureMapView

enum class MapEffectMode(
    val intentValue: String,
    val softeningSpec: BackdropSofteningSpec? = null,
    val isPassOneDiagnostic: Boolean = false,
) {
    None("none", isPassOneDiagnostic = true),
    Identity("identity", isPassOneDiagnostic = true),
    HalfScreenColor("half"),
    LocalDistortion("distortion"),
    Raw("raw", BackdropSofteningSpec.Raw, isPassOneDiagnostic = true),
    Soft("soft", BackdropSofteningSpec.Soft, isPassOneDiagnostic = true),
    Medium("medium", BackdropSofteningSpec.Medium, isPassOneDiagnostic = true),
    Strong("strong", BackdropSofteningSpec.Strong, isPassOneDiagnostic = true),
    ;

    val isVisualCandidate: Boolean
        get() = softeningSpec != null

    companion object {
        val visualCandidates = listOf(Raw, Soft, Medium, Strong)

        fun fromIntent(value: String?): MapEffectMode =
            entries.firstOrNull { it.intentValue == value } ?: Raw
    }
}

data class BackdropGeometry(
    val bodyLeftInWindow: Float,
    val bodyTopInWindow: Float,
    val bodyWidth: Float,
    val bodyHeight: Float,
    val bodyRadius: Float,
    val lensCenterInWindow: Float,
    val lensWidth: Float,
    val lensHeight: Float,
    val lensRadius: Float,
)

class BackdropSofteningController {
    private var target: MapEffectContainer? = null
    private var geometry: BackdropGeometry? = null

    internal fun attach(view: MapEffectContainer) {
        target = view
        geometry?.let(view::updateGeometry)
    }

    internal fun detach(view: MapEffectContainer) {
        if (target === view) target = null
    }

    fun updateGeometry(geometry: BackdropGeometry) {
        this.geometry = geometry
        target?.updateGeometry(geometry)
    }
}

/**
 * Applies an AGSL RenderEffect to the parent of AMap's TextureMapView. In the
 * Pass 1 modes the child shader input is the live map itself.
 */
class MapEffectContainer(
    context: Context,
    initialMode: MapEffectMode,
    private val controller: BackdropSofteningController,
) : FrameLayout(context) {
    private var mode = initialMode
    private var shader: RuntimeShader? = null
    private var geometry: BackdropGeometry? = null
    private var effectApplied = false

    init {
        isClickable = false
        isFocusable = false
        controller.attach(this)
    }

    fun attachMap(mapView: TextureMapView) {
        addView(
            mapView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        configureEffect(forceShaderRebuild = true)
    }

    fun updateMode(newMode: MapEffectMode) {
        if (mode == newMode) return
        val canReuseBackdropShader = mode.softeningSpec != null && newMode.softeningSpec != null
        mode = newMode
        effectApplied = false
        configureEffect(forceShaderRebuild = !canReuseBackdropShader)
    }

    internal fun updateGeometry(windowGeometry: BackdropGeometry) {
        val location = IntArray(2)
        getLocationInWindow(location)
        geometry = windowGeometry.copy(
            bodyLeftInWindow = windowGeometry.bodyLeftInWindow - location[0],
            bodyTopInWindow = windowGeometry.bodyTopInWindow - location[1],
            lensCenterInWindow = windowGeometry.lensCenterInWindow - location[0],
        )
        updateUniformsAndApply()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateUniformsAndApply()
        Log.i(LogTag, "Size=${width}x$height mode=${mode.intentValue}")
    }

    override fun onDetachedFromWindow() {
        controller.detach(this)
        super.onDetachedFromWindow()
    }

    private fun configureEffect(forceShaderRebuild: Boolean) {
        if (mode == MapEffectMode.None) {
            setRenderEffect(null)
            shader = null
            effectApplied = false
            Log.i(LogTag, "Disabled mode=${mode.intentValue}")
            return
        }

        if (forceShaderRebuild || shader == null) {
            shader = RuntimeShader(mode.shaderSource())
            effectApplied = false
        }
        updateUniformsAndApply()
    }

    private fun updateUniformsAndApply() {
        val activeShader = shader ?: return
        if (width <= 0 || height <= 0) return

        when (mode) {
            MapEffectMode.None -> return
            MapEffectMode.Identity -> Unit
            MapEffectMode.HalfScreenColor,
            MapEffectMode.LocalDistortion,
            -> activeShader.setFloatUniform(ResolutionUniform, width.toFloat(), height.toFloat())

            MapEffectMode.Raw,
            MapEffectMode.Soft,
            MapEffectMode.Medium,
            MapEffectMode.Strong,
            -> {
                val currentGeometry = geometry ?: return
                val spec = checkNotNull(mode.softeningSpec)
                activeShader.setFloatUniform(ResolutionUniform, width.toFloat(), height.toFloat())
                activeShader.setFloatUniform(
                    BodyRectUniform,
                    currentGeometry.bodyLeftInWindow,
                    currentGeometry.bodyTopInWindow,
                    currentGeometry.bodyWidth,
                    currentGeometry.bodyHeight,
                )
                activeShader.setFloatUniform(BodyRadiusUniform, currentGeometry.bodyRadius)
                activeShader.setFloatUniform(LensCenterXUniform, currentGeometry.lensCenterInWindow)
                activeShader.setFloatUniform(LensWidthUniform, currentGeometry.lensWidth)
                activeShader.setFloatUniform(LensHeightUniform, currentGeometry.lensHeight)
                activeShader.setFloatUniform(LensRadiusUniform, currentGeometry.lensRadius)
                activeShader.setFloatUniform(SampleRadiusUniform, spec.sampleRadiusPx)
                activeShader.setFloatUniform(SampleStrengthUniform, spec.sampleStrength)
                activeShader.setFloatUniform(KernelModeUniform, spec.kernel.shaderMode)
            }
        }

        if (!effectApplied) {
            setRenderEffect(RenderEffect.createRuntimeShaderEffect(activeShader, MapInputName))
            effectApplied = true
        } else {
            invalidate()
        }
        Log.i(LogTag, "Applied mode=${mode.intentValue}")
    }

    private companion object {
        const val LogTag = "MapRenderEffectTest"
        const val MapInputName = "mapContent"
        const val ResolutionUniform = "resolution"
        const val BodyRectUniform = "bodyRect"
        const val BodyRadiusUniform = "bodyRadius"
        const val LensCenterXUniform = "lensCenterX"
        const val LensWidthUniform = "lensWidth"
        const val LensHeightUniform = "lensHeight"
        const val LensRadiusUniform = "lensRadius"
        const val SampleRadiusUniform = "sampleRadiusPx"
        const val SampleStrengthUniform = "sampleStrength"
        const val KernelModeUniform = "kernelMode"
    }
}

private fun MapEffectMode.shaderSource(): String = when (this) {
    MapEffectMode.None -> error("No shader is created for the no-effect mode")

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

    MapEffectMode.Raw,
    MapEffectMode.Soft,
    MapEffectMode.Medium,
    MapEffectMode.Strong,
    -> BackdropSofteningShader
}

private val BackdropSofteningShader = """
    uniform shader mapContent;
    uniform float2 resolution;
    uniform float4 bodyRect;
    uniform float bodyRadius;
    uniform float lensCenterX;
    uniform float lensWidth;
    uniform float lensHeight;
    uniform float lensRadius;
    uniform float sampleRadiusPx;
    uniform float sampleStrength;
    uniform float kernelMode;

    float roundedRectSdf(float2 point, float2 halfSize, float radius) {
        float2 q = abs(point) - halfSize + float2(radius);
        return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - radius;
    }

    half4 sampleMap(float2 coord) {
        float2 safeCoord = clamp(coord, float2(0.5), resolution - float2(0.5));
        return mapContent.eval(safeCoord);
    }

    half4 fiveSample(float2 coord, float radius) {
        half4 result = sampleMap(coord) * 0.40;
        result += sampleMap(coord + float2(radius, 0.0)) * 0.15;
        result += sampleMap(coord + float2(-radius, 0.0)) * 0.15;
        result += sampleMap(coord + float2(0.0, radius)) * 0.15;
        result += sampleMap(coord + float2(0.0, -radius)) * 0.15;
        return result;
    }

    half4 nineSample(float2 coord, float radius) {
        float diagonal = radius * 0.70710678;
        half4 result = sampleMap(coord) * 0.25;
        result += sampleMap(coord + float2(radius, 0.0)) * 0.125;
        result += sampleMap(coord + float2(-radius, 0.0)) * 0.125;
        result += sampleMap(coord + float2(0.0, radius)) * 0.125;
        result += sampleMap(coord + float2(0.0, -radius)) * 0.125;
        result += sampleMap(coord + float2(diagonal, diagonal)) * 0.0625;
        result += sampleMap(coord + float2(-diagonal, diagonal)) * 0.0625;
        result += sampleMap(coord + float2(diagonal, -diagonal)) * 0.0625;
        result += sampleMap(coord + float2(-diagonal, -diagonal)) * 0.0625;
        return result;
    }

    half4 thirteenSample(float2 coord, float radius) {
        float diagonal = radius * 0.70710678;
        float farRadius = radius * 1.65;
        half4 result = sampleMap(coord) * 0.20;
        result += sampleMap(coord + float2(radius, 0.0)) * 0.10;
        result += sampleMap(coord + float2(-radius, 0.0)) * 0.10;
        result += sampleMap(coord + float2(0.0, radius)) * 0.10;
        result += sampleMap(coord + float2(0.0, -radius)) * 0.10;
        result += sampleMap(coord + float2(diagonal, diagonal)) * 0.0625;
        result += sampleMap(coord + float2(-diagonal, diagonal)) * 0.0625;
        result += sampleMap(coord + float2(diagonal, -diagonal)) * 0.0625;
        result += sampleMap(coord + float2(-diagonal, -diagonal)) * 0.0625;
        result += sampleMap(coord + float2(farRadius, 0.0)) * 0.0375;
        result += sampleMap(coord + float2(-farRadius, 0.0)) * 0.0375;
        result += sampleMap(coord + float2(0.0, farRadius)) * 0.0375;
        result += sampleMap(coord + float2(0.0, -farRadius)) * 0.0375;
        return result;
    }

    half4 main(float2 coord) {
        half4 source = sampleMap(coord);

        float2 bodyCenter = bodyRect.xy + bodyRect.zw * 0.5;
        float bodyDistance = roundedRectSdf(coord - bodyCenter, bodyRect.zw * 0.5, bodyRadius);
        float lensDistance = roundedRectSdf(
            coord - float2(lensCenterX, bodyCenter.y),
            float2(lensWidth * 0.5, lensHeight * 0.5),
            lensRadius
        );
        float bodyMask = 1.0 - smoothstep(-1.5, 1.5, bodyDistance);
        float lensMask = 1.0 - smoothstep(-1.5, 1.5, lensDistance);
        float timelineMask = max(bodyMask, lensMask);

        if (timelineMask <= 0.0 || kernelMode < 0.5 || sampleStrength <= 0.0) {
            return source;
        }

        half4 softened;
        if (kernelMode < 1.5) {
            softened = fiveSample(coord, sampleRadiusPx);
        } else if (kernelMode < 2.5) {
            softened = nineSample(coord, sampleRadiusPx);
        } else {
            softened = thirteenSample(coord, sampleRadiusPx);
        }
        return mix(source, softened, half(sampleStrength * timelineMask));
    }
""".trimIndent()
