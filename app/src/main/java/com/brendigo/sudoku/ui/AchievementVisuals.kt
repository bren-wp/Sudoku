package com.brendigo.sudoku.ui

import android.graphics.BitmapFactory
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
    val context = LocalContext.current
    val art = remember(context, title) {
        val resource = when (title) {
            "Prva pobjeda" -> "achievement-gold-art.webp"
            "Bez greške" -> "achievement-warm-art.webp"
            "Sedam dana zaredom", "Vrući niz" -> "achievement-blue-art.webp"
            else -> null
        }
        resource?.let {
            runCatching {
                context.assets.open(it).use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
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
        modifier
            .then(if (completed) Modifier.shadow(
                elevation = 5.dp, shape = frame,
                ambientColor = edge.copy(alpha = .37f),
                spotColor = edge.copy(alpha = .47f),
            ) else Modifier)
            .clip(frame)
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
            // Owner-provided artwork is a small text-free crop of only
            // mountains/clouds/moon. Scores, rewards, labels and progress
            // remain live Compose UI drawn above this decorative layer.
            if (art != null) {
                drawImage(
                    image = art,
                    dstOffset = IntOffset((w * .47f).roundToInt(), (h * .025f).roundToInt()),
                    dstSize = IntSize((w * .53f).roundToInt(), (h * .69f).roundToInt()),
                    alpha = if (completed && warm) .93f
                        else if (warm) .80f else .82f,
                    filterQuality = FilterQuality.High,
                )
                // Fade from the *left edge of the entire card*. The earlier
                // overlay started abruptly at x=34%, making a visible
                // vertical seam in the screenshot behind the live badge UI.
                // Keep the owner-supplied scenery strictly decorative.
                drawRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFA071935), Color(0xEF071935),
                            Color(0xB8071935), Color.Transparent,
                        ),
                        startX = 0f, endX = w * .92f,
                    ),
                    topLeft = Offset.Zero,
                    size = Size(w, h * .74f),
                )
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
            // Narrow illuminated rim follows the reference's layered card.
            drawLine(
                color = edge.copy(alpha = if (muted) .15f else .52f),
                start = Offset(w * .06f, 1.5.dp.toPx()),
                end = Offset(w * .65f, 1.5.dp.toPx()),
                strokeWidth = 1.25.dp.toPx(),
            )
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


/**
 * The five large featured badges are cut from the OWNER-SUPPLIED reference
 * art, not from an emulator screenshot and not a rasterized UI. They contain
 * icon silhouettes only: the awards' texts, points, progress, gestures and
 * semantics are still live Compose components.
 *
 * A 5×1 WebP atlas is bundled offline: 5 cells, 64×64 pixels each, ordered
 * trophy / calendar / brain / bolt / flame. On error fall back to our real
 * vector glyph; never make the entire badge disappear.
 */
@Composable
fun AchievementBadgeArt(
    title: String,
    modifier: Modifier = Modifier,
    tint: Color = Ink.cyan,
) {
    val cell = when (title) {
        "Prva pobjeda" -> 0
        "Sedam dana zaredom" -> 1
        "Majstor logike" -> 2
        "Bez greške" -> 3
        "Vrući niz" -> 4
        else -> -1
    }
    if (cell < 0) {
        BrandIcon(achievementIconFor(title), modifier, tint)
        return
    }
    val context = LocalContext.current
    val atlas = remember(context) {
        runCatching {
            context.assets.open("achievement-badges-atlas.webp").use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }
    if (atlas == null || atlas.width < 320 || atlas.height < 64) {
        BrandIcon(achievementIconFor(title), modifier, tint)
    } else {
        // The 64px atlas deliberately includes transparent padding around
        // each original silhouette. Crop that empty margin at draw time,
        // or the icon appears 35–45% smaller than the owner's reference.
        // Each box is (x, y, width, height) within one 64px atlas cell.
        val crop = when (cell) {
            0 -> intArrayOf(5, 7, 52, 50)   // 3D trophy
            1 -> intArrayOf(9, 9, 47, 47)   // calendar with 7
            2 -> intArrayOf(8, 8, 52, 47)   // neural outlines
            3 -> intArrayOf(15, 9, 38, 51)  // lightning bolt
            else -> intArrayOf(11, 7, 43, 49) // flaming streak
        }
        Canvas(modifier) {
            val fit = minOf(
                size.width / crop[2].toFloat(),
                size.height / crop[3].toFloat(),
            )
            val dw = (crop[2] * fit).roundToInt().coerceAtLeast(1)
            val dh = (crop[3] * fit).roundToInt().coerceAtLeast(1)
            drawImage(
                image = atlas,
                srcOffset = IntOffset(cell * 64 + crop[0], crop[1]),
                srcSize = IntSize(crop[2], crop[3]),
                dstOffset = IntOffset(
                    ((size.width - dw) / 2f).roundToInt(),
                    ((size.height - dh) / 2f).roundToInt(),
                ),
                dstSize = IntSize(dw, dh),
                filterQuality = FilterQuality.High,
            )
        }
    }
}
