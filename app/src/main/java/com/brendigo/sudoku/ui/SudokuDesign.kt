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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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

/** Custom original vector scenery: no bitmap stretching and no photo/network assets. */
@Composable
fun ScenicBackdrop(modifier: Modifier = Modifier, bright: Boolean = false) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.verticalGradient(
                if (bright) listOf(
                    Color(0xFF163D79), Color(0xFF255A9C),
                    Color(0xFF12376F), Color(0xFF091F46)
                )
                else listOf(Color(0xFF020921), Color(0xFF071A43), Color(0xFF052052), Color(0xFF03112F)),
                startY = 0f, endY = h,
            )
        )
        // Faint oversized sudoku grid, like the supplied artwork.
        val spacing = w / 3.5f
        for (i in -2..8) {
            val x = i * spacing
            drawLine(Ink.blue.copy(alpha = .12f), Offset(x, 0f), Offset(x + w * .25f, h * .28f), 2.5f)
            drawLine(Ink.blue.copy(alpha = .10f), Offset(0f, x), Offset(w, x - w * .20f), 2.5f)
        }
        // Large translucent Sudoku digits are part of the original visual motif.
        // Native canvas text here is decoration, never interactive content.
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.rgb(24, 101, 209)
                alpha = 105
                textSize = w * .084f
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL
                )
            }
            listOf(
                Triple("9", .10f, .034f), Triple("5", .86f, .021f),
                Triple("3", .20f, .11f), Triple("1", .70f, .095f),
                Triple("6", .92f, .18f)
            ).forEach { (digit, x, y) ->
                canvas.nativeCanvas.drawText(digit, w * x, h * y, paint)
            }
        }
        // Stars are deterministic for stable rendering and no flashing/recomposition.
        for (i in 1..60) {
            val x = ((i * 173 + 61) % 991) / 991f * w
            val y = ((i * 137 + 19) % 997) / 997f * h * .83f
            drawCircle(Ink.white.copy(alpha = .10f + (i % 4) * .055f),
                radius = if (i % 7 == 0) 1.7f else .9f, center = Offset(x, y))
        }
        drawCircle(
            Brush.radialGradient(listOf(Color(0x5584C5FA), Color.Transparent)),
            radius = w * .16f, center = Offset(w * .81f, h * .77f),
        )
        drawCircle(Color(0xFFA2D8FF).copy(alpha = .84f),
            radius = w * .030f, center = Offset(w * .81f, h * .77f))
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
        mountains(h * .835f, h * .039f, Color(0xFF102B66).copy(alpha = .80f), 2.2f)
        mountains(h * .862f, h * .050f, Color(0xFF0A2455), .1f)
        mountains(h * .902f, h * .033f, Color(0xFF051943), 1.5f)
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0x00226BD0), Color(0x882459A3), Color(0xFF03112C)),
                startY = h * .88f, endY = h,
            ),
            topLeft = Offset(0f, h * .88f),
            size = androidx.compose.ui.geometry.Size(w, h * .12f),
        )
        // Fine moonlit ripples across the lake.
        for (i in 0..100) {
            val y = h * (.90f + (i / 100f) * .10f)
            val x = (i * 83 % 100) / 100f * w
            drawLine(Ink.cyan.copy(alpha = .04f + i % 5 * .02f),
                Offset(x, y), Offset(x + w * .045f, y), 1.2f)
        }
        // Distinctive pagoda on the right, built from paths rather than external art.
        val cx = w * .865f
        val by = h * .920f
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
            moveTo(-w * .05f, h * .99f)
            cubicTo(w * .06f, h * .94f, w * .06f, h * .87f, w * .23f, h * .83f)
            moveTo(w * .02f, h * .93f)
            cubicTo(w * .04f, h * .88f, w * .12f, h * .84f, w * .17f, h * .79f)
        }
        drawPath(branch, Color(0xFF010C24), style = Stroke(width = w * .009f))
        for (i in 0..112) {
            val t = ((i * 53 + 7) % 101) / 100f
            val x = w * t * .36f
            val y = h * (.86f + .12f * ((i * 37 % 103) / 103f))
            val radius = w * (.0028f + (i % 4) * .0012f)
            val hue = if (i % 3 == 0) Color(0xFF7897E4) else Color(0xFF4B67BA)
            val opacity = if (i % 4 == 0) .76f else .49f
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
                Color(0xFF91B9EE).copy(alpha = opacity * .55f),
                radius = radius * .49f,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        SudokuLogo(if (compact) 46.dp else 76.dp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "Sudoku",
                fontSize = if (compact) 36.sp else 45.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(brush = Brush.horizontalGradient(listOf(Ink.white, Ink.white, Ink.cyan, Ink.blue))),
                letterSpacing = (-1).sp,
            )
            Text("by Brendigo", fontSize = 11.sp, color = Ink.muted, letterSpacing = 3.sp)
        }
    }
}

@Composable
fun SudokuLogo(size: Dp) {
    val values = listOf("7", "", "", "", "3", "", "", "", "9")
    Column(
        Modifier.size(size)
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
                    listOf(highlight.copy(alpha = .23f),
                        Color(0xF3071C42), Color(0xFB03102B)),
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
                    listOf(highlight.copy(alpha = if (warm) .43f else .31f),
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
val difficultyColors = listOf(Ink.teal, Ink.teal, Ink.blue, Ink.gold, Ink.purple, Ink.red)
