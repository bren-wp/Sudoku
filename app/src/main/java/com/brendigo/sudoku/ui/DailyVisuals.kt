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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
            // Subtle raised glass; all labels and actions stay native Compose.
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(highlight.copy(alpha = highlight.alpha * .19f), Color.Transparent),
                        center = Offset(w * .84f, h * .08f),
                        radius = w * .62f,
                    ),
                )
                drawLine(
                    color = Color.White.copy(alpha = .18f),
                    start = Offset(w * .07f, 1.1.dp.toPx()),
                    end = Offset(w * .60f, 1.1.dp.toPx()),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .border(1.dp, highlight.copy(alpha = highlight.alpha * .83f), rounded),
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
    Canvas(Modifier.matchParentSize().testTag("daily-hero-scenery")) {
        val w = size.width
        val h = size.height
        drawRect(
            Brush.horizontalGradient(listOf(
                Color(0xFF06172E), Color(0xFF14233A),
                Color(0xFF5D4A31), Color(0xFF976C37),
            )),
        )
        // Carry the original mountains into the dark text half of the card
        // without copying any text/buttons from the reference screenshot.
        // These still mountain ridges are native draw paths: no bitmap UI,
        // animation, battery work, or changes to the existing moon/pagoda.
        val farRidge = Path().apply {
            moveTo(w * .29f, h * .70f)
            lineTo(w * .29f, h * .62f)
            lineTo(w * .37f, h * .48f)
            lineTo(w * .42f, h * .57f)
            lineTo(w * .49f, h * .39f)
            lineTo(w * .57f, h * .56f)
            lineTo(w * .65f, h * .45f)
            lineTo(w * .74f, h * .63f)
            lineTo(w * .74f, h * .70f)
            close()
        }
        drawPath(
            farRidge,
            Brush.verticalGradient(
                listOf(Color(0x6B496280), Color(0x9F142E51), Color(0xB6071C3B)),
                startY = h * .37f,
                endY = h * .72f,
            ),
        )
        val nearRidge = Path().apply {
            moveTo(w * .27f, h * .77f)
            lineTo(w * .27f, h * .68f)
            lineTo(w * .35f, h * .61f)
            lineTo(w * .43f, h * .72f)
            lineTo(w * .52f, h * .58f)
            lineTo(w * .61f, h * .68f)
            lineTo(w * .71f, h * .60f)
            lineTo(w * .77f, h * .76f)
            close()
        }
        drawPath(nearRidge, Color(0x9C061A39))
        scenery?.let { art ->
            // Preserve the reference moon/pagoda coordinates, but softly
            // reveal the bitmap over the native mountains. Mask only the
            // decorative WebP, never Compose text, controls or reward data.
            // The previous sharp WebP left edge at 60% of the card caused a
            // visible vertical stripe against the continuous mountain ridge.
            drawContext.canvas.saveLayer(
                Rect(0f, 0f, w, h * .67f),
                Paint(),
            )
            drawImage(
                image = art,
                dstOffset = IntOffset((w * .60f).roundToInt(), 3),
                dstSize = IntSize((w * .395f).roundToInt(), (h * .67f).roundToInt()),
                filterQuality = FilterQuality.High,
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    0.00f to Color.Transparent,
                    0.36f to Color.White,
                    1.00f to Color.White,
                    startX = w * .60f,
                    endX = w * .995f,
                ),
                topLeft = Offset(w * .60f, 0f),
                size = Size(w * .395f, h * .67f),
                blendMode = BlendMode.DstIn,
            )
            drawContext.canvas.restore()
        }
        // Keep the existing moon and pagoda coordinates. The former right
        // crop started abruptly at x=60% of the panel; an explicit smooth
        // multi-stop dark scrim hides the seam while allowing mountain
        // silhouettes to read through the center-right under live text.
        drawRect(
            Brush.horizontalGradient(
                0.00f to Color(0xF9041733),
                0.34f to Color(0xEC041733),
                0.55f to Color(0xCF041733),
                0.64f to Color(0x99041733),
                0.84f to Color.Transparent,
                startX = 0f,
                endX = w,
            ),
            topLeft = Offset.Zero,
            size = Size(w, h * .70f),
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
        modifier.drawBehind {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    Brush.radialGradient(
                        listOf(Color(0x77FFC764), Color.Transparent),
                        center = c, radius = size.width * .69f,
                    ),
                    radius = size.width * .69f, center = c,
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0x36FFF2BB), Color(0x08FFB61C), Color.Transparent),
                        center = Offset(size.width * .42f, size.height * .30f),
                        radius = size.width * .51f,
                    ),
                    radius = size.width * .51f,
                    center = Offset(size.width * .42f, size.height * .30f),
                )
                drawCircle(
                    color = Color(0x72FFEDAC),
                    radius = size.width * .46f,
                    center = c,
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .border(1.6.dp, Ink.gold, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) {
        // Owner's reference shows a small golden award jar with a star,
        // not a generic gift bow. Render native vectors at any density; the
        // surrounding true-day reward text remains independently selectable.
        Canvas(Modifier.fillMaxSize(.72f)) {
            val w = size.width
            val h = size.height
            val jar = Offset(w * .18f, h * .39f)
            val jarSize = Size(w * .64f, h * .48f)
            drawRoundRect(
                color = Color(0xFF231A22),
                topLeft = jar,
                size = jarSize,
                cornerRadius = CornerRadius(w * .07f),
            )
            drawRoundRect(
                color = Ink.gold,
                topLeft = jar,
                size = jarSize,
                cornerRadius = CornerRadius(w * .07f),
                style = Stroke(width = w * .06f),
            )
            // Two short cap handles and the arched handle of the original
            // medal motif; art only, with no touch handler or fake state.
            drawLine(
                Ink.gold, Offset(w * .16f, h * .34f),
                Offset(w * .84f, h * .34f),
                strokeWidth = w * .09f, cap = StrokeCap.Round,
            )
            drawArc(
                color = Ink.gold,
                startAngle = 180f, sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(w * .30f, h * .10f),
                size = Size(w * .40f, h * .38f),
                style = Stroke(width = w * .065f, cap = StrokeCap.Round),
            )
            val star = Path()
            for (point in 0 until 10) {
                val angle = -Math.PI / 2.0 + point * Math.PI / 5.0
                val radius = if (point % 2 == 0) w * .165f else w * .074f
                val x = w * .5f + kotlin.math.cos(angle).toFloat() * radius
                val y = h * .615f + kotlin.math.sin(angle).toFloat() * radius
                if (point == 0) star.moveTo(x, y) else star.lineTo(x, y)
            }
            star.close()
            drawPath(star, Ink.gold)
        }
    }
}

@Composable
fun DailyWeekMarker(checked: Boolean, dayLabel: String, diameter: Dp = 32.dp) {
    val circle = RoundedCornerShape(50)
    Box(
        Modifier.size(diameter)
            .then(if (checked) Modifier.shadow(
                elevation = 4.dp, shape = circle,
                ambientColor = Ink.cyan.copy(alpha = .45f),
                spotColor = Ink.cyan.copy(alpha = .52f),
            ) else Modifier)
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
            .drawBehind {
                if (enabled) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = .27f), Color.Transparent),
                            startY = 0f, endY = size.height * .57f,
                        ),
                        size = Size(size.width, size.height * .55f),
                        cornerRadius = CornerRadius(80.dp.toPx()),
                    )
                    drawLine(
                        color = Color.White.copy(alpha = .34f),
                        start = Offset(size.width * .10f, 1.2.dp.toPx()),
                        end = Offset(size.width * .80f, 1.2.dp.toPx()),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .border(1.6.dp, if (enabled) Ink.cyan else Ink.line, rounded)
            .testTag("primary-action")
            .semantics {
                contentDescription = title
                role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Keep the reference typography when space permits, but preserve
        // readable full labels at 240-270dp and larger system font scales.
        val compactLabel = maxWidth < 225.dp ||
            LocalDensity.current.fontScale >= 1.5f
        val dense = compactLabel || maxWidth < 300.dp || title.length > 23
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
                modifier = Modifier.testTag("daily-primary-action-label"),
                color = Ink.white,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (compactLabel) 13.sp else if (dense) 15.sp else 20.sp,
                lineHeight = if (compactLabel) 15.sp else 22.sp,
                textAlign = TextAlign.Center,
                maxLines = if (compactLabel) 2 else 1,
            )
        }
    }
}
