package com.brendigo.sudoku.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Native Compose card without the generic Sudoku-grid watermark.
 * This is a purely decorative surface; touch and accessibility nodes are
 * owned by the content of the panel.
 */
@Composable
fun DailyFrame(
    modifier: Modifier = Modifier,
    highlight: Color = Ink.blue,
    radius: Dp = 13.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val rounded = RoundedCornerShape(radius)
    Box(
        modifier.clip(rounded)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xF902173D), Color(0xF704193B), Color(0xFB061736))
                )
            )
            .border(1.dp, highlight.copy(alpha = .83f), rounded),
    ) { content() }
}

/**
 * This WebP contains ONLY the upper-right nightscape from the user's artwork:
 * moon, foliage, mountains and a pavilion. It contains no screenshot labels,
 * UI controls, dates, scores or other dynamic content.
 */
@Composable
fun BoxScope.DailyHeroBackdrop() {
    val context = LocalContext.current
    val scenery = remember(context) {
        runCatching {
            context.assets.open("daily-hero-scenery.webp").use {
                BitmapFactory.decodeStream(it)?.asImageBitmap()
            }
        }.getOrNull()
    }
    Canvas(Modifier.matchParentSize()) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.horizontalGradient(listOf(
                Color(0xFF06172E), Color(0xFF14233A),
                Color(0xFF5D4A31), Color(0xFF976C37),
            )),
        )
        scenery?.let { art ->
            drawImage(
                image = art,
                dstOffset = IntOffset((w * .60f).roundToInt(), 3),
                dstSize = IntSize((w * .395f).roundToInt(), (h * .67f).roundToInt()),
                filterQuality = FilterQuality.High,
            )
        }
        // Seamless navy-to-art feather at the left side of the decorative
        // bitmap. The source contains no text, so real Compose labels still
        // own the readable foreground on every viewport.
        drawRect(
            Brush.horizontalGradient(
                listOf(Color(0xFF04152F), Color(0xFF04152F),
                    Color(0x5604152F), Color.Transparent),
                startX = w * .46f, endX = w * .98f,
            ),
            topLeft = Offset(w * .46f, 0f),
            size = Size(w * .54f, h * .70f),
        )
        drawRect(
            Brush.horizontalGradient(
                listOf(Color(0xFE04152F), Color(0xD8041733),
                    Color(0x13031733), Color.Transparent),
                startX = 0f, endX = w * .68f,
            ),
        )
        // Match the reference: the metric row floats above navy shadows,
        // not on top of a bright yellow background.
        drawRect(
            Brush.verticalGradient(
                listOf(Color.Transparent, Color(0xB8031634),
                    Color(0xFF03132D)),
                startY = h * .40f, endY = h * .97f,
            ),
        )
    }
}

@Composable
fun DailyRewardEmblem(modifier: Modifier = Modifier) {
    Box(
        modifier
            .drawBehind {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0x77FFC764), Color.Transparent),
                        center = c, radius = size.width * .69f,
                    ),
                    radius = size.width * .69f, center = c,
                )
            }
            .border(1.6.dp, Ink.gold, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        BrandIcon("gift", Modifier.fillMaxSize(.62f), Ink.gold)
    }
}

@Composable
fun DailyWeekMarker(checked: Boolean, dayLabel: String, diameter: Dp = 32.dp) {
    val circle = RoundedCornerShape(50)
    Box(
        Modifier.size(diameter)
            .semantics {
                contentDescription = "$dayLabel: " +
                    if (checked) "dnevni izazov riješen" else "dnevni izazov nije riješen"
            }
            .drawBehind {
                if (checked) {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Ink.cyan.copy(alpha = .30f), Color.Transparent),
                            center = Offset(size.width * .5f, size.height * .5f),
                            radius = size.width * .95f,
                        ),
                        radius = size.width * .95f,
                        center = Offset(size.width * .5f, size.height * .5f),
                    )
                }
            }
            .background(if (checked) Color(0xFF04465C) else Color(0xFF041B40), circle)
            .border(if (checked) 1.6.dp else 1.2.dp,
                if (checked) Ink.cyan else Ink.blue, circle),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) BrandIcon("check", Modifier.size(diameter * .70f), Ink.cyan)
    }
}

@Composable
fun DailyStartAction(
    title: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val rounded = RoundedCornerShape(50)
    BoxWithConstraints(
        modifier.heightIn(min = 55.dp)
            .clip(rounded)
            .background(
                Brush.verticalGradient(
                    if (enabled) listOf(
                        Color(0xFF08D4FF), Color(0xFF0A83FE), Color(0xFF0755F2))
                    else listOf(Color(0xFF1A3855), Color(0xFF173151)),
                ),
            )
            .border(1.6.dp, if (enabled) Ink.cyan else Ink.line, rounded)
            .testTag("primary-action")
            .semantics {
                contentDescription = title
                role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val dense = maxWidth < 300.dp || title.length > 23
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            BrandIcon(
                if (title.startsWith("✓")) "check" else "play-filled",
                Modifier.size(if (dense) 23.dp else 27.dp),
                Ink.white,
            )
            Spacer(Modifier.width(if (dense) 9.dp else 14.dp))
            Text(
                title.removePrefix("✓").trim(),
                color = Ink.white,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (dense) 15.sp else 20.sp,
                maxLines = 1,
            )
        }
    }
}
