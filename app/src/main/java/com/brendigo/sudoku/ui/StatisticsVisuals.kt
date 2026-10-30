package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Statistics-only glass surface.
 *
 * A deliberate departure from NeonPanel: the reference has subtle color
 * gradients and thin precise borders, not the full-size diagonal-grid and
 * mountain silhouettes seen on the generic navigation cards.
 */
@Composable
fun StatisticsSurface(
    modifier: Modifier = Modifier,
    accent: Color = Ink.blue,
    background: List<Color> = listOf(
        Color(0xED061A42), Color(0xF2041B45), Color(0xF9041434)
    ),
    corner: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    Box(
        modifier.clip(shape)
            .background(Brush.horizontalGradient(background))
            .border(1.dp, accent.copy(alpha = .83f), shape),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = .16f), Color.Transparent),
                    center = Offset(w * .80f, h * .04f), radius = w * .85f,
                ),
                radius = w * .85f, center = Offset(w * .80f, h * .04f),
            )
        }
        content()
    }
}

/** Dedicated backgrounds for the four statistic metrics. */
fun metricCardColors(key: String): List<Color> = when (key) {
    "played" -> listOf(Color(0xF104303A), Color(0xF2034550), Color(0xFB02263A))
    "best" -> listOf(Color(0xF21C174A), Color(0xF4201B57), Color(0xFB13133B))
    "total" -> listOf(Color(0xF1042653), Color(0xF3053674), Color(0xFA051F4A))
    "streak" -> listOf(Color(0xF331302C), Color(0xF53D332B), Color(0xFB302A28))
    else -> listOf(Color(0xF4051B41), Color(0xF4052146))
}

/** Background lines for the graphical chart, not data or a screenshot. */
@Composable
fun StatisticsChartGrid(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        for (tick in 0..4) {
            val y = h * (tick / 4f)
            drawLine(
                color = Color(0xFF3977B8).copy(alpha = if (tick == 4) .58f else .20f),
                start = Offset(0f, y), end = Offset(w, y),
                strokeWidth = if (tick == 4) 1.7f else 1f,
            )
        }
        drawLine(
            color = Color(0xFF3977B8).copy(alpha = .36f),
            start = Offset(0f, 0f), end = Offset(0f, h),
            strokeWidth = 1.7f,
        )
    }
}
