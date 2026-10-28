package com.brendigo.sudoku.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.brendigo.sudoku.data.SudokuBackup
import com.brendigo.sudoku.data.SudokuController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.LocalDate

/** SAF backup UI. No storage permission, cloud or background network service. */
@Composable
fun SudokuBackupFlow(
    controller: SudokuController,
    content: @Composable (onExport: () -> Unit, onImport: () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var showExport by remember { mutableStateOf(false) }
    var showImport by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var imported by remember { mutableStateOf<Map<String, String>?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    val createFile = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        val key = password.toCharArray()
        password = ""
        if (uri == null) {
            key.fill('\u0000')
        } else if (!controller.flush()) {
            key.fill('\u0000')
            controller.showMessage("Nije moguće sigurno spremiti napredak; izvoz je otkazan.")
        } else {
            busy = true
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        try {
                            val bytes = SudokuBackup.encrypt(SudokuBackup.snapshot(context), key)
                            context.contentResolver.openOutputStream(uri, "w")?.use {
                                it.write(bytes)
                                it.flush()
                            } ?: throw IOException("Datoteku nije moguće spremiti.")
                        } finally {
                            key.fill('\u0000')
                        }
                    }
                    controller.showMessage("Šifrirana sigurnosna kopija uspješno je spremljena.")
                } catch (_: Exception) {
                    controller.showMessage("Izvoz nije uspio. Provjeri slobodan prostor i pokušaj ponovno.")
                } finally {
                    busy = false
                }
            }
        }
    }

    val openFile = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        val key = importPassword.toCharArray()
        importPassword = ""
        if (uri == null) {
            key.fill('\u0000')
        } else {
            busy = true
            scope.launch {
                try {
                    val restored = withContext(Dispatchers.IO) {
                        try {
                            val stream = context.contentResolver.openInputStream(uri)
                                ?: throw IOException("Datoteku nije moguće otvoriti.")
                            val bytes = stream.use { input ->
                                val output = ByteArrayOutputStream()
                                val buffer = ByteArray(8192)
                                while (true) {
                                    val count = input.read(buffer)
                                    if (count < 0) break
                                    if (output.size() + count > 1_000_000) {
                                        throw IOException("Datoteka je prevelika.")
                                    }
                                    output.write(buffer, 0, count)
                                }
                                output.toByteArray()
                            }
                            SudokuBackup.decrypt(bytes, key)
                        } finally {
                            key.fill('\u0000')
                        }
                    }
                    imported = restored
                    showConfirm = true
                } catch (_: Exception) {
                    controller.showMessage("Uvoz nije uspio: provjeri lozinku i valjanost kopije.")
                } finally {
                    busy = false
                }
            }
        }
    }

    content(
        { if (!busy) { password = ""; showExport = true } },
        { if (!busy) { importPassword = ""; showImport = true } },
    )

    if (showExport) {
        AlertDialog(
            onDismissRequest = { if (!busy) { password = ""; showExport = false } },
            containerColor = Ink.navy,
            title = { Text("Izvezi sigurnosnu kopiju", color = Ink.white) },
            text = {
                Column {
                    Text("Napredak, partije i postavke šifriraju se lozinkom. " +
                        "Bez lozinke nije moguće vratiti podatke; čuvaj je odvojeno.",
                        color = Ink.muted)
                    OutlinedTextField(
                        value = password, onValueChange = { if (it.length <= 128) password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lozinka (najmanje 12 znakova)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy && password.length in 12..128,
                    onClick = {
                        showExport = false
                        createFile.launch("Sudoku-Brendigo-${LocalDate.now()}.sudoku")
                    },
                ) { Text("Odaberi mjesto spremanja", color = Ink.cyan) }
            },
            dismissButton = {
                TextButton(onClick = { password = ""; showExport = false }) {
                    Text("Odustani", color = Ink.muted)
                }
            },
        )
    }

    if (showImport) {
        AlertDialog(
            onDismissRequest = { if (!busy) { importPassword = ""; showImport = false } },
            containerColor = Ink.navy,
            title = { Text("Uvezi sigurnosnu kopiju", color = Ink.white) },
            text = {
                Column {
                    Text("Odaberi šifriranu .sudoku datoteku. Prije zamjene " +
                        "lokalnog napretka zatražit ćemo tvoju potvrdu.",
                        color = Ink.muted)
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { if (it.length <= 128) importPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Lozinka sigurnosne kopije") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy && importPassword.length in 12..128,
                    onClick = {
                        controller.flush()
                        showImport = false
                        openFile.launch(arrayOf("*/*"))
                    },
                ) { Text("Odaberi datoteku", color = Ink.cyan) }
            },
            dismissButton = {
                TextButton(onClick = { importPassword = ""; showImport = false }) {
                    Text("Odustani", color = Ink.muted)
                }
            },
        )
    }

    if (showConfirm && imported != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false; imported = null },
            containerColor = Ink.navy,
            title = { Text("Zamijeniti trenutačni napredak?", color = Ink.white) },
            text = {
                Text("Kopija je uspješno provjerena i dešifrirana. " +
                    "Uvoz će ZAMIJENITI tvoje lokalne igre, statistiku i postavke. " +
                    "Ovaj postupak nije moguće poništiti. Preporučujemo da " +
                    "prvo izvezeš kopiju postojećeg stanja.", color = Ink.muted)
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        val values = imported ?: return@TextButton
                        val activity = context as? Activity ?: run {
                            controller.showMessage("Vraćanje podataka zahtijeva aktivan zaslon aplikacije.")
                            return@TextButton
                        }
                        // Lock the old controller synchronously BEFORE any disk write;
                        // Activity.onStop can be called while SAF/IO is running.
                        if (!controller.prepareForBackupRestore()) {
                            controller.showMessage("Spremanje trenutačne igre nije uspjelo. Uvoz je otkazan.")
                            return@TextButton
                        }
                        busy = true
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    SudokuBackup.restore(context, values)
                                }
                                imported = null
                                showConfirm = false
                                activity.recreate()
                            } catch (_: Exception) {
                                controller.cancelBackupRestore()
                                controller.showMessage("Vraćanje kopije nije uspjelo. Provjeri dostupnu pohranu.")
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) { Text("Zamijeni podatke", color = Ink.gold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false; imported = null }) {
                    Text("Zadrži sadašnji napredak", color = Ink.cyan)
                }
            },
        )
    }
}
