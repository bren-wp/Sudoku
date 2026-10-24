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
                if (bright) listOf(Color(0xFFBDE9FF), Color(0xFF2C7DCA), Color(0xFF071944))
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
        drawCircle(Color(0xFFA2D8FF).copy(alpha = .74f),
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
        mountains(h * .81f, h * .042f, Color(0xFF0A2A68).copy(alpha = .68f), 2.2f)
        mountains(h * .85f, h * .055f, Color(0xFF0B2A60), .1f)
        mountains(h * .89f, h * .038f, Color(0xFF071D49), 1.5f)
        drawRect(
            Brush.verticalGradient(
                listOf(Color(0x00226BD0), Color(0x993A89CD), Color(0xFF03112C)),
                startY = h * .88f, endY = h,
            ),
            topLeft = Offset(0f, h * .88f),
            size = androidx.compose.ui.geometry.Size(w, h * .12f),
        )
        // Fine moonlit ripples across the lake.
        for (i in 0..70) {
            val y = h * (.90f + (i / 70f) * .10f)
            val x = (i * 83 % 100) / 100f * w
            drawLine(Ink.cyan.copy(alpha = .04f + i % 5 * .02f),
                Offset(x, y), Offset(x + w * .045f, y), 1.2f)
        }
        // Distinctive pagoda on the right, built from paths rather than external art.
        val cx = w * .87f
        val by = h * .865f
        val unit = w * .033f
        val structure = Path().apply {
            moveTo(cx - unit * 1.5f, by)
            lineTo(cx - unit * 1.5f, by - unit * 3.5f)
            lineTo(cx + unit * 1.5f, by - unit * 3.5f)
            lineTo(cx + unit * 1.5f, by)
            close()
        }
        drawPath(structure, Color(0xFF020C22))
        for (tier in 0..2) {
            val y = by - unit * (3f + tier * 1.6f)
            val roof = Path().apply {
                moveTo(cx - unit * (2.8f - tier * .35f), y)
                quadraticTo(cx, y - unit * 1.4f, cx + unit * (2.8f - tier * .35f), y)
                lineTo(cx + unit * (2.6f - tier * .35f), y + unit * .36f)
                quadraticTo(cx, y - unit * .35f, cx - unit * (2.6f - tier * .35f), y + unit * .36f)
                close()
            }
            drawPath(roof, Color(0xFF010817))
        }
        drawRect(Ink.gold.copy(alpha = .82f),
            Offset(cx - unit * .36f, by - unit * 3f),
            androidx.compose.ui.geometry.Size(unit * .72f, unit * 1.4f))
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
        for (i in 0..28) {
            val y = by + i * h * .003f
            if (y < h) drawLine(
                Ink.gold.copy(alpha = (.24f * (1f - i / 28f)).coerceAtLeast(0f)),
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
        drawPath(branch, Color(0xFF020C26), style = Stroke(width = w * .012f))
        for (i in 0..130) {
            val t = ((i * 53 + 7) % 101) / 100f
            val x = w * t * .45f
            val y = h * (.82f + .14f * ((i * 37 % 103) / 103f))
            val radius = w * (.0035f + (i % 4) * .0013f)
            val hue = if (i % 3 == 0) Color(0xFF9CA6FB) else Color(0xFF617BCF)
            val opacity = if (i % 4 == 0) .60f else .37f
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
                Color(0xFFFFDBB0).copy(alpha = opacity * .65f),
                radius = radius * .49f,
                center = Offset(x, y),
            )
        }
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        SudokuLogo(if (compact) 46.dp else 64.dp)
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = "Sudoku",
                fontSize = if (compact) 38.sp else 49.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(brush = Brush.horizontalGradient(listOf(Ink.white, Ink.white, Ink.cyan, Ink.blue))),
                letterSpacing = (-1).sp,
            )
            Text("by Brendigo", fontSize = 14.sp, color = Ink.muted, letterSpacing = 3.sp)
        }
    }
}

@Composable
fun SudokuLogo(size: Dp) {
    val values = listOf("7", "", "", "", "3", "", "", "", "9")
    Column(
        Modifier.size(size)
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
                            .background(if (center) Color(0xFF0468D8) else Color(0xFF071B47)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(values[r * 3 + c], fontSize = if (size > 50.dp) 18.sp else 13.sp,
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
    Box(
        modifier.clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(listOf(highlight.copy(alpha = .23f), Color(0xE8061740), Color(0xF603102E))))
            .border(1.dp, highlight.copy(alpha = .8f), RoundedCornerShape(corner)),
    ) {
        // Dim moonlight and distant terrain are decorative, never baked-in UI.
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                Brush.radialGradient(
                    listOf(highlight.copy(alpha = .32f), Color.Transparent),
                    center = Offset(w * .82f, h * .14f),
                    radius = w * .48f,
                ),
                radius = w * .48f,
                center = Offset(w * .82f, h * .14f),
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(highlight.copy(alpha = .13f), Color.Transparent),
                    center = Offset(w * .08f, h * .76f),
                    radius = w * .41f,
                ),
                radius = w * .41f,
                center = Offset(w * .08f, h * .76f),
            )
            val mountain = Path().apply {
                moveTo(w * .38f, h)
                lineTo(w * .54f, h * .69f)
                lineTo(w * .67f, h * .81f)
                lineTo(w * .81f, h * .61f)
                lineTo(w, h * .87f)
                lineTo(w, h)
                close()
            }
            drawPath(mountain, highlight.copy(alpha = .085f))
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
    Text(text, modifier = modifier, color = Ink.muted, fontSize = 15.sp,
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
