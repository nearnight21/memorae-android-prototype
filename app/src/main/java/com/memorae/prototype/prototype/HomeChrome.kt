package com.memorae.prototype.prototype

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MapAtmosphere(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color(0x7A171916),
                0.18f to Color(0x34312E28),
                0.64f to Color(0x24342F28),
                1.0f to Color(0x58171916),
            ),
        )
    }
}

@Composable
internal fun HomeChrome(amapKeyPresent: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFFD6C7A7), CircleShape),
            )
            Spacer(Modifier.width(10.dp))
            Column {
                BasicText(
                    text = "MEMORAE",
                    style = TextStyle(
                        color = Color(0xFFF1EFE9),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.2.sp,
                        fontFamily = FontFamily.SansSerif,
                    ),
                )
                Spacer(Modifier.height(3.dp))
                BasicText(
                    text = "SHANGHAI · AUG 2024",
                    style = TextStyle(
                        color = Color(0xAFC9C7BF),
                        fontSize = 9.sp,
                        letterSpacing = 1.1.sp,
                        fontFamily = FontFamily.SansSerif,
                    ),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(42.dp)
                .background(Color(0x59252724), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(22.dp)) {
                drawCircle(Color(0x55F3F0E7), style = Stroke(width = 1.dp.toPx()))
                drawLine(
                    color = Color(0xFFD8D2C4),
                    start = Offset(size.width * 0.5f, size.height * 0.18f),
                    end = Offset(size.width * 0.5f, size.height * 0.78f),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = Color(0xFF8D8B84),
                    start = Offset(size.width * 0.5f, size.height * 0.78f),
                    end = Offset(size.width * 0.36f, size.height * 0.56f),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }

        if (!amapKeyPresent) {
            BasicText(
                text = "AMAP KEY REQUIRED FOR LIVE MAP",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 64.dp)
                    .background(Color(0xB85A4936), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                style = TextStyle(
                    color = Color(0xFFE7DAC4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp,
                    fontFamily = FontFamily.SansSerif,
                ),
            )
        }
    }
}
