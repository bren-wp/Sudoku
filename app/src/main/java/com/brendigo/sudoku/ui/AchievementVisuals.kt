package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Decorative background only. All text, achievements, progress, points,
 * buttons, event handling and accessibility remain genuine Compose nodes.
 * Nothing from the user's reference screenshot is reproduced as a fake UI.
 */
@Composable
fun AchievementSurface(
    title: String,
    tint: Color,
    completed: Boolean,
    modifier: Modifier = Modifier,
    radius: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val warm = title == "Prva pobjeda" || title == "Bez greške"
    val lunar = title == "Sedam dana zaredom" || title == "Vrući niz"
    val brain = title == "Majstor logike"
    val muted = !completed && !lunar && !brain && !warm
    val frame = RoundedCornerShape(radius)
    val base = when {
        warm -> listOf(Color(0xFF111A2B), Color(0xFF4F3529), Color(0xFF33251E))
        lunar -> listOf(Color(0xFF061B45), Color(0xFF052E68), Color(0xFF02163C))
        brain -> listOf(Color(0xFF031E30), Color(0xFF06495A), Color(0xFF022436))
        else -> listOf(Color(0xFF071832), Color(0xFF0A1F42), Color(0xFF071831))
    }
    val edge = when {
        muted -> Color(0xFF49699E)
        warm -> Ink.gold
        brain -> Ink.teal
        else -> Ink.blue
    }
    Box(
        modifier.clip(frame)
            .background(Brush.horizontalGradient(base))
            .border(1.dp, edge.copy(alpha = if (muted) .67f else .95f), frame)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            if (w < 2f || h < 2f) return@Canvas
            // The original composition uses a little illuminated sky in the
            // upper-right and an almost black foreground for descriptions.
            val highlight = when {
                warm -> Color(0xFFFFC55D)
                lunar -> Ink.blue
                brain -> Ink.cyan
                else -> Color(0xFF4479D5)
            }
            drawCircle(
                Brush.radialGradient(
                    listOf(highlight.copy(alpha = if (muted) .13f else .37f),
                        Color.Transparent),
                    center = Offset(w * .81f, h * .13f),
                    radius = w * .65f,
                ),
                radius = w * .65f, center = Offset(w * .81f, h * .13f),
            )
            if (warm || lunar) {
                val moonAt = Offset(w * .75f, h * .20f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(highlight.copy(alpha = .32f), Color.Transparent),
                        center = moonAt, radius = w * .21f,
                    ), radius = w * .21f, center = moonAt,
                )
                drawCircle(
                    if (warm) Color(0xFFFFD26A).copy(alpha = .89f)
                    else Color(0xFF74C3FF).copy(alpha = .83f),
                    radius = w * .068f, center = moonAt,
                )
                // Four mountain silhouettes with deterministic heights.
                repeat(3) { ridge ->
                    val yBase = h * (.52f + ridge * .10f)
                    val p = Path().apply {
                        moveTo(0f, h)
                        lineTo(0f, yBase)
                        for (i in 0..56) {
                            val x = w * i / 56f
                            val heightNoise = sin(i * .32f + ridge * 1.2f) * .42f +
                                sin(i * .69f + ridge * 2.4f) * .28f +
                                sin(i * .11f) * .28f
                            lineTo(x, yBase - heightNoise * h * .11f)
                        }
                        lineTo(w, h);close()
                    }
                    drawPath(
                        p, if (warm) Color(0xFF100E18).copy(alpha = .24f + ridge * .14f)
                        else Color(0xFF01132A).copy(alpha = .27f + ridge * .16f)
                    )
                }
                // Small pavilion landmark over the landscape on the right.
                val px = w * .865f
                val py = h * .56f
                val dark = if (warm) Color(0xFF0E1422) else Color(0xFF020D22)
                drawRect(dark.copy(alpha = .86f),
                    topLeft = Offset(px - w * .017f, py - h * .03f),
                    size = Size(w * .034f, h * .12f))
                repeat(2) { roof ->
                    val y = py - h * .075f + roof * h * .055f
                    val path = Path().apply {
                        moveTo(px - w * .060f, y)
                        quadraticBezierTo(px, y - h * .065f, px + w * .059f, y)
                        lineTo(px + w * .044f, y + h * .012f)
                        lineTo(px - w * .044f, y + h * .012f)
                        close()
                    }
                    drawPath(path, dark)
                }
            } else {
                val startX = w * .36f
                repeat(7) { i ->
                    val p = startX + i * w * .18f
                    drawLine(
                        tint.copy(alpha = if (brain) .15f else .07f),
                        Offset(p + w * .10f, 0f),
                        Offset(p - w * .18f, h),
                        strokeWidth = 1.5f
                    )
                    val y = h * (i * .22f - .15f)
                    drawLine(
                        tint.copy(alpha = if (brain) .15f else .07f),
                        Offset(w * .23f, y),
                        Offset(w, y + h * .40f), strokeWidth = 1.5f
                    )
                }
            }
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color.Transparent, Color(0x28030C24), Color(0xE006142D)),
                    startY = h * .24f, endY = h,
                )
            )
            if (muted) {
                drawRect(Color(0x50041430))
            }
        }
        content()
    }
}

@Composable
fun AchievementSummarySurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(13.dp)
    Box(
        modifier.clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xF9051A40), Color(0xF407204F), Color(0xFA061B43))
                )
            )
            .border(1.dp, Ink.blue.copy(alpha = .86f), shape),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                Brush.radialGradient(listOf(Ink.gold.copy(alpha = .15f), Color.Transparent),
                    center = Offset(w * .14f, h * .5f), radius = w * .2f),
                w * .2f, Offset(w * .14f, h * .5f),
            )
            drawCircle(
                Brush.radialGradient(listOf(Ink.cyan.copy(alpha = .16f), Color.Transparent),
                    center = Offset(w * .71f, h * .5f), radius = w * .25f),
                w * .25f, Offset(w * .71f, h * .5f),
            )
        }
        content()
    }
}

