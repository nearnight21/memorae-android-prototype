package com.memorae.prototype.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.TextureMapView
import com.memorae.prototype.BuildConfig
import com.memorae.prototype.map.MapEffectMode
import com.memorae.prototype.map.PrototypeMap
import com.memorae.prototype.timeline.SmokeCrystalShaderController
import com.memorae.prototype.timeline.SmokeCrystalShaderLayer
import com.memorae.prototype.timeline.SmokeCrystalSpec
import com.memorae.prototype.timeline.SmokeCrystalTimeline
import com.memorae.prototype.timeline.rememberTimelineState

/**
 * PROTOTYPE: answers whether a live AMap backdrop can support a convincing
 * smoked optical-glass timeline. It intentionally contains no product data.
 */
class HomePrototypeActivity : ComponentActivity() {
    private var textureMapView: TextureMapView? = null
    private var restoredMapState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoredMapState = savedInstanceState

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContent {
            var privacyAccepted by rememberSaveable { mutableStateOf(false) }
            val mapEffectMode = remember {
                MapEffectMode.fromIntent(intent.getStringExtra(MapEffectModeExtra))
            }

            if (privacyAccepted) {
                HomePrototype(
                    restoredMapState = restoredMapState,
                    mapEffectMode = mapEffectMode,
                    onMapCreated = { mapView ->
                        textureMapView = mapView
                        if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                            mapView.onResume()
                        }
                    },
                )
            } else {
                PrivacyGate(
                    onAccept = {
                        MapsInitializer.updatePrivacyShow(applicationContext, true, true)
                        MapsInitializer.updatePrivacyAgree(applicationContext, true)
                        privacyAccepted = true
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        textureMapView?.onResume()
    }

    override fun onPause() {
        textureMapView?.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        textureMapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        textureMapView?.onDestroy()
        textureMapView = null
        super.onDestroy()
    }

    private companion object {
        const val MapEffectModeExtra = "map_effect"
    }
}

@Composable
private fun HomePrototype(
    restoredMapState: Bundle?,
    mapEffectMode: MapEffectMode,
    onMapCreated: (TextureMapView) -> Unit,
) {
    val spec = remember { SmokeCrystalSpec.Experimental }
    val shaderController = remember { SmokeCrystalShaderController(spec) }
    val timelineState = rememberTimelineState(initialYear = 2024, spec = spec)

    DisposableEffect(shaderController) {
        onDispose { shaderController.detach() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF252824)),
    ) {
        PrototypeMap(
            restoredState = restoredMapState,
            effectMode = mapEffectMode,
            onMapCreated = onMapCreated,
            modifier = Modifier.fillMaxSize(),
        )

        MapAtmosphere(modifier = Modifier.fillMaxSize())
        SmokeCrystalShaderLayer(
            controller = shaderController,
            modifier = Modifier.fillMaxSize(),
        )
        HomeChrome(amapKeyPresent = BuildConfig.AMAP_KEY_PRESENT)

        SmokeCrystalTimeline(
            state = timelineState,
            spec = spec,
            shaderController = shaderController,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = spec.horizontalInset)
                .padding(bottom = spec.bottomSpacing),
        )
    }
}

@Composable
private fun PrivacyGate(onAccept: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF20221F))
            .systemBarsPadding()
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "MEMORAE",
                style = TextStyle(
                    color = Color(0xFFF0EEE8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 3.sp,
                    fontFamily = FontFamily.SansSerif,
                ),
            )
            Spacer(Modifier.height(28.dp))
            BasicText(
                text = "地图用于验证 Smoke Crystal 在真实道路、水域和建筑背景上的视觉表现。继续即表示同意本次原型加载高德地图服务。",
                style = TextStyle(
                    color = Color(0xFFC5C3BC),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                ),
            )
            Spacer(Modifier.height(34.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE1DED3), RoundedCornerShape(22.dp))
                    .clickable(onClick = onAccept)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "同意并进入视觉原型",
                    style = TextStyle(
                        color = Color(0xFF2A2B28),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                    ),
                )
            }
        }
    }
}
