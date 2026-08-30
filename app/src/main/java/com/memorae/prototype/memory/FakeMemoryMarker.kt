package com.memorae.prototype.memory

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import com.amap.api.maps.AMap
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import kotlin.math.roundToInt

object FakeMemoryMarker {
    private data class Memory(
        val title: String,
        val point: LatLng,
        val topColor: Int,
        val bottomColor: Int,
    )

    private val memories = listOf(
        Memory("Morning platform", LatLng(31.23090, 121.47035), 0xFFBDA58F.toInt(), 0xFF5C6258.toInt()),
        Memory("Riverside walk", LatLng(31.24125, 121.47790), 0xFF9AAEAA.toInt(), 0xFF5A706A.toInt()),
        Memory("Quiet coffee", LatLng(31.22810, 121.47930), 0xFFC3AA89.toInt(), 0xFF6D584C.toInt()),
        Memory("Late light", LatLng(31.23770, 121.48520), 0xFFB9A28D.toInt(), 0xFF665E5B.toInt()),
        Memory("Garden rain", LatLng(31.24420, 121.46510), 0xFF9EAE8D.toInt(), 0xFF536052.toInt()),
    )

    fun install(amap: AMap) {
        memories.forEachIndexed { index, memory ->
            amap.addMarker(
                MarkerOptions()
                    .position(memory.point)
                    .title(memory.title)
                    .anchor(0.5f, 0.82f)
                    .zIndex(4f + index)
                    .icon(BitmapDescriptorFactory.fromBitmap(createMarkerBitmap(memory))),
            )
        }
    }

    private fun createMarkerBitmap(memory: Memory): Bitmap {
        val density = android.content.res.Resources.getSystem().displayMetrics.density
        val width = (54f * density).roundToInt()
        val height = (66f * density).roundToInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val card = RectF(5f * density, 3f * density, 49f * density, 49f * density)
        paint.color = 0xB8ECE8DE.toInt()
        paint.setShadowLayer(6f * density, 0f, 3f * density, 0x66000000)
        canvas.drawRoundRect(card, 13f * density, 13f * density, paint)
        paint.clearShadowLayer()

        val photo = RectF(8f * density, 6f * density, 46f * density, 46f * density)
        val photoPath = Path().apply {
            addRoundRect(photo, 10f * density, 10f * density, Path.Direction.CW)
        }
        canvas.save()
        canvas.clipPath(photoPath)
        paint.shader = LinearGradient(
            photo.left,
            photo.top,
            photo.right,
            photo.bottom,
            memory.topColor,
            memory.bottomColor,
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(photo, paint)
        paint.shader = null
        paint.color = 0x48272A26
        canvas.drawCircle(19f * density, 32f * density, 13f * density, paint)
        paint.color = 0x35EFE7D3
        canvas.drawCircle(39f * density, 13f * density, 12f * density, paint)
        canvas.restore()

        paint.strokeWidth = 1.2f * density
        paint.color = 0xA8EEE9DD.toInt()
        canvas.drawLine(27f * density, 48f * density, 27f * density, 57f * density, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        paint.setShadowLayer(3f * density, 0f, 1f * density, 0x66000000)
        canvas.drawCircle(27f * density, 59f * density, 3.3f * density, paint)

        return bitmap
    }
}
