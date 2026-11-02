package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dedicated Settings glass. Unlike navigation cards, this deliberately has
 * no diagonal Sudoku watermark or scenery crossing interactive controls.
 * Controls are real Compose nodes rendered by [content].
 */
@Composable
fun SettingsSurface(
    modifier: Modifier = Modifier,
    accent: Color = Ink.blue,
    corner: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier.clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xF704193A),
                        Color(0xF507204B),
                        Color(0xF7041A3F),
                    )
                )
            )
            .drawBehind {
                // The owner-supplied panels have a soft directional glass
                // reflection; the text and click targets are separate nodes.
                drawLine(
                    color = Color.White.copy(alpha = .16f),
                    start = Offset(size.width * .055f, 1.dp.toPx()),
                    end = Offset(size.width * .62f, 1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .border(1.dp, accent.copy(alpha = .85f), shape),
    ) {
        content()
    }
}

@Composable
fun SettingsGlyph(
    icon: String,
    tint: Color = Ink.cyan,
    modifier: Modifier = Modifier,
    size: Dp = 37.dp,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier.size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        tint.copy(alpha = .29f),
                        Color(0xE9021A3D),
                    )
                )
            )
            .drawBehind {
                // 'size' is also the outer Dp parameter of SettingsGlyph;
                // explicitly select DrawScope.size for pixel dimensions.
                val canvasSize = this.size
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(tint.copy(alpha = .25f), Color.Transparent),
                        center = Offset(canvasSize.width * .32f, canvasSize.height * .24f),
                        radius = canvasSize.width * .77f,
                    ),
                    center = Offset(canvasSize.width * .32f, canvasSize.height * .24f),
                    radius = canvasSize.width * .77f,
                )
            }
            .border(1.dp, tint.copy(alpha = .9f), shape),
        contentAlignment = Alignment.Center,
    ) {
        BrandIcon(icon, Modifier.size(size * .64f), tint)
    }
}

@Composable
fun SettingsRule(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().height(0.5.dp)
            .background(Ink.blue.copy(alpha = .52f))
    )
}
