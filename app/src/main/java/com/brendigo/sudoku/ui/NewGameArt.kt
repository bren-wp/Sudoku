package com.brendigo.sudoku.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val newGameAccents = listOf(
    Color(0xFF25F5A8), Color(0xFF02E9CB), Color(0xFF12CFFF),
    Color(0xFFFFDA66), Color(0xFFAA74FF), Color(0xFFFF5668),
)

/**
 * Non-interactive reference board, clipped inside each real Compose radio card.
 * Numbers and lines are art only; the actual Sudoku game remains engine-driven.
 */
@Composable
fun NewGameDifficultySurface(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val tint = newGameAccents[index]
    val rounded = RoundedCornerShape(11.dp)
    // The 941x1672 owner's reference uses very dark navy interiors with
    // localized neon edges; the previous gradient made the full right half
    // of each card too luminous (especially Teško and Majstor). These stops
    // darken the surfaces without changing contrast of native labels/icons.
    val base = when (index) {
        0 -> listOf(Color(0xFF003039), Color(0xFF002635), Color(0xFF002C39), Color(0xFF003743), Color(0xFF00343E))
        1 -> listOf(Color(0xFF002C39), Color(0xFF002333), Color(0xFF002937), Color(0xFF003D4A), Color(0xFF003844))
        2 -> listOf(Color(0xFF021C45), Color(0xFF021638), Color(0xFF021A45), Color(0xFF022B55), Color(0xFF02284D))
        3 -> listOf(Color(0xFF252A2B), Color(0xFF16212B), Color(0xFF2A2829), Color(0xFF463C2B), Color(0xFF42392D))
        4 -> listOf(Color(0xFF1B1B4B), Color(0xFF12153B), Color(0xFF1F1A50), Color(0xFF271B57), Color(0xFF241B4E))
        else -> listOf(Color(0xFF401B2E), Color(0xFF2C1529), Color(0xFF3B172D), Color(0xFF4C1B30), Color(0xFF491C2F))
    }
    Box(modifier.clip(rounded).background(Brush.horizontalGradient(base))
        .border(1.1.dp, tint, rounded)) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val glowCenter = Offset(w * .77f, h * .13f)
            drawCircle(
                Brush.radialGradient(
                    listOf(tint.copy(alpha = .08f), Color.Transparent),
                    center = glowCenter, radius = w * .42f,
                ),
                radius = w * .42f, center = glowCenter,
            )
            for (column in 0..4) {
                val x = w * (.65f + column * .108f)
                drawLine(tint.copy(alpha = if (column == 2) .27f else .12f),
                    Offset(x + h * .20f, 0f), Offset(x - h * .29f, h),
                    strokeWidth = if (column == 2) 2.0f else 1.45f)
            }
            for (row in 0..3) {
                val y = h * (-.23f + row * .55f)
                drawLine(tint.copy(alpha = if (row == 2) .27f else .12f),
                    Offset(w * .57f, y - h * .22f), Offset(w * 1.06f, y + h * .19f),
                    strokeWidth = if (row == 2) 2.1f else 1.5f)
            }
            val active = Offset(w * .76f, h * .58f)
            drawCircle(
                Brush.radialGradient(
                    listOf(tint.copy(alpha = .09f), Color.Transparent),
                    center = active, radius = h * .55f,
                ), radius = h * .55f, center = active,
            )
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = h * .35f
                    color = android.graphics.Color.argb(255,
                        (tint.red * 255).toInt(), (tint.green * 255).toInt(),
                        (tint.blue * 255).toInt())
                }
                val number = listOf("1", "2", "3", "4", "7", "9")[index]
                paint.setShadowLayer(h * .12f, 0f, 0f, paint.color)
                canvas.nativeCanvas.drawText(number, active.x, h * .71f, paint)
                paint.clearShadowLayer()
                paint.textSize = h * .11f
                paint.color = android.graphics.Color.argb(55, 125, 180, 240)
                canvas.nativeCanvas.drawText("3", w * .79f, h * .17f, paint)
                canvas.nativeCanvas.drawText("6", w * .90f, h * .84f, paint)
            }
            drawRect(
                Brush.horizontalGradient(listOf(Color(0x20001630), Color.Transparent),
                    startX = 0f, endX = w * .65f),
                size = androidx.compose.ui.geometry.Size(w * .65f, h),
            )
        }
        content()
    }
}

/** Single segmented control; preserves test tags, radio selection and touch targets. */
@Composable
fun NewGameSizeSelector(
    selectedSize: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val corners = RoundedCornerShape(12.dp)
    Row(modifier.height(43.dp).clip(corners)
        .background(Color(0xD0071A42)).border(1.dp, Ink.line, corners)
        .testTag("new-game-size-selector"),
        verticalAlignment = Alignment.CenterVertically) {
        listOf(9, 6, 4).forEachIndexed { index, option ->
            val active = selectedSize == option
            Box(Modifier.weight(1f).fillMaxHeight().testTag("size-$option")
                .semantics {
                    contentDescription = "Veličina $option puta $option"
                    role = Role.RadioButton
                    selected = active
                }
                .then(if (active) Modifier.clip(corners)
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF0757CD), Color(0xFF073B91), Color(0xFF052356))))
                    .border(1.5.dp, Ink.cyan, corners) else Modifier)
                .clickable(enabled = enabled) { onSelect(option) },
                contentAlignment = Alignment.Center) {
                Text("$option×$option", color = if (active) Ink.white else Ink.muted,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                if (index < 2 && !active) {
                    Box(Modifier.align(Alignment.CenterEnd).width(.7.dp)
                        .fillMaxHeight(.76f)
                        .background(Ink.line.copy(alpha = .65f)))
                }
            }
        }
    }
}

/** New Game only: vertically lit CTA, native text and filled play icon. */
@Composable
fun NewGameStartAction(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val round = RoundedCornerShape(50)
    Box(modifier.heightIn(min = 53.dp).clip(round)
        .background(Brush.verticalGradient(if (enabled) listOf(
            Color(0xFF10C9FF), Color(0xFF087FFB), Color(0xFF0757EE))
            else listOf(Color.DarkGray, Color.Gray)))
        .border(1.8.dp, if (enabled) Ink.cyan else Color.Gray, round)
        .testTag("primary-action")
        .semantics { contentDescription = title; role = Role.Button }
        .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            drawLine(Color.White.copy(alpha = .30f),
                Offset(size.width * .07f, 1.5f),
                Offset(size.width * .93f, 1.5f), strokeWidth = 2f)
        }
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            if (enabled) {
                BrandIcon("play-filled", Modifier.size(26.dp), Ink.white)
                Spacer(Modifier.width(14.dp))
            }
            Text(title, color = Ink.white, fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}
