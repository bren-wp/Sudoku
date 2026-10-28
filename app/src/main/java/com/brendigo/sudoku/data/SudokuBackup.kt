package com.brendigo.sudoku.data

import android.content.Context
import android.content.SharedPreferences
import com.brendigo.sudoku.engine.SudokuEngine
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.LocalDate
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Explicit, completely offline, authenticated backups. No network, account or
 * storage permissions: the user chooses the destination through Android SAF.
 *
 * Files contain the COMPLETE puzzle solution as well as player progress.
 * Do not expose the plaintext, password or decryption failures in logs.
 */
object SudokuBackup {
    private const val PREFERENCES = "sudoku_state_v1"
    private val HEADER = "BRENDIGO-SUDOKU-BACKUP-1\n".toByteArray(StandardCharsets.US_ASCII)
    private const val SALT_LENGTH = 16
    private const val NONCE_LENGTH = 12
    private const val PBKDF2_ITERATIONS = 240_000
    private const val MAX_FILE = 1_000_000
    private const val MAX_PLAIN = 600_000
    private val keys = listOf(
        "game", "regular_game", "daily_game", "undo", "regular_undo",
        "daily_undo", "options", "stats", "stats_backup",
    )

    private fun preferences(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun snapshot(context: Context): Map<String, String> {
        val prefs = preferences(context)
        return keys.mapNotNull { key ->
            prefs.getString(key, null)?.let { key to it }
        }.toMap()
    }

    private fun requirePassword(password: CharArray) {
        require(password.size in 12..128) {
            "Lozinka mora imati od 12 do 128 znakova."
        }
    }

    private fun derive(password: CharArray, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, 256)
        return try {
            val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
            try {
                SecretKeySpec(key, "AES")
            } finally {
                key.fill(0)
            }
        } finally {
            spec.clearPassword()
        }
    }

    fun encrypt(values: Map<String, String>, password: CharArray): ByteArray {
        requirePassword(password)
        val plain = JSONObject().apply {
            put("format", 1)
            put("state", JSONObject().apply {
                values.forEach { (key, value) ->
                    require(key in keys) { "Nepoznato polje kopije." }
                    put(key, value)
                }
            })
        }.toString().toByteArray(StandardCharsets.UTF_8)
        require(plain.size <= MAX_PLAIN) { "Sigurnosna kopija je prevelika." }
        validate(values)
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val nonce = ByteArray(NONCE_LENGTH).also { SecureRandom().nextBytes(it) }
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, derive(password, salt),
                GCMParameterSpec(128, nonce))
            cipher.updateAAD(HEADER)
            val encrypted = cipher.doFinal(plain)
            HEADER + salt + nonce + encrypted
        } finally {
            plain.fill(0)
        }
    }

    /** Decrypt and validate *before* any SharedPreferences mutation. */
    fun decrypt(archive: ByteArray, password: CharArray): Map<String, String> {
        requirePassword(password)
        if (archive.size > MAX_FILE ||
            archive.size < HEADER.size + SALT_LENGTH + NONCE_LENGTH + 16 ||
            !archive.copyOfRange(0, HEADER.size).contentEquals(HEADER)) {
            throw IOException("Datoteka nije podržana Sudoku sigurnosna kopija.")
        }
        val salt = archive.copyOfRange(HEADER.size, HEADER.size + SALT_LENGTH)
        val nonce = archive.copyOfRange(
            HEADER.size + SALT_LENGTH, HEADER.size + SALT_LENGTH + NONCE_LENGTH
        )
        val cipherText = archive.copyOfRange(
            HEADER.size + SALT_LENGTH + NONCE_LENGTH, archive.size
        )
        val plain = try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, derive(password, salt),
                GCMParameterSpec(128, nonce))
            cipher.updateAAD(HEADER)
            cipher.doFinal(cipherText)
        } catch (_: AEADBadTagException) {
            throw IOException("Pogrešna lozinka ili oštećena sigurnosna kopija.")
        } catch (_: java.security.GeneralSecurityException) {
            throw IOException("Sigurnosnu kopiju nije moguće dešifrirati.")
        }
        return try {
            if (plain.size > MAX_PLAIN) throw IOException("Kopija je prevelika.")
            val root = JSONObject(String(plain, StandardCharsets.UTF_8))
            if (root.getInt("format") != 1) throw IOException("Nepodržana verzija kopije.")
            val data = root.getJSONObject("state")
            val values = mutableMapOf<String, String>()
            val names = data.keys()
            while (names.hasNext()) {
                val key = names.next()
                if (key !in keys) throw IOException("Nepoznat zapis u kopiji.")
                values[key] = data.getString(key)
            }
            validate(values)
            values
        } catch (_: org.json.JSONException) {
            throw IOException("Sigurnosna kopija nije valjana.")
        } finally {
            plain.fill(0)
        }
    }

    /** Imported data completely replaces *only* known Sudoku keys on approval. */
    fun restore(context: Context, values: Map<String, String>) {
        validate(values)
        val prefs = preferences(context)
        val editor = prefs.edit()
        for (key in keys) editor.remove(key)
        values.forEach { (key, value) -> editor.putString(key, value) }
        if (!editor.commit()) throw IOException("Nije uspjelo spremanje sigurnosne kopije.")
    }

    private fun validate(values: Map<String, String>) {
        if (values.keys.any { it !in keys }) throw IOException("Nepoznati podaci u kopiji.")
        if (values.values.sumOf { it.length } > MAX_PLAIN)
            throw IOException("Sigurnosna kopija je prevelika.")
        try {
            listOf("game", "regular_game", "daily_game").forEach { key ->
                values[key]?.let { raw -> validateGame(JSONObject(raw), key) }
            }
            listOf("undo", "regular_undo", "daily_undo").forEach { key ->
                values[key]?.let { raw ->
                    val moves = JSONArray(raw)
                    if (moves.length() > 100) throw IOException("Preduga Undo povijest.")
                    val gameKey = when (key) {
                        "regular_undo" -> "regular_game"
                        "daily_undo" -> "daily_game"
                        else -> "game"
                    }
                    val board = values[gameKey]?.let { JSONObject(it) }
                    if (board == null && moves.length() > 0)
                        throw IOException("Undo povijest nema pripadajuću partiju.")
                    val size = board?.getInt("size") ?: 9
                    val givens = board?.getJSONArray("givens")
                    for (i in 0 until moves.length()) {
                        val item = moves.getJSONObject(i)
                        val cell = item.getInt("cell")
                        val notes = item.getInt("notes")
                        val value = item.getInt("value")
                        if (cell !in 0 until size * size || value !in 0..size ||
                            notes < 0 || notes and ((1 shl (size + 1)) - 2).inv() != 0 ||
                            givens?.getInt(cell) != 0
                        ) throw IOException("Nevaljan Undo zapis.")
                    }
                }
            }
            values["stats"]?.let { validateStats(JSONObject(it)) }
            values["stats_backup"]?.let { validateStats(JSONObject(it)) }
            values["options"]?.let {
                val options = JSONObject(it)
                if (options.optString("language", "hr") !in setOf("hr"))
                    throw IOException("Nepodržan jezik u kopiji.")
            }
        } catch (_: org.json.JSONException) {
            throw IOException("Nevaljani podaci u sigurnosnoj kopiji.")
        }
    }

    private fun validateGame(game: JSONObject, key: String) {
        val size = game.getInt("size")
        if (size !in setOf(4, 6, 9)) throw IOException("Nevaljana Sudoku mreža.")
        val br = if (size == 9) 3 else 2
        val bc = size / br
        if (game.getInt("boxRows") != br || game.getInt("boxColumns") != bc ||
            game.getInt("level") !in 0..5 ||
            game.optInt("selected", -1) !in -1 until size * size ||
            game.optInt("elapsed") < 0 ||
            game.optInt("mistakes") !in 0..3 ||
            game.optInt("hints") !in 0..SudokuController.MAX_HINTS) {
            throw IOException("Nevaljane postavke spremljene igre.")
        }
        fun board(name: String): IntArray {
            val array = game.getJSONArray(name)
            if (array.length() != size * size) throw IOException("Nevaljana duljina mreže.")
            return IntArray(size * size) { array.getInt(it) }
        }
        val solution = board("solution")
        val givens = board("givens")
        val current = board("board")
        val notes = game.optJSONArray("notes") ?: JSONArray(List(size * size) { 0 })
        if (notes.length() != size * size ||
            !SudokuEngine.isValidCompleted(solution, size, br, bc) ||
            givens.none { it != 0 } ||
            givens.indices.any { i ->
                (givens[i] != 0 && (givens[i] != solution[i] || current[i] != givens[i])) ||
                    current[i] !in 0..size ||
                    notes.getInt(i) < 0 ||
                    notes.getInt(i) and ((1 shl (size + 1)) - 2).inv() != 0
            }
        ) throw IOException("Sudoku igra nije valjana.")
        if (game.optBoolean("completed") && !current.contentEquals(solution))
            throw IOException("Dovršena igra nema valjano rješenje.")
        val daily = game.optBoolean("daily")
        if ((key == "daily_game" && !daily) || (key == "regular_game" && daily))
            throw IOException("Pogrešna vrsta spremljene igre.")
        if (daily) {
            val day = runCatching { LocalDate.parse(game.getString("day")) }.getOrNull()
            if (day == null) throw IOException("Nevaljan datum dnevne igre.")
        }
    }

    private fun validateStats(stats: JSONObject) {
        for (key in listOf("played", "finished", "flawless", "dailyWins", "dailyPoints",
            "streak", "bestStreak", "noHintWins", "fastWins", "longWins")) {
            if (stats.optInt(key) < 0) throw IOException("Nevaljana statistika.")
        }
        for (key in listOf("totalSeconds", "totalPlayedSeconds")) {
            if (stats.optLong(key) < 0) throw IOException("Nevaljano vrijeme statistike.")
        }
        val history = stats.optJSONArray("history")
        if (history != null && history.length() > 50)
            throw IOException("Prevelika povijest igara.")
        val dates = stats.optJSONArray("completedDailyDates")
        if (dates != null && dates.length() > 4000)
            throw IOException("Prevelika povijest dnevnih izazova.")
        for (i in 0 until (dates?.length() ?: 0)) {
            if (runCatching { LocalDate.parse(dates!!.getString(i)) }.isFailure)
                throw IOException("Nevaljan datum u statistici.")
        }
        for (key in listOf("winsBySize", "levels")) {
            val array = stats.optJSONArray(key) ?: continue
            if (array.length() != if (key == "levels") 6 else 3 ||
                (0 until array.length()).any { array.getInt(it) < 0 }) {
                throw IOException("Nevaljane vrijednosti statistike.")
            }
        }
    }
}
