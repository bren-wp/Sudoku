package com.brendigo.sudoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brendigo.sudoku.data.SudokuController

@Composable
fun SettingsPage(
    controller: SudokuController,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
) {
    val opts = controller.options
    // Android destroys and recreates Activities for rotation, multi-window,
    // font scale and theme changes. Preserve any open Settings dialog.
    var showPrivacy by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showSound by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showLanguage by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var showAbout by androidx.compose.runtime.saveable.rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    PageContent(topPadding = 27.dp, horizontalPadding = 19.dp) {
        SectionTitle(
            "Postavke", "Prilagodi igru svojim željama",
            centered = true, titleOffset = 5.dp,
            onBack = { controller.back() },
        )
        SettingsSurface(Modifier.fillMaxWidth().testTag("settings-appearance")) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp)) {
                SettingHeader("palette", "Izgled", "Prilagodi vizualni doživljaj igre")
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color(0x8B051637), RoundedCornerShape(11.dp))
                        .border(.5.dp, Ink.blue.copy(alpha = .47f), RoundedCornerShape(11.dp))
                ) {
                    SettingToggle("moon", "Tamna tema",
                        "Koristi tamni izgled aplikacije", opts.dark) {
                        controller.updateOptions(opts.copy(dark = it))
                    }
                    SettingsRule(Modifier.padding(horizontal = 6.dp))
                    SettingToggle("sparkles", "Animacije",
                        "Prikaži animacije i vizualne efekte", opts.animations) {
                        controller.updateOptions(opts.copy(animations = it))
                    }
                }
            }
        }
        SettingsSurface(Modifier.fillMaxWidth().testTag("settings-game")) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp)) {
                SettingHeader("gamepad", "Igra", "Postavke igrivosti i pomoći")
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color(0x8B051637), RoundedCornerShape(11.dp))
                        .border(.5.dp, Ink.blue.copy(alpha = .47f), RoundedCornerShape(11.dp))
                ) {
                    SettingToggle("note", "Automatske bilješke",
                        "Automatski dodaj moguće brojeve u prazna polja", opts.autoNotes) {
                        controller.updateOptions(opts.copy(autoNotes = it))
                    }
                    SettingsRule(Modifier.padding(horizontal = 6.dp))
                    SettingToggle("warning", "Provjera greške",
                        "Odmah označi netočne unose", opts.checkMistakes) {
                        controller.updateOptions(opts.copy(checkMistakes = it))
                    }
                    SettingsRule(Modifier.padding(horizontal = 6.dp))
                    SettingToggle("grid", "Isticanje brojeva",
                        "Naglasi sve iste brojeve na ploči", opts.highlightEquals) {
                        controller.updateOptions(opts.copy(highlightEquals = it))
                    }
                }
            }
        }
        SettingsSurface(
            Modifier.fillMaxWidth().heightIn(min = 55.dp)
                .testTag("sound-vibration")
                .semantics { contentDescription = "Zvuk i vibracija"; role = Role.Button }
                .clickable { showSound = true },
            accent = Ink.blue,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsGlyph("speaker", Ink.purple, size = 36.dp)
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text("Zvuk i vibracija", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = Ink.white)
                    Text("Uključi ili isključi zvučne efekte i vibracije",
                        color = Ink.muted, fontSize = 11.sp,
                        lineHeight = 14.sp, maxLines = 2)
                }
                BrandIcon("next", Modifier.size(22.dp), Ink.muted)
            }
        }
        SettingsSurface(
            Modifier.fillMaxWidth().heightIn(min = 55.dp)
                .testTag("language-settings")
                .semantics {
                    contentDescription = "Jezik aplikacije, hrvatski"
                    role = Role.Button
                }
                .clickable { showLanguage = true },
            // The owner's reference uses a BLUE outer panel border with a
            // turquoise globe glyph, not a fully turquoise panel.
            accent = Ink.blue,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 13.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SettingsGlyph("globe", Ink.teal, size = 36.dp)
                Spacer(Modifier.width(13.dp))
                Column(Modifier.weight(1f)) {
                    Text("Jezik", fontSize = 17.sp, color = Ink.white,
                        fontWeight = FontWeight.Bold)
                    Text("Odaberi jezik aplikacije",
                        color = Ink.muted, fontSize = 11.sp, maxLines = 2)
                }
                Row(
                    Modifier.testTag("settings-language-value")
                        .background(Color(0xB0082452), RoundedCornerShape(10.dp))
                        .border(.8.dp, Ink.blue.copy(alpha = .72f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 11.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Hrvatski", fontSize = 12.sp, color = Ink.muted)
                    Spacer(Modifier.width(12.dp))
                    BrandIcon("down", Modifier.size(17.dp), Ink.muted)
                }
            }
        }
        SettingsSurface(Modifier.fillMaxWidth().testTag("settings-about")) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 5.dp)) {
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 49.dp)
                        .testTag("about-app")
                        .semantics {
                            contentDescription = "O aplikaciji Sudoku by Brendigo"
                            role = Role.Button
                        }
                        .clickable { showAbout = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SettingHeader("info", "O aplikaciji", "Sudoku by Brendigo",
                        Modifier.weight(1f), tint = Ink.blue)
                    BrandIcon("next", Modifier.size(20.dp), Ink.muted)
                }
                SettingsRule()
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        .testTag("privacy-policy")
                        .semantics {
                            contentDescription = "Otvori pravila privatnosti"
                            role = Role.Button
                        }
                        .clickable { showPrivacy = true }
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandIcon("shield", Modifier.size(20.dp), Ink.muted)
                    Spacer(Modifier.width(15.dp))
                    Text("Pravila privatnosti", color = Ink.muted,
                        fontSize = 13.sp, modifier = Modifier.weight(1f))
                    BrandIcon("next", Modifier.size(17.dp), Ink.muted)
                }
                SettingsRule()
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 28.dp)
                        .testTag("settings-version")
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BrandIcon("grid", Modifier.size(19.dp), Ink.muted)
                    Spacer(Modifier.width(15.dp))
                    Text("Verzija " + com.brendigo.sudoku.BuildConfig.VERSION_NAME,
                        color = Ink.muted, fontSize = 12.sp)
                }
            }
        }
        // Offline encrypted backups remain accessible by scrolling below the
        // reference viewport. Keep their real callbacks and persistence.
        SettingsSurface(Modifier.fillMaxWidth().padding(top = 25.dp)
            .testTag("settings-backup"), Ink.teal) {
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingHeader("shield", "Sigurnosna kopija",
                    "Šifrirano spremanje i vraćanje napretka na uređaju", tint = Ink.teal)
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SolidAction("Izvezi kopiju", onExportBackup,
                            Modifier.fillMaxWidth(), minHeight = 48.dp, tag = "backup-export")
                    }
                    Box(Modifier.weight(1f)) {
                        SolidAction("Uvezi kopiju", onImportBackup,
                            Modifier.fillMaxWidth(), minHeight = 48.dp, tag = "backup-import")
                    }
                }
                Subtitle("Za uvoz je potrebna lozinka. Podaci ne napuštaju uređaj " +
                    "osim datoteke koju sami odaberete.")
            }
        }
    }
    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Jezik", color = Ink.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BrandIcon("check", Modifier.size(20.dp), Ink.cyan)
                        Spacer(Modifier.width(8.dp))
                        Text("Hrvatski", color = Ink.cyan, fontWeight = FontWeight.Bold)
                    }
                    Subtitle("Aplikacija trenutačno podržava hrvatski jezik.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguage = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
    if (showSound) {
        AlertDialog(
            onDismissRequest = { showSound = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Zvuk i vibracija", color = Ink.white) },
            text = {
                Column {
                    SettingToggle("speaker", "Zvuk", "Zvučni efekt tijekom igre", opts.sound) {
                        controller.updateOptions(controller.options.copy(sound = it))
                    }
                    SettingToggle("sparkles", "Vibracija", "Vibracija pri unosu broja", opts.vibration) {
                        controller.updateOptions(controller.options.copy(vibration = it))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSound = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            }
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            containerColor = Color(0xFF081A41),
            title = { Text("O aplikaciji", color = Ink.white) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Text("Sudoku by Brendigo", color = Ink.white,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Subtitle("Treniraj mozak svaki dan.")
                    Subtitle("Verzija " + com.brendigo.sudoku.BuildConfig.VERSION_NAME)
                    Subtitle("Igra radi bez registracije i internetske veze.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            containerColor = Color(0xFF081A41),
            title = { Text("Pravila privatnosti", color = Ink.white) },
            text = {
                Column(
                    Modifier.heightIn(max = 370.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Text(
                        "Sudoku by Brendigo možeš igrati bez registracije i internetske veze.",
                        color = Ink.white, fontSize = 14.sp,
                    )
                    Subtitle("Podaci o odigranim igrama, postavkama, napretku i postignućima " +
                        "spremaju se lokalno u privatnu pohranu aplikacije na uređaju.")
                    Subtitle("Aplikacija ne koristi oglase, analitiku, praćenje korisnika niti " +
                        "internetsku dozvolu. Podaci se ne šalju na poslužitelj.")
                    Subtitle("U postavkama možeš ručno izvesti šifriranu sigurnosnu kopiju " +
                        "lozinkom i vratiti je uz svoju potvrdu. Izgubljenu lozinku nije moguće obnoviti.")
                    Subtitle("Deinstalacija bez sigurnosne kopije može trajno ukloniti napredak. " +
                        "Ako odabereš vanjsku/cloud pohranu, njome upravlja odabrani pružatelj.")
                    Subtitle("Izdavač: Brendigo · verzija " +
                        com.brendigo.sudoku.BuildConfig.VERSION_NAME)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) {
                    Text("Zatvori", color = Ink.cyan)
                }
            },
        )
    }
}

@Composable
private fun SettingHeader(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tint: Color = Ink.cyan,
) {
    Row(
        modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsGlyph(icon = icon, tint = tint, size = 36.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 18.sp, color = Ink.white,
                fontWeight = FontWeight.Bold, maxLines = 2)
            Text(subtitle, fontSize = 12.sp, lineHeight = 15.sp,
                color = Ink.muted, maxLines = 2)
        }
    }
}

@Composable
private fun SettingToggle(
    icon: String,
    title: String,
    caption: String,
    checked: Boolean,
    change: (Boolean) -> Unit,
) {
    val tag = "setting-" + title.lowercase().replace(" ", "-")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .testTag(tag)
            .toggleable(value = checked, role = Role.Switch, onValueChange = change)
            .semantics {
                contentDescription = "$title: ${if (checked) "uključeno" else "isključeno"}"
            }
            .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandIcon(icon, Modifier.size(21.dp), Ink.muted)
        Spacer(Modifier.width(17.dp))
        Column(Modifier.weight(1f).padding(end = 7.dp)) {
            Text(
                title,
                fontSize = 14.sp, color = Ink.white,
                fontWeight = FontWeight.SemiBold, maxLines = 2,
                lineHeight = 18.sp,
            )
            Text(
                caption, color = Ink.muted,
                fontSize = 11.sp, lineHeight = 14.sp, maxLines = 3,
            )
        }
        // The entire parent is the single accessible switch and persists
        // state through the existing SudokuController options callbacks.
        Box(
            Modifier.width(48.dp).height(28.dp)
                .background(
                    if (checked) Brush.horizontalGradient(
                        listOf(Color(0xFF00D4F5), Color(0xFF0876F7)),
                    ) else Brush.horizontalGradient(
                        listOf(Color(0xFF223553), Color(0xFF132543)),
                    ),
                    RoundedCornerShape(25.dp),
                )
                .drawBehind {
                    // Glass highlight on the real, state-driven native switch.
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = if (checked) .25f else .09f),
                                Color.Transparent),
                        ),
                        size = androidx.compose.ui.geometry.Size(size.width, size.height * .45f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(25.dp.toPx()),
                    )
                }
                .border(1.dp, if (checked) Ink.cyan else Ink.muted.copy(alpha = .66f),
                    RoundedCornerShape(25.dp)),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Box(
                Modifier.padding(horizontal = 4.dp).size(20.dp)
                    .background(
                        Brush.verticalGradient(listOf(Color.White, Color(0xFFD9ECFF))),
                        RoundedCornerShape(50),
                    )
                    .border(.5.dp, Color(0xFFAAD9FF), RoundedCornerShape(50)),
            )
        }
    }
}
