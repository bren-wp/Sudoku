package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.graphics.BitmapFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.math.cos

object Ink {
    val navy = Color(0xFF020B27)
    val blue = Color(0xFF0764FA)
    val cyan = Color(0xFF11D9FA)
    val teal = Color(0xFF00E4C2)
    val gold = Color(0xFFFFD058)
    val purple = Color(0xFFAC83FF)
    val muted = Color(0xFFB0C8EF)
    val white = Color(0xFFF5F9FF)
    val red = Color(0xFFFF5978)
    val line = Color(0xFF2876D5)
}

@Composable
fun SudokuTheme(dark: Boolean = true, content: @Composable () -> Unit) {
    val scheme = if (dark) darkColorScheme(
        primary = Ink.cyan, secondary = Ink.teal, background = Ink.navy,
        surface = Color(0xFF06183E), onSurface = Ink.white, onBackground = Ink.white,
    ) else lightColorScheme(
        primary = Color(0xFF006BE8), secondary = Color(0xFF008879),
        background = Color(0xFFEAF4FF), surface = Color.White,
        onSurface = Color(0xFF091C42), onBackground = Color(0xFF091C42),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

/**
 * Composited visual background: procedural interactive-scale gradients,
 * plus a small compressed artwork layer extracted from the owner's original
 * noninteractive scenery. No full-screen screenshot replaces any UI.
 */
@Composable
fun ScenicBackdrop(modifier: Modifier = Modifier, bright: Boolean = false,
                   splash: Boolean = false, home: Boolean = false,
                   newGame: Boolean = false, gameplay: Boolean = false) {
    val context = LocalContext.current
    val originalNightscape = remember(context) {
        runCatching {
            context.assets.open("scenery-reference.webp").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(
                if (bright) listOf(
                    Color(0xFF163D79), Color(0xFF255A9C),
                    Color(0xFF12376F), Color(0xFF091F46)
                )
                else listOf(Color(0xFF001132), Color(0xFF03173D), Color(0xFF031A43), Color(0xFF010D26)),
                startY = 0f, endY = h,
            )
        )
        // The reference has TWO separate angled Sudoku grids flanking
        // a dark central title area, not one grid crossing the full screen.
        // Geometry is anchored to viewport width, not device pixel density.
        // Keep it purely decorative so Compose buttons retain touch semantics.
        if (gameplay) {
            // The gameplay reference deliberately has a quiet upper field.
            // Never let decorative sudoku numbers/grid interfere with HUD
            // typography, error hearts, or the actual playable board.
            drawCircle(
                Brush.radialGradient(
                    listOf(Color(0x24175CA6), Color.Transparent),
                    center = Offset(w * .78f, h * .05f), radius = w * .44f,
                ),
                radius = w * .44f, center = Offset(w * .78f, h * .05f),
            )
        } else if (newGame) {
            // Only the upper-right tilted board belongs to the New Game
            // reference. Never draw the giant full-screen decorative grid.
            val boardTint = Color(0xFF167AF2)
            for (i in 0..5) {
                val x = w * (.67f + i * .102f)
                drawLine(boardTint.copy(alpha = if (i == 2) .65f else .38f),
                    Offset(x + w * .07f, 0f),
                    Offset(x - w * .13f, h * .24f),
                    strokeWidth = if (i == 2) 3.0f else 2.2f)
            }
            for (row in 0..5) {
                val y = h * (-.045f + row * .061f)
                drawLine(boardTint.copy(alpha = if (row == 3) .58f else .34f),
                    Offset(w * .60f, y - h * .055f),
                    Offset(w * 1.06f, y + h * .06f), strokeWidth = 2.5f)
            }
            val selected = Offset(w * .80f, h * .152f)
            drawCircle(Brush.radialGradient(
                listOf(Ink.cyan.copy(alpha = .36f), Color.Transparent),
                center = selected, radius = w * .12f),
                radius = w * .12f, center = selected)
        } else if (home) {
            // The original Home reference has two isolated, slanted boards.
            // The old shared grid crossed the title and bled behind the cards.
            for (i in -2..5) {
                val leftX = w * (i * .105f)
                val leftAlpha = if (i % 3 == 0) .42f else .22f
                drawLine(
                    Color(0xFF1768C0).copy(alpha = leftAlpha),
                    Offset(leftX, 0f), Offset(leftX + w * .12f, h * .217f),
                    strokeWidth = w * .0029f,
                )
                val leftY = i * w * .096f
                drawLine(
                    Color(0xFF1464AD).copy(alpha = .29f),
                    Offset(0f, leftY), Offset(w * .47f, leftY - w * .12f),
                    strokeWidth = w * .0027f,
                )
                val rightX = w * (.66f + i * .103f)
                drawLine(
                    Color(0xFF1268C5).copy(alpha = leftAlpha),
                    Offset(rightX, 0f), Offset(rightX - w * .13f, h * .213f),
                    strokeWidth = w * .0028f,
                )
                val rightY = i * w * .091f
                drawLine(
                    Color(0xFF0D5AAE).copy(alpha = .24f),
                    Offset(w * .61f, rightY - w * .015f),
                    Offset(w, rightY + w * .09f),
                    strokeWidth = w * .0027f,
                )
            }
        } else {
            for (i in -1..5) {
                val x = i * w * .105f
                val leftColor = Color(0xFF1267BD).copy(alpha = if (i % 3 == 0) .46f else .27f)
                drawLine(leftColor, Offset(x, 0f),
                    Offset(x + w * .20f, h * .275f), strokeWidth = w * .0029f)
                val y = i * w * .095f
                if (y < h * .30f) {
                    drawLine(Color(0xFF146FC0).copy(alpha = .30f),
                        Offset(0f, y), Offset(w * .52f, y - w * .145f),
                        strokeWidth = w * .0028f)
                }
                val rightX = w * (.66f + i * .093f)
                drawLine(Color(0xFF1268C5).copy(alpha = if (i % 3 == 0) .46f else .26f),
                    Offset(rightX, 0f), Offset(rightX - w * .16f, h * .29f),
                    strokeWidth = w * .0026f)
                val rightY = i * w * .090f
                if (rightY < h * .30f) {
                    drawLine(Color(0xFF0D5AAE).copy(alpha = .24f),
                        Offset(w * .59f, rightY - w * .045f),
                        Offset(w, rightY + w * .110f), strokeWidth = w * .0028f)
                }
            }
        }
        // Large translucent Sudoku digits are part of the original visual motif.
        // Native canvas text here is decoration, never interactive content.
        if (!gameplay) drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(24, 101, 209)
                alpha = 105
                textSize = w * .084f
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL
                )
            }
            val digits = if (newGame) listOf(
                Triple("5", .73f, .095f), Triple("9", .96f, .127f),
                Triple("3", .80f, .176f), Triple("1", .70f, .215f),
                Triple("6", .85f, .231f)
            ) else listOf(
                Triple("9", .10f, .034f), Triple("5", .86f, .021f),
                Triple("3", .20f, .11f), Triple("1", .70f, .095f),
                Triple("6", .92f, .18f)
            )
            if (newGame) {
                paint.textSize = w * .052f
                paint.alpha = 180
            }
            digits.forEach { (digit, x, y) ->
                if (newGame && digit == "3") {
                    paint.color = android.graphics.Color.rgb(26, 213, 255)
                    paint.alpha = 245
                    paint.setShadowLayer(w * .025f, 0f, 0f, paint.color)
                } else if (newGame) {
                    paint.color = android.graphics.Color.rgb(37, 121, 228)
                    paint.alpha = 165
                    paint.clearShadowLayer()
                }
                canvas.nativeCanvas.drawText(digit, w * x, h * y, paint)
            }
        }
        // Stars are deterministic for stable rendering and no flashing/recomposition.
        for (i in 1..60) {
            val x = ((i * 173 + 61) % 991) / 991f * w
            val y = ((i * 137 + 19) % 997) / 997f * h * .83f
            drawCircle(Ink.white.copy(alpha = .10f + (i % 4) * .055f),
                radius = if (i % 7 == 0) 1.4f else .7f, center = Offset(x, y))
        }
        // On the source splash, the moon and horizon sit appreciably higher
        // than behind the dense menu cards. Keep gameplay scenery unchanged.
        val moonY = if (splash) .705f else .77f
        if (!gameplay) {
            drawCircle(
                Brush.radialGradient(listOf(Color(0x5584C5FA), Color.Transparent)),
                radius = w * .16f, center = Offset(w * .81f, h * moonY),
            )
            drawCircle(Color(0xFFA2D8FF).copy(alpha = .84f),
                radius = w * .030f, center = Offset(w * .81f, h * moonY))
        }
        // Distant layered mountain silhouettes.
        fun mountains(y: Float, amplitude: Float, color: Color, phase: Float) {
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(0f, y)
                for (i in 0..48) {
                    val x = i / 48f * w
                    val f = sin(i * .46f + phase) * .46f + sin(i * .19f + phase) * .54f
                    lineTo(x, y - f * amplitude)
                }
                lineTo(w, h)
                close()
            }
            drawPath(path, color)
        }
        val distantMountainY = if (splash) .756f else .847f
        val nearMountainY = if (splash) .806f else .875f
        val lakeHorizon = if (splash) .843f else .910f
        mountains(h * distantMountainY, h * .035f, Color(0xFF091F50).copy(alpha = .87f), 2.2f)
        mountains(h * nearMountainY, h * .048f, Color(0xFF061B40), .1f)
        mountains(h * lakeHorizon, h * .034f, Color(0xFF02132D), 1.5f)
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0x00226BD0), Color(0x88234993), Color(0xFF010D27)),
                startY = h * (lakeHorizon - .03f), endY = h,
            ),
            topLeft = Offset(0f, h * (lakeHorizon - .03f)),
            size = androidx.compose.ui.geometry.Size(w, h * (1.03f - lakeHorizon)),
        )
        // Fine moonlit ripples across the lake.
        for (i in 0..100) {
            val y = h * (lakeHorizon + (i / 100f) * (1f - lakeHorizon))
            val x = (i * 83 % 100) / 100f * w
            drawLine(Ink.cyan.copy(alpha = .04f + i % 5 * .02f),
                Offset(x, y), Offset(x + w * .045f, y), 1.2f)
        }
        // Distinctive pagoda on the right, built from paths rather than external art.
        val cx = w * .865f
        val by = h * (if (splash) .855f else .920f)
        val unit = w * .028f
        // A three-storey lakeside pagoda: tiered roofs, curved eaves, a
        // narrow spire and two glowing lantern openings, all drawn behind UI.
        drawRect(Color(0xFF020B24),
            Offset(cx - unit * 1.45f, by - unit * 4.9f),
            androidx.compose.ui.geometry.Size(unit * 2.9f, unit * 4.9f))
        drawRect(Color(0xFF071A35),
            Offset(cx - unit * .90f, by - unit * 1.85f),
            androidx.compose.ui.geometry.Size(unit * 1.8f, unit * 1.85f))
        for (tier in 0..2) {
            val y = by - unit * (1.3f + tier * 1.75f)
            val reach = unit * (2.75f - tier * .22f)
            val roof = Path().apply {
                moveTo(cx - reach, y)
                quadraticTo(cx - reach * .70f, y + unit * .06f, cx, y - unit * .83f)
                quadraticTo(cx + reach * .70f, y + unit * .06f, cx + reach, y)
                lineTo(cx + reach * .90f, y + unit * .38f)
                quadraticTo(cx, y - unit * .21f, cx - reach * .90f, y + unit * .38f)
                close()
            }
            drawPath(roof, Color(0xFF01091B))
            drawLine(Color(0xFF14528B).copy(alpha = .40f),
                Offset(cx - reach * .83f, y + unit * .25f),
                Offset(cx + reach * .83f, y + unit * .25f), strokeWidth = 1.4f)
        }
        drawLine(Color(0xFF01091B),
            Offset(cx, by - unit * 6.1f), Offset(cx, by - unit * 7.0f),
            strokeWidth = unit * .22f)
        drawRect(Ink.gold.copy(alpha = .93f),
            Offset(cx - unit * .38f, by - unit * 3.75f),
            androidx.compose.ui.geometry.Size(unit * .76f, unit * 1.27f))
        drawRect(Color(0xFFFFC477).copy(alpha = .92f),
            Offset(cx - unit * .40f, by - unit * 1.68f),
            androidx.compose.ui.geometry.Size(unit * .80f, unit * 1.26f))
        drawRect(Color(0xFF020B24),
            Offset(cx - unit * 2.1f, by - unit * .23f),
            androidx.compose.ui.geometry.Size(unit * 4.2f, unit * .34f))
        // Soft amber lantern glow and patterned reflections on the water.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FFB66B), Color.Transparent),
                center = Offset(cx, by - unit * 2.4f),
                radius = unit * 6f,
            ),
            radius = unit * 6f,
            center = Offset(cx, by - unit * 2.4f),
        )
        for (i in 0..34) {
            val y = by + i * h * .0025f
            if (y < h) drawLine(
                Ink.gold.copy(alpha = (.28f * (1f - i / 34f)).coerceAtLeast(0f)),
                Offset(cx - w * .005f - (i % 4) * w * .006f, y),
                Offset(cx + w * .006f + (i % 5) * w * .009f, y),
                strokeWidth = 1.5f,
            )
        }

        // The owner's night-landscape references feature flowering branches
        // along the lower left edge. Draw them as deterministic vector petals.
        val branch = Path().apply {
            val rise = if (splash) .055f else 0f
            moveTo(-w * .05f, h * .99f)
            cubicTo(w * .06f, h * .94f, w * .06f, h * (.87f - rise), w * .23f, h * (.83f - rise))
            moveTo(w * .02f, h * .93f)
            cubicTo(w * .04f, h * .88f, w * .12f, h * (.84f - rise), w * .17f, h * (.79f - rise))
        }
        drawPath(branch, Color(0xFF010C24), style = Stroke(width = w * .009f))
        for (i in 0..87) {
            val t = ((i * 53 + 7) % 101) / 100f
            val x = w * t * .36f
            val y = h * ((if (splash) .80f else .86f) +
                (if (splash) .18f else .12f) * ((i * 37 % 103) / 103f))
            val radius = w * (.0024f + (i % 4) * .0009f)
            val hue = if (i % 3 == 0) Color(0xFF8274AE) else Color(0xFF485C96)
            val opacity = if (i % 4 == 0) .60f else .38f
            for (petal in 0..4) {
                val radians = petal * 1.256637f
                drawCircle(
                    color = hue.copy(alpha = opacity),
                    radius = radius * 1.22f,
                    center = Offset(
                        x + cos(radians) * radius * 1.35f,
                        y + sin(radians) * radius * 1.35f,
                    ),
                )
            }
            drawCircle(
                Color(0xFFD2B4C7).copy(alpha = opacity * .55f),
                radius = radius * .49f,
                center = Offset(x, y),
            )
        }

        // Precise source-art detail, not a flattened game screenshot.
        // The source image contains only scenery beneath all buttons and
        // card interactions. Its first pixels fade in to avoid a visible seam.
        originalNightscape?.let { art ->
            val y = when {
                splash -> .613f
                newGame -> .800f
                gameplay -> .795f
                else -> .730f
            }
            val height = 1f - y
            drawImage(
                image = art,
                srcSize = IntSize(art.width, art.height),
                dstOffset = IntOffset(0, (h * y).roundToInt()),
                dstSize = IntSize(w.roundToInt(), (h * height).roundToInt()),
                filterQuality = FilterQuality.Medium,
            )
        }
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    // Reference-sized 7-3-9 brand mark, with a compact fallback for narrow phones.
    // Keep a real text logo for scaling, localization and accessibility.
    BoxWithConstraints(modifier) {
        val narrow = maxWidth < 350.dp
        val logoSize = if (compact) 46.dp else if (narrow) 69.dp else 82.dp
        val headingSize = if (compact) 36.sp else if (narrow) 41.sp else 55.sp
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            SudokuLogo(logoSize)
            Spacer(Modifier.width(if (narrow) 10.dp else 9.dp))
            Column(Modifier.offset(y = if (compact) 0.dp else 8.dp)) {
                Text(
                    text = "Sudoku",
                    fontSize = headingSize,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(brush = Brush.horizontalGradient(
                        listOf(Ink.white, Ink.white, Ink.cyan, Ink.blue),
                    )),
                    letterSpacing = (-1.2).sp,
                    maxLines = 1,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.width(if (narrow) 18.dp else 30.dp).height(1.dp)
                            .background(Ink.cyan.copy(alpha = .65f)),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text("by Brendigo", fontSize = if (narrow) 10.sp else 11.sp,
                        color = Ink.muted, letterSpacing = 2.5.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun SudokuLogo(size: Dp, glowing: Boolean = false) {
    val values = listOf("7", "", "", "", "3", "", "", "", "9")
    Column(
        Modifier.size(size)
            .then(if (glowing) Modifier.shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(13.dp),
                ambientColor = Ink.cyan,
                spotColor = Ink.blue,
            ) else Modifier)
            .clearAndSetSemantics {
                contentDescription = "Sudoku by Brendigo, logotip 7, 3, 9"
            }
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF0058DF), Ink.cyan)))
            .padding(2.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(Color(0xFF061A49))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        repeat(3) { r ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                repeat(3) { c ->
                    val center = r == 1 && c == 1
                    Box(
                        Modifier.weight(1f).fillMaxHeight()
                            .background(if (center)
                                Brush.radialGradient(listOf(Color(0xFF12DFFF), Color(0xFF0054BA), Color(0xFF062358)))
                            else Brush.linearGradient(listOf(Color(0xFF071B47), Color(0xFF062452))))
                            .border(.55.dp, Ink.cyan.copy(alpha = .68f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(values[r * 3 + c], fontSize = (size.value * .29f).sp,
                            color = if (center) Ink.cyan else Ink.white, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    highlight: Color = Ink.line,
    corner: Dp = 18.dp,
    warmPagoda: Boolean = true,
    warmGlowAlpha: Float = .43f,
    content: @Composable BoxScope.() -> Unit,
) {
    val warm = highlight == Ink.gold
    Box(
        modifier.clip(RoundedCornerShape(corner))
            .background(
                if (warm) Brush.horizontalGradient(
                    listOf(Color(0xFC04132F), Color(0xF4091B3A),
                        Color(0xF940311A)),
                ) else Brush.linearGradient(
                    // Reference surfaces are substantially darker than the
                    // previous electric-blue glass treatment.
                    listOf(highlight.copy(alpha = .14f),
                        Color(0xF3051739), Color(0xFB030F29)),
                ),
            )
            .border(1.dp, highlight.copy(alpha = .88f),
                RoundedCornerShape(corner)),
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            drawCircle(
                Brush.radialGradient(
                    listOf(highlight.copy(alpha = if (warm) warmGlowAlpha.coerceIn(0f, 1f) else .31f),
                        Color.Transparent),
                    center = Offset(w * .83f, h * .16f),
                    radius = w * .65f,
                ),
                w * .65f, Offset(w * .83f, h * .16f),
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(highlight.copy(alpha = .11f), Color.Transparent),
                    center = Offset(w * .08f, h * .87f),
                    radius = w * .48f,
                ),
                w * .48f, Offset(w * .08f, h * .87f),
            )

            // Sudoku watermark belongs on cool-blue panels; warm illustrated
            // reward cards need a clear sky so their text stays readable.
            if (!warm) {
                val step = w * .23f
                val left = w * .57f
                for (index in 0..5) {
                    val x = left + index * step
                    drawLine(highlight.copy(alpha = .11f),
                        Offset(x, 0f), Offset(x - w * .31f, h), 1.5f)
                }
                for (index in 0..5) {
                    val y = -h * .27f + index * h * .29f
                    drawLine(highlight.copy(alpha = .10f),
                        Offset(left - w * .04f, y),
                        Offset(w, y + h * .31f), 1.5f)
                }
            }
            val hill = Path().apply {
                moveTo(0f, h)
                lineTo(0f, h * .93f)
                for (index in 0..35) {
                    val x = index / 35f * w
                    val variation = sin(index * .35f + 2f) * .48f +
                        sin(index * .73f + 1.1f) * .22f
                    lineTo(x, h * (.84f - variation * .075f))
                }
                lineTo(w, h)
                close()
            }
            drawPath(hill, Color(0xFF010D2E).copy(alpha = .26f))
            // Glass-like highlight remains behind actual Compose content.
            drawLine(
                highlight.copy(alpha = .38f),
                Offset(w * .075f, 1.5f),
                Offset(w * .60f, 1.5f),
                strokeWidth = 1.25f,
            )
            if (warm) {
                val moon = Offset(w * .87f, h * .21f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Ink.gold.copy(alpha = .30f), Color.Transparent),
                        center = moon, radius = w * .24f,
                    ), w * .24f, moon,
                )
                drawCircle(Ink.gold.copy(alpha = .85f),
                    radius = minOf(w, h) * .090f, center = moon)
                if (warmPagoda) {
                    val roof = Path().apply {
                        moveTo(w * .82f, h * .70f)
                    quadraticTo(w * .88f, h * .57f, w * .94f, h * .70f)
                    lineTo(w * .96f, h * .71f)
                    quadraticTo(w * .88f, h * .64f, w * .81f, h * .72f)
                    close()
                }
                drawPath(roof, Color(0xFF020E27))
                    drawRect(Color(0xFF020E27),
                        Offset(w * .85f, h * .70f),
                        androidx.compose.ui.geometry.Size(w * .07f, h * .20f))
                }
            }
        }
        content()
    }
}

@Composable
fun AccentText(text: String, size: Int = 38, modifier: Modifier = Modifier) {
    Text(text, modifier, style = TextStyle(
        brush = Brush.horizontalGradient(listOf(Ink.white, Ink.white, Ink.cyan)),
        fontSize = size.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-.5).sp,
    ))
}

@Composable
fun Subtitle(text: String, modifier: Modifier = Modifier, centered: Boolean = false) {
    Text(text, modifier = modifier, color = Ink.muted,
        fontSize = 12.sp, lineHeight = 16.sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start)
}

fun formatTime(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

val difficultyLabels = listOf("Vrlo lako", "Lako", "Srednje", "Teško", "Ekspert", "Majstor")

/** Natural Croatian difficulty caption on the game and resume screens. */
fun difficultyTitle(level: Int): String = when (level) {
    0 -> "Vrlo laka težina"
    1 -> "Laka težina"
    2 -> "Srednja težina"
    3 -> "Teška težina"
    4 -> "Ekspert"
    5 -> "Majstor"
    else -> "Sudoku"
}
val difficultyColors = listOf(Ink.teal, Ink.teal, Ink.blue, Ink.gold, Ink.purple, Ink.red)
