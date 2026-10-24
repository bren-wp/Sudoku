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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

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
        for (i in 0..35) {
            val y = h * (.90f + (i / 35f) * .10f)
            val x = (i * 83 % 100) / 100f * w
            drawLine(Ink.cyan.copy(alpha = .04f + i % 5 * .02f),
                Offset(x, y), Offset(x + w * .045f, y), 1.2f)
        }
        // Distinctive pagoda on the right, built from paths rather than external art.
        val cx = w * .91f
        val by = h * .87f
        val unit = w * .021f
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
        // Flower clusters create the foreground framing from the references.
        for (i in 0..24) {
            val t = i / 24f
            val x = w * t * .27f
            val y = h * (.87f + .08f * sin(i * .5f).toFloat())
            val radius = w * (.004f + (i % 3) * .002f)
            drawCircle(Color(0xFF8EAEFF).copy(alpha = .2f), radius, Offset(x, y))
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
            .background(Brush.linearGradient(listOf(highlight.copy(alpha = .20f), Color(0xE8061740), Color(0xF603102E))))
            .border(1.dp, highlight.copy(alpha = .8f), RoundedCornerShape(corner)),
        content = content,
    )
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
