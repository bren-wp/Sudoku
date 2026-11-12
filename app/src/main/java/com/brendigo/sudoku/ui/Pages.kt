package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.Locale

/**
 * Refresh the visible calendar date at local midnight, and within one minute
 * after manual clock/time-zone changes, without invalidating the UI each tick.
 */
@Composable
internal fun rememberToday(): LocalDate {
    val today by produceState(initialValue = LocalDate.now()) {
        while (true) {
            delay(DailyDateTicker.delayUntilDayRefresh(ZonedDateTime.now()))
            value = LocalDate.now()
        }
    }
    return today
}

@Composable
fun PageContent(
    topPadding: androidx.compose.ui.unit.Dp = 28.dp,
    footerText: String = "V I Š E   O D   I G R E  ·  V J E Ž B A   Z A   U M",
    showFooter: Boolean = true,
    horizontalPadding: androidx.compose.ui.unit.Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Footer occupies its own layout slot below the scroll viewport.
    // Keeping it outside the scrolling content prevents overlays on cards,
    // dialogs, settings actions and the "show all achievements" control.
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding)
                .padding(top = topPadding, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            content()
        }
        if (showFooter) Text(
            footerText,
            color = Ink.muted, fontSize = 9.sp, letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp)
                .testTag("brand-footer"),
            maxLines = 1,
        )
    }
}

@Composable
fun SectionTitle(title: String, description: String = "", onBack: () -> Unit,
                 centered: Boolean = false,
                 titleOffset: androidx.compose.ui.unit.Dp = 0.dp) {
    if (!centered) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).testTag("navigate-back")
                    .semantics { contentDescription = "Natrag"; role = Role.Button }
                    .background(Color(0x99082150), RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BrandIcon("back", Modifier.size(23.dp), Ink.muted)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f).testTag(
                "screen-" + title.lowercase().replace(" ", "-")
            )) {
                AccentText(title, if (title.length > 16) 26 else 30)
                if (description.isNotBlank()) Subtitle(description)
            }
        }
    } else {
        Box(Modifier.fillMaxWidth().heightIn(min = 63.dp)) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 50.dp)
                    .offset(y = titleOffset).testTag("screen-" + title.lowercase().replace(" ", "-"))
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    if (title == "Statistika") {
                        BrandIcon("statistics", Modifier.size(29.dp), Ink.cyan)
                        Spacer(Modifier.width(8.dp))
                    }
                    AccentText(title, if (title == "Dnevni izazov") 46 else if (title.length > 16) 35 else 39)
                }
                if (description.isNotBlank()) {
                    if (title == "Statistika" || title == "Postavke") {
                        Text(
                            description.uppercase(Locale("hr", "HR")),
                            modifier = Modifier.fillMaxWidth(),
                            color = Ink.muted,
                            fontSize = 9.sp,
                            letterSpacing = 3.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    } else if (title == "Dnevni izazov") {
                        Text(description, color = Ink.muted, fontSize = 17.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth())
                    } else {
                        Subtitle(description, Modifier.fillMaxWidth(), centered = true)
                    }
                }
            }
            Box(
                Modifier.size(48.dp).testTag("navigate-back")
                    .semantics { contentDescription = "Natrag"; role = Role.Button }
                    .align(Alignment.CenterStart)
                    .background(Color(0x99082150), RoundedCornerShape(14.dp))
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                BrandIcon("back", Modifier.size(23.dp), Ink.muted)
            }
        }
    }
}

@Composable
fun SolidAction(title: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                enabled: Boolean = true, minHeight: androidx.compose.ui.unit.Dp = 49.dp,
                tag: String = "primary-action") {
    Box(
        modifier.testTag(tag)
            .semantics { contentDescription = title; role = Role.Button }
            .heightIn(min = 48.dp).heightIn(min = minHeight)
            // A small outer cyan/blue halo follows the real interactive
            // pill, without changing its measured layout or tap target.
            .then(if (enabled) Modifier.shadow(
                elevation = 7.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Ink.cyan.copy(alpha = .48f),
                spotColor = Ink.blue.copy(alpha = .65f),
            ) else Modifier)
            .background(
                Brush.horizontalGradient(if (enabled)
                    listOf(Color(0xFF04BFFF), Ink.blue, Color(0xFF0755EE))
                    else listOf(Color.DarkGray, Color.Gray)),
                RoundedCornerShape(30.dp)
            )
            // Raised glass highlight is painted behind the actual Compose
            // play icon and label. It is not a static button screenshot.
            .drawBehind {
                if (enabled) {
                    val topHeight = size.height * .48f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = .24f), Color.Transparent),
                            startY = 0f, endY = topHeight,
                        ),
                        size = androidx.compose.ui.geometry.Size(size.width, topHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(30.dp.toPx()),
                    )
                }
            }
            .border(
                1.4.dp,
                if (enabled) Brush.verticalGradient(listOf(
                    Color(0xFFB1FDFF), Ink.cyan, Color(0xFF087DFA),
                )) else Brush.verticalGradient(listOf(Color.Gray, Color.DarkGray)),
                RoundedCornerShape(30.dp),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            val glyph = when {
                title.trimStart().startsWith("▶") -> "play-filled"
                title.trimStart().startsWith("✓") -> "check"
                else -> null
            }
            if (glyph != null) {
                BrandIcon(glyph, Modifier.size(21.dp), Ink.white)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                title.trimStart().removePrefix("▶").removePrefix("✓").trim(),
                color = Ink.white, fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp, maxLines = 1,
            )
        }
    }
}
