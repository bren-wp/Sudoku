package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.Screen
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.launch

@Composable
fun NewGamePage(controller: SudokuController) {
    val scope = rememberCoroutineScope()
    // Keep the selected board and difficulty across Android activity recreation
    // (rotation, display size change and process-state restoration).
    var size by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableIntStateOf(9)
    }
    var level by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableIntStateOf(2)
    }
    PageContent(topPadding = 22.dp, showFooter = false, horizontalPadding = 21.dp) {
        // Reference hierarchy: back + board size on the same first row,
        // followed by the large title and the six full-width difficulty cards.
        BoxWithConstraints(Modifier.fillMaxWidth().testTag("new-game-size-header")) {
            // 192dp is the reference width. The selector shrinks with the
            // viewport instead of pushing the 48dp Back target off-screen.
            val selectorWidth = minOf(192.dp, (maxWidth - 56.dp).coerceAtLeast(0.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(46.dp).testTag("navigate-back")
                        .semantics { contentDescription = "Natrag"; role = Role.Button }
                        .background(Color(0xB0082150), RoundedCornerShape(12.dp))
                        .border(1.dp, Ink.line.copy(alpha = .80f), RoundedCornerShape(12.dp))
                        .clickable { controller.go(Screen.HOME) },
                    contentAlignment = Alignment.Center,
                ) {
                    BrandIcon("back", Modifier.size(24.dp), Ink.muted)
                }
                NewGameSizeSelector(
                    selectedSize = size,
                    enabled = !controller.isGenerating,
                    onSelect = { size = it },
                    modifier = Modifier.width(selectorWidth),
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            // Keep the 55sp reference heading on full-size phones; fit it to
            // real available width on 240dp devices and at large font scales.
            // This stays live Compose text rather than a scaled bitmap.
            val headingSize = minOf(
                55,
                (maxWidth.value / (5.3f * LocalDensity.current.fontScale))
                    .toInt().coerceAtLeast(20),
            )
            val subtitleSize = if (maxWidth < 300.dp) 17.sp else 21.sp
            Column(Modifier.fillMaxWidth().testTag("screen-nova-igra")
                .padding(start = 5.dp)) {
                // The reference wordmark has a deliberately WHITE "Nova" and
                // an illuminated CYAN "igra". One accessible text node keeps
                // the title's bounds and test semantics intact on every DPI.
                Text(
                    text = buildAnnotatedString {
                        append("Nova ")
                        withStyle(SpanStyle(brush = Brush.horizontalGradient(
                            listOf(Color(0xFF57EDFF), Color(0xFF04C9FF), Color(0xFF1389FA))
                        ))) { append("igra") }
                    },
                    modifier = Modifier.testTag("new-game-title"),
                    color = Ink.white,
                    fontSize = headingSize.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-.5).sp,
                    maxLines = 1,
                )
                Text("Odaberi težinu", color = Ink.muted,
                    fontSize = subtitleSize,
                    modifier = Modifier.testTag("new-game-subtitle"))
            }
        }
        Spacer(Modifier.height(1.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val accessibilityCards = maxWidth < 300.dp ||
                LocalDensity.current.fontScale >= 1.3f
            Column(Modifier.testTag("new-game-difficulty-list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                difficultyLabels.forEachIndexed { i, label ->
                    NewGameDifficultySurface(
                        i,
                        Modifier.fillMaxWidth().testTag("difficulty-$i")
                            .heightIn(min = if (accessibilityCards) 96.dp else 74.dp)
                            .semantics {
                                contentDescription = "Težina $label"
                                role = Role.RadioButton
                                selected = level == i
                            }
                            .clickable(enabled = !controller.isGenerating) { level = i },
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(
                                horizontal = if (accessibilityCards) 12.dp else 18.dp,
                                vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val iconSize = if (accessibilityCards) 41.dp else 47.dp
                            Box(
                                Modifier.size(iconSize)
                                    .shadow(
                                        elevation = if (level == i) 9.dp else 6.dp,
                                        shape = RoundedCornerShape(50),
                                        ambientColor = newGameAccents[i].copy(alpha = .68f),
                                        spotColor = newGameAccents[i].copy(alpha = .72f),
                                    )
                                    // The reference uses a recessed circular
                                    // 3D badge with a bright rim and a softer
                                    // halo behind each difficulty glyph.
                                    .background(
                                        Brush.radialGradient(listOf(
                                            newGameAccents[i].copy(alpha = .34f),
                                            newGameAccents[i].copy(alpha = .13f),
                                            Color(0xEF00132D),
                                        )),
                                        RoundedCornerShape(50),
                                    )
                                    .border(
                                        2.5.dp,
                                        Brush.linearGradient(listOf(
                                            Color.White.copy(alpha = .70f),
                                            newGameAccents[i],
                                            newGameAccents[i].copy(alpha = .78f),
                                        )),
                                        RoundedCornerShape(50),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                BrandIcon(
                                    when (i) {
                                        0 -> "leaf-solid"
                                        1 -> "star-solid"
                                        2 -> "double-star-solid"
                                        3 -> "triple-star-solid"
                                        4 -> "crown-solid"
                                        else -> "crown-royal"
                                    },
                                    Modifier.size(if (accessibilityCards) 25.dp else 28.dp),
                                    newGameAccents[i],
                                )
                            }
                            Spacer(Modifier.width(if (accessibilityCards) 9.dp else 22.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    label,
                                    fontSize = if (accessibilityCards) 17.sp else 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink.white,
                                    maxLines = 2,
                                )
                                Text(
                                    listOf(
                                        "Savršeno za početnike",
                                        "Lagani izazov za vježbu",
                                        "Uravnotežen izazov",
                                        "Za iskusne igrače",
                                        "Ozbiljan izazov",
                                        "Samo za najbolje",
                                    )[i],
                                    color = Ink.muted,
                                    fontSize = if (accessibilityCards) 12.sp else 14.sp,
                                    lineHeight = if (accessibilityCards) 15.sp else 17.sp,
                                    maxLines = if (accessibilityCards) 3 else 2,
                                )
                            }
                            BrandIcon(
                                "next",
                                Modifier.size(if (accessibilityCards) 19.dp else 23.dp),
                                if (level == i) Ink.cyan else newGameAccents[i],
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val fullWidthAction = maxWidth < 290.dp
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                NewGameStartAction(
                    title = if (controller.isGenerating) "Pripremam zagonetku…" else "Započni igru",
                    onClick = { scope.launch { controller.startNew(size, level) } },
                    modifier = Modifier.fillMaxWidth(if (fullWidthAction) 1f else .74f),
                    enabled = !controller.isGenerating,
                )
            }
        }
        if (controller.isGenerating) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally),
            color = Ink.cyan)
    }
}
