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
    val base = when (index) {
        0 -> listOf(Color(0xFF01464B), Color(0xFF01343F), Color(0xFF01414C), Color(0xFF01565A), Color(0xFF015157))
        1 -> listOf(Color(0xFF00404C), Color(0xFF002D3C), Color(0xFF003748), Color(0xFF015B69), Color(0xFF015866))
        2 -> listOf(Color(0xFF032F64), Color(0xFF03214B), Color(0xFF032752), Color(0xFF033665), Color(0xFF033765))
        3 -> listOf(Color(0xFF37382F), Color(0xFF202830), Color(0xFF3A3429), Color(0xFF6F5631), Color(0xFF685637))
        4 -> listOf(Color(0xFF292466), Color(0xFF1D1B58), Color(0xFF29225F), Color(0xFF322367), Color(0xFF2D205F))
        else -> listOf(Color(0xFF612035), Color(0xFF39142E), Color(0xFF51172B), Color(0xFF771E31), Color(0xFF761F30))
    }
    Box(modifier.clip(rounded).background(Brush.horizontalGradient(base))
        .border(1.1.dp, tint, rounded)) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val glowCenter = Offset(w * .77f, h * .13f)
            drawCircle(
                Brush.radialGradient(
                    listOf(tint.copy(alpha = .12f), Color.Transparent),
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
                    listOf(tint.copy(alpha = .13f), Color.Transparent),
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
