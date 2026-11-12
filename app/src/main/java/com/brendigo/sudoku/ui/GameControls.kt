package com.brendigo.sudoku.ui

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun GameTool(
    name: String, accent: Color, modifier: Modifier,
    height: androidx.compose.ui.unit.Dp = 73.dp,
    active: Boolean = false, badge: String? = null,
    enabled: Boolean = true, tag: String = "", onClick: () -> Unit,
) {
    Box(
        modifier.testTag(tag)
            .alpha(if (enabled) 1f else .53f)
            .semantics {
                contentDescription = when {
                    name == "Bilješke" -> "$name, ${if (active) "uključeno" else "isključeno"}"
                    badge != null -> "$name, preostalo $badge"
                    else -> name
                }
                role = Role.Button
                selected = active
            }
            .heightIn(min = 48.dp).height(height)
            .clip(RoundedCornerShape(13.dp))
            .background(
                Brush.verticalGradient(
                    if (active) listOf(Color(0xFF00626D), Color(0xFF003B52), Color(0xFF032B45))
                    else listOf(Color(0xFF0A2350), Color(0xFF061C46), Color(0xFF081C44))
                )
            )
            // Reference: a raised glass top, a subtle edge reflection and
            // brighter teal emission for the selected Notes control.
            .drawBehind {
                val top = this.size.height * .43f
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (active) Color(0x4449FFFF) else Color(0x2668B7FF),
                            Color.Transparent,
                        ),
                        startY = 0f, endY = top,
                    ),
                    size = androidx.compose.ui.geometry.Size(this.size.width, top),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx()),
                )
            }
            .border(
                if (active) 1.6.dp else 1.dp,
                Brush.linearGradient(
                    if (active) listOf(Color(0xFF9EFFFF), Ink.cyan, Color(0xFF009CC6))
                    else listOf(Color(0xFF4B9DFF), Ink.blue.copy(alpha = .75f)),
                ),
                RoundedCornerShape(13.dp),
            )
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val narrowTool = maxWidth < 83.dp
            val condensedTool = narrowTool || maxHeight < 58.dp
            val isNotes = name == "Bilješke"
            if (isNotes) {
                // Separate, flow-laid-out icon/title/status rows. The old
                // overlapping Box alignment gave Text and status 24dp
                // semantic bounds each, causing real clipping on BOTH
                // 941x1672 and 720x1280 emulator captures.
                Column(
                    Modifier.fillMaxSize()
                        .padding(horizontal = 3.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BrandIcon("note",
                        Modifier.size(if (condensedTool) 14.dp else 20.dp)
                            .drawBehind {
                                if (active) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(Brush.radialGradient(
                                        listOf(Ink.cyan.copy(alpha = .45f), Color.Transparent),
                                        center = center, radius = size.width * .95f,
                                    ), center = center, radius = size.width * .95f)
                                }
                            },
                        if (active) Ink.cyan else Ink.muted)
                    Text(
                        "Bilješke", color = Ink.white,
                        modifier = Modifier.testTag("game-notes-label"),
                        fontSize = if (condensedTool) 8.sp else 11.sp,
                        lineHeight = if (condensedTool) 10.sp else 13.sp,
                        maxLines = 1,
                    )
                    Row(
                        Modifier.testTag("game-notes-status"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (active) "Uključeno" else "Isključeno",
                            color = if (active) Ink.cyan else Ink.muted,
                            fontSize = if (condensedTool) 7.sp else 9.sp,
                            lineHeight = if (condensedTool) 9.sp else 11.sp,
                            maxLines = 1,
                        )
                        Box(
                            Modifier.width(if (condensedTool) 17.dp else 22.dp)
                                .height(if (condensedTool) 10.dp else 12.dp)
                                .background(
                                    if (active) Ink.cyan else Color(0xFF294266),
                                    RoundedCornerShape(50),
                                ),
                            contentAlignment = if (active) Alignment.CenterEnd
                                else Alignment.CenterStart,
                        ) {
                            Box(
                                Modifier.padding(horizontal = 1.5.dp)
                                    .size(if (condensedTool) 7.dp else 9.dp)
                                    .background(Ink.white, RoundedCornerShape(50)),
                            )
                        }
                    }
                }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(
                        start = 5.dp, end = 5.dp, top = 4.dp, bottom = 5.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BrandIcon(
                        when (name) {
                            "Savjet" -> "hint"
                            "Poništi" -> "undo"
                            "Izbriši" -> "erase"
                            else -> "grid"
                        },
                        Modifier.size(if (condensedTool) 23.dp else 29.dp)
                            .drawBehind {
                                if (enabled) {
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    drawCircle(Brush.radialGradient(
                                        listOf(accent.copy(alpha = .35f), Color.Transparent),
                                        center = center, radius = size.width * .80f,
                                    ), center = center, radius = size.width * .80f)
                                }
                            },
                        if (enabled) Ink.muted else Ink.muted.copy(alpha = .65f),
                    )
                    Spacer(Modifier.height(if (condensedTool) 1.dp else 3.dp))
                    Text(name, color = Ink.white,
                        fontSize = if (condensedTool) 9.sp else 13.sp,
                        maxLines = 1)
                }
            }
        }
        if (badge != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(19.dp)
                    .background(Ink.blue, RoundedCornerShape(50))
                    .border(.8.dp, Ink.cyan, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge, color = Ink.white, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** Five uniform keys per row on 9×9, with the last four centered. */
@Composable
internal fun NumberRow(
    values: List<Int>, height: androidx.compose.ui.unit.Dp,
    enabled: Boolean, columns: Int, onClick: (Int) -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val gap = 6.dp
        val keyWidth = (maxWidth - gap * (columns - 1)) / columns
        val keyFontSize = when {
            keyWidth < 42.dp -> 20.sp
            keyWidth < 56.dp -> 25.sp
            else -> 29.sp
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        ) {
            values.forEach { value ->
                Box(
                    Modifier.width(keyWidth).testTag("number-$value")
                        .alpha(if (enabled) 1f else .50f)
                        .semantics { contentDescription = "Unesi broj $value"; role = Role.Button }
                        .heightIn(min = 48.dp).height(height)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(10.dp),
                            ambientColor = Ink.blue.copy(alpha = .43f),
                            spotColor = Ink.cyan.copy(alpha = .19f),
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF103773), Color(0xFF0B2B63), Color(0xFF092452))
                            )
                        )
                        .drawBehind {
                            val sheenHeight = this.size.height * .52f
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    listOf(Color(0x5562BEFF), Color.Transparent),
                                    startY = 0f, endY = sheenHeight,
                                ),
                                size = androidx.compose.ui.geometry.Size(this.size.width, sheenHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()),
                            )
                        }
                        .border(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFF5CABFF), Color(0xFF1665D7)),
                            ),
                            RoundedCornerShape(10.dp),
                        )
                        .clickable(enabled = enabled) { onClick(value) },
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(value.toString(), color = Ink.white,
                            fontSize = keyFontSize, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
