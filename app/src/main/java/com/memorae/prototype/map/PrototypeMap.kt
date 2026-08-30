package com.memorae.prototype.map

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.memorae.prototype.memory.FakeMemoryMarker

@Composable
fun PrototypeMap(
    restoredState: Bundle?,
    onMapCreated: (TextureMapView) -> Unit,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapsInitializer.loadWorldVectorMap(true)

            TextureMapView(context).apply {
                onCreate(restoredState)
                configurePrototypeMap(map)
                onMapCreated(this)
            }
        },
    )
}

private fun configurePrototypeMap(amap: AMap) {
    amap.uiSettings.apply {
        isZoomControlsEnabled = false
        isScaleControlsEnabled = false
        isCompassEnabled = false
        isMyLocationButtonEnabled = false
        isRotateGesturesEnabled = true
        isTiltGesturesEnabled = true
        isScrollGesturesEnabled = true
        isZoomGesturesEnabled = true
    }
    amap.mapType = AMap.MAP_TYPE_SATELLITE
    amap.showMapText(false)
    amap.showBuildings(false)
    amap.setRoadArrowEnable(false)
    amap.isTrafficEnabled = false
    amap.moveCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                LatLng(31.23540, 121.47475),
                14.4f,
                28f,
                345f,
            ),
        ),
    )

    FakeMemoryMarker.install(amap)
}
