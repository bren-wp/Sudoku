package com.brendigo.sudoku.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brendigo.sudoku.engine.SudokuEngine
import com.brendigo.sudoku.engine.SudokuCandidates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

enum class Screen { SPLASH, HOME, NEW_GAME, GAME, DAILY, STATS, ACHIEVEMENTS, SETTINGS }

data class Options(
    val dark: Boolean = true,
    val animations: Boolean = true,
    val autoNotes: Boolean = false,
    val checkMistakes: Boolean = true,
    val highlightEquals: Boolean = true,
    val sound: Boolean = false,
    val vibration: Boolean = false,
    val language: String = "hr",
)

data class Completed(
    val level: Int,
    val size: Int,
    val seconds: Int,
    val date: String,
    val daily: Boolean = false,
    val hints: Int = 0,
)

data class Statistics(
    val played: Int = 0,
    val finished: Int = 0,
    val totalSeconds: Long = 0L,
    val best: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDaily: String = "",
    val flawless: Int = 0,
    val winsByLevel: List<Int> = List(6) { 0 },
    val history: List<Completed> = emptyList(),
    val dailyWins: Int = 0,
    val dailyPoints: Int = 0,
    val noHintWins: Int = 0,
    val fastWins: Int = 0,
    val longWins: Int = 0,
    val winsBySize: List<Int> = List(3) { 0 },
)

data class Session(
    val size: Int,
    val boxRows: Int,
    val boxColumns: Int,
    val level: Int,
    val solution: IntArray,
    val givens: IntArray,
    val board: IntArray,
    val notes: IntArray,
    val selected: Int = -1,
    val elapsed: Int = 0,
    val mistakes: Int = 0,
    val hints: Int = 0,
    val notesMode: Boolean = false,
    val paused: Boolean = false,
    val daily: Boolean = false,
    val day: String = "",
    val completed: Boolean = false,
    val failed: Boolean = false,
) {
    val emptyCells: Int get() = board.count { it == 0 }
    fun correctCount(): Int = board.indices.count { board[it] == solution[it] }
    fun isEditable(index: Int): Boolean = index in board.indices && givens[index] == 0
}

private data class Move(val cell: Int, val oldValue: Int, val oldNotes: Int)

class SudokuController(context: Context) {
    companion object { const val MAX_HINTS = 3 }
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sudoku_state_v1", Context.MODE_PRIVATE)
    var page by mutableStateOf(Screen.SPLASH)
        private set
    var game by mutableStateOf(readGame())
        private set
    var stats by mutableStateOf(readStats())
        private set
    var options by mutableStateOf(readOptions())
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var toast by mutableStateOf("")
        private set
    private val undoHistory = readUndoHistory()
    private var settingsOrigin = Screen.HOME

    init {
        // Migrate the in-progress classic game created by versions <= 1.0.3
        // before switching between a normal game and the daily challenge.
        if (game?.daily == false) saveGame()
    }

    fun hasSavedRegularGame(): Boolean {
        val regular = readGame("regular_game") ?: return false
        return !regular.daily && !regular.completed && !regular.failed
    }

    fun resumeRegularGame(): Boolean {
        val saved = readGame("regular_game") ?: return false
        if (saved.daily || saved.completed || saved.failed) return false
        game = saved
        undoHistory.clear()
        undoHistory.addAll(readUndoHistory("regular_undo", saved))
        page = Screen.GAME
        saveGame()
        return true
    }

    fun go(destination: Screen) {
        if (page == Screen.GAME && destination != Screen.GAME) saveGame()
        if (destination == Screen.SETTINGS) {
            settingsOrigin = if (page == Screen.GAME) Screen.GAME else Screen.HOME
        }
        page = destination
        toast = ""
    }

    fun back() {
        if (page == Screen.SETTINGS && settingsOrigin == Screen.GAME) {
            page = Screen.GAME
        } else {
            go(Screen.HOME)
        }
    }

    /** Persist a just-entered move when Android moves the activity into background. */
    fun flush() {
        saveGame()
    }

    fun clearToast() {
        toast = ""
    }

    suspend fun startNew(size: Int, level: Int, daily: Boolean = false) {
        if (isGenerating) return
        val today = LocalDate.now()
        val date = today.toString()
        if (daily && dailyAlreadyCompleted()) {
            toast = "Današnji izazov već je završen. Vrati se sutra!"
            page = Screen.DAILY
            return
        }
        if (daily && game?.daily == true && game?.day == date && game?.completed == false && game?.failed == false) {
            page = Screen.GAME
            return
        }
        if (daily) {
            val saved = readGame("daily_game")
            if (saved != null && saved.daily && saved.day == date && !saved.completed && !saved.failed) {
                game = saved
                undoHistory.clear()
                undoHistory.addAll(readUndoHistory("daily_undo", saved))
                page = Screen.GAME
                saveGame()
                return
            }
        }
        isGenerating = true
        try {
            val result = withContext(Dispatchers.Default) {
                SudokuEngine.create(size, level, if (daily) today.toEpochDay().toInt() else kotlin.random.Random.nextInt())
            }
            undoHistory.clear()
            game = Session(
                size = result.size,
                boxRows = result.boxRows,
                boxColumns = result.boxColumns,
                level = level,
                solution = result.solution,
                givens = result.givens,
                board = result.givens.clone(),
                notes = if (options.autoNotes) SudokuCandidates.automaticNotes(
                    result.givens, size, result.boxRows, result.boxColumns
                ) else IntArray(size * size),
                daily = daily,
                day = if (daily) date else "",
            )
            stats = stats.copy(played = stats.played + 1)
            saveStats()
            saveGame()
            page = Screen.GAME
        } catch (error: Exception) {
            toast = "Igru nije moguće pokrenuti. Pokušaj ponovno."
        } finally {
            isGenerating = false
        }
    }

    fun select(index: Int) {
        val session = game ?: return
        if (index in session.board.indices && !session.paused && !session.completed && !session.failed) {
            game = session.copy(selected = index)
        }
    }

    fun toggleNotes() {
        val session = game ?: return
        game = session.copy(notesMode = !session.notesMode)
        saveGame()
    }

    fun enter(number: Int) {
        val s = game ?: return
        val cell = s.selected
        if (!s.isEditable(cell) || s.paused || s.failed || s.completed || number !in 1..s.size) return
        if (s.notesMode && s.board[cell] != 0) return
        if (!s.notesMode && s.board[cell] == number) return
        rememberMove(s, cell)
        val board = s.board.clone()
        val notes = s.notes.clone()
        var errors = s.mistakes
        if (s.notesMode) {
            notes[cell] = notes[cell] xor (1 shl number)
        } else {
            board[cell] = number
            notes[cell] = 0
            if (options.checkMistakes && s.solution[cell] != number && s.board[cell] != number) errors++
            if (options.autoNotes) {
                val refreshed = SudokuCandidates.automaticNotes(
                    board, s.size, s.boxRows, s.boxColumns
                )
                refreshed.copyInto(notes)
            }
        }
        val finished = !s.notesMode && board.contentEquals(s.solution)
        val failed = options.checkMistakes && errors >= 3
        game = s.copy(board = board, notes = notes, mistakes = errors, completed = finished, failed = failed)
        if (finished) recordWin(game!!)
        saveGame()
    }

    fun erase() {
        val s = game ?: return
        val cell = s.selected
        if (!s.isEditable(cell) || s.paused || s.completed || s.failed) return
        if (s.board[cell] == 0 && s.notes[cell] == 0) return
        rememberMove(s, cell)
        val board = s.board.clone()
        val notes = s.notes.clone()
        board[cell] = 0
        notes[cell] = 0
        if (options.autoNotes) SudokuCandidates.automaticNotes(
            board, s.size, s.boxRows, s.boxColumns
        ).copyInto(notes)
        game = s.copy(board = board, notes = notes)
        saveGame()
    }

    fun undo() {
        val s = game ?: return
        if (undoHistory.isEmpty() || s.paused || s.completed || s.failed) return
        val previous = undoHistory.removeLast()
        val board = s.board.clone()
        val notes = s.notes.clone()
        board[previous.cell] = previous.oldValue
        notes[previous.cell] = previous.oldNotes
        if (options.autoNotes) SudokuCandidates.automaticNotes(
            board, s.size, s.boxRows, s.boxColumns
        ).copyInto(notes)
        game = s.copy(board = board, notes = notes, selected = previous.cell)
        saveGame()
    }

    fun hint() {
        val s = game ?: return
        if (s.paused || s.completed || s.failed) return
        if (s.hints >= MAX_HINTS) {
            toast = "Iskorištena su sva tri savjeta."
            return
        }
        val cell = s.selected.takeIf { it in s.board.indices &&
            s.isEditable(it) && s.board[it] != s.solution[it]
        } ?: s.board.indices.firstOrNull {
            s.isEditable(it) && s.board[it] != s.solution[it]
        } ?: return
        rememberMove(s, cell)
        val board = s.board.clone()
        val notes = s.notes.clone()
        board[cell] = s.solution[cell]
        notes[cell] = 0
        if (options.autoNotes) SudokuCandidates.automaticNotes(
            board, s.size, s.boxRows, s.boxColumns
        ).copyInto(notes)
        val finished = board.contentEquals(s.solution)
        game = s.copy(board = board, notes = notes, selected = cell, hints = s.hints + 1, completed = finished)
        if (finished) recordWin(game!!)
        saveGame()
    }

    fun togglePause() {
        val s = game ?: return
        if (!s.completed && !s.failed) {
            game = s.copy(paused = !s.paused)
            saveGame()
        }
    }

    fun tick() {
        val s = game ?: return
        if (page != Screen.GAME || s.paused || s.completed || s.failed) return
        game = s.copy(elapsed = s.elapsed + 1)
        if (game!!.elapsed % 10 == 0) saveGame()
    }

    fun updateOptions(next: Options) {
        val old = options
        options = next
        if (next.autoNotes && !old.autoNotes) {
            game?.let { s ->
                game = s.copy(notes = SudokuCandidates.automaticNotes(
                    s.board, s.size, s.boxRows, s.boxColumns
                ))
                saveGame()
            }
        }
        prefs.edit().putString("options", JSONObject().apply {
            put("dark", next.dark)
            put("animations", next.animations)
            put("autoNotes", next.autoNotes)
            put("checkMistakes", next.checkMistakes)
            put("highlightEquals", next.highlightEquals)
            put("sound", next.sound)
            put("vibration", next.vibration)
            put("language", next.language)
        }.toString()).apply()
    }

    private fun rememberMove(session: Session, cell: Int) {
        undoHistory.addLast(Move(cell, session.board[cell], session.notes[cell]))
        if (undoHistory.size > 100) undoHistory.removeFirst()
    }

    private fun readUndoHistory(key: String = "undo", session: Session? = game): ArrayDeque<Move> {
        val saved = ArrayDeque<Move>()
        try {
            val entries = JSONArray(prefs.getString(key, "[]"))
            val s = session ?: return saved
            for (i in 0 until entries.length()) {
                val j = entries.optJSONObject(i) ?: continue
                val cell = j.optInt("cell", -1)
                val value = j.optInt("value", -1)
                val notes = j.optInt("notes", 0)
                if (s.isEditable(cell) && value in 0..s.size && notes >= 0) {
                    saved.addLast(Move(cell, value, notes))
                    if (saved.size > 100) saved.removeFirst()
                }
            }
        } catch (_: Exception) {
            saved.clear()
        }
        return saved
    }

    private fun recordWin(s: Session) {
        val today = LocalDate.now()
        var streak = stats.streak
        var lastDaily = stats.lastDaily
        if (s.daily && stats.lastDaily != today.toString()) {
            streak = if (stats.lastDaily == today.minusDays(1).toString()) stats.streak + 1 else 1
            lastDaily = today.toString()
        }
        val levels = stats.winsByLevel.toMutableList()
        levels[s.level]++
        val sizes = stats.winsBySize.toMutableList()
        sizes[listOf(4, 6, 9).indexOf(s.size)]++
        val newDailyWin = s.daily && stats.lastDaily != today.toString()
        stats = stats.copy(
            finished = stats.finished + 1,
            totalSeconds = stats.totalSeconds + s.elapsed,
            best = if (stats.best == 0) s.elapsed else minOf(stats.best, s.elapsed),
            streak = streak,
            bestStreak = maxOf(stats.bestStreak, streak),
            lastDaily = lastDaily,
            flawless = stats.flawless + if (s.mistakes == 0) 1 else 0,
            winsByLevel = levels,
            dailyWins = stats.dailyWins + if (newDailyWin) 1 else 0,
            dailyPoints = stats.dailyPoints + if (newDailyWin) 50 else 0,
            noHintWins = stats.noHintWins + if (s.hints == 0) 1 else 0,
            fastWins = stats.fastWins + if (s.size == 9 && s.elapsed < 300) 1 else 0,
            longWins = stats.longWins + if (s.elapsed >= 3600) 1 else 0,
            winsBySize = sizes,
            history = (listOf(Completed(s.level, s.size, s.elapsed,
                today.toString(), s.daily, s.hints)) + stats.history).take(50),
        )
        saveStats()
    }

    /** Current active streak, unlike the lifetime best, expires after a missed day. */
    fun currentStreak(today: LocalDate = LocalDate.now()): Int {
        val latest = runCatching { LocalDate.parse(stats.lastDaily) }.getOrNull() ?: return 0
        return if (latest == today || latest == today.minusDays(1)) stats.streak else 0
    }

    fun dailyAlreadyCompleted(): Boolean = stats.lastDaily == LocalDate.now().toString()

    private fun readOptions(): Options = try {
        val j = JSONObject(prefs.getString("options", "{}") ?: "{}")
        Options(
            j.optBoolean("dark", true), j.optBoolean("animations", true),
            j.optBoolean("autoNotes", false), j.optBoolean("checkMistakes", true),
            j.optBoolean("highlightEquals", true), j.optBoolean("sound", false),
            j.optBoolean("vibration", false), j.optString("language", "hr")
        )
    } catch (_: Exception) { Options() }

    private fun readStats(): Statistics = try {
        val j = JSONObject(prefs.getString("stats", "{}") ?: "{}")
        val levels = j.optJSONArray("levels")
        val history = j.optJSONArray("history")
        Statistics(
            played = j.optInt("played"),
            finished = j.optInt("finished"),
            totalSeconds = j.optLong("totalSeconds"),
            best = j.optInt("best"),
            streak = j.optInt("streak"),
            bestStreak = j.optInt("bestStreak", j.optInt("streak")),
            lastDaily = j.optString("lastDaily", ""),
            flawless = j.optInt("flawless"),
            winsByLevel = List(6) { levels?.optInt(it) ?: 0 },
            history = if (history == null) emptyList() else (0 until history.length()).mapNotNull { i ->
                history.optJSONObject(i)?.let { entry ->
                    Completed(entry.optInt("level").coerceIn(0, 5),
                        entry.optInt("size", 9), entry.optInt("seconds"),
                        entry.optString("date"), entry.optBoolean("daily"), entry.optInt("hints"))
                }
            },
            dailyWins = j.optInt("dailyWins"),
            dailyPoints = j.optInt("dailyPoints", j.optInt("dailyWins", 0) * 50),
            noHintWins = j.optInt("noHintWins"),
            fastWins = j.optInt("fastWins"),
            longWins = j.optInt("longWins"),
            winsBySize = List(3) { j.optJSONArray("winsBySize")?.optInt(it) ?: 0 },
        )
    } catch (_: Exception) { Statistics() }

    private fun saveStats() {
        prefs.edit().putString("stats", JSONObject().apply {
            put("played", stats.played)
            put("finished", stats.finished)
            put("totalSeconds", stats.totalSeconds)
            put("best", stats.best)
            put("streak", stats.streak)
            put("bestStreak", stats.bestStreak)
            put("lastDaily", stats.lastDaily)
            put("flawless", stats.flawless)
            put("dailyWins", stats.dailyWins)
            put("dailyPoints", stats.dailyPoints)
            put("noHintWins", stats.noHintWins)
            put("fastWins", stats.fastWins)
            put("longWins", stats.longWins)
            put("winsBySize", JSONArray(stats.winsBySize))
            put("levels", JSONArray(stats.winsByLevel))
            put("history", JSONArray().apply {
                stats.history.forEach { entry ->
                    put(JSONObject().apply {
                        put("level", entry.level)
                        put("size", entry.size)
                        put("seconds", entry.seconds)
                        put("date", entry.date)
                        put("daily", entry.daily)
                        put("hints", entry.hints)
                    })
                }
            })
        }.toString()).apply()
    }

    private fun saveGame() {
        val s = game ?: return
        val snapshot = JSONObject().apply {
            put("size", s.size)
            put("boxRows", s.boxRows)
            put("boxColumns", s.boxColumns)
            put("level", s.level)
            put("solution", JSONArray(s.solution.toList()))
            put("givens", JSONArray(s.givens.toList()))
            put("board", JSONArray(s.board.toList()))
            put("notes", JSONArray(s.notes.toList()))
            put("selected", s.selected)
            put("elapsed", s.elapsed)
            put("mistakes", s.mistakes)
            put("hints", s.hints)
            put("notesMode", s.notesMode)
            put("paused", s.paused)
            put("daily", s.daily)
            put("day", s.day)
            put("completed", s.completed)
            put("failed", s.failed)
        }.toString()
        val history = JSONArray().apply {
            undoHistory.forEach { move ->
                put(JSONObject().apply {
                    put("cell", move.cell)
                    put("value", move.oldValue)
                    put("notes", move.oldNotes)
                })
            }
        }.toString()
        val editor = prefs.edit().putString("game", snapshot).putString("undo", history)
        if (s.daily) editor.putString("daily_game", snapshot).putString("daily_undo", history)
        else editor.putString("regular_game", snapshot).putString("regular_undo", history)
        editor.apply()
    }

    private fun readGame(key: String = "game"): Session? {
        return try {
        val raw = prefs.getString(key, null) ?: return null
        val j = JSONObject(raw)
        val size = j.getInt("size")
        if (size !in setOf(4, 6, 9)) return null
        fun array(name: String): IntArray {
            val values = j.getJSONArray(name)
            require(values.length() == size * size)
            return IntArray(values.length()) { values.getInt(it) }
        }
        Session(
            size = size, boxRows = j.getInt("boxRows"), boxColumns = j.getInt("boxColumns"),
            level = j.getInt("level"), solution = array("solution"),
            givens = array("givens"), board = array("board"), notes = array("notes"),
            selected = j.optInt("selected", -1),
            elapsed = j.optInt("elapsed"), mistakes = j.optInt("mistakes"),
            hints = j.optInt("hints"), notesMode = j.optBoolean("notesMode"),
            paused = j.optBoolean("paused"), daily = j.optBoolean("daily"),
            day = j.optString("day", ""),
            completed = j.optBoolean("completed"), failed = j.optBoolean("failed"),
        ).takeIf { session ->
            session.boxRows * session.boxColumns == size &&
                session.boxRows == (if (size == 9) 3 else 2) &&
                session.level in 0..5 &&
                session.selected in -1 until size * size &&
                session.elapsed >= 0 && session.mistakes in 0..3 &&
                session.hints in 0..MAX_HINTS &&
                SudokuEngine.isValidCompleted(session.solution, size,
                    session.boxRows, session.boxColumns) &&
                session.givens.indices.all { i ->
                    (session.givens[i] == 0 || session.givens[i] == session.solution[i]) &&
                        session.board[i] in 0..size &&
                        (session.givens[i] == 0 || session.board[i] == session.givens[i]) &&
                        session.notes[i] >= 0
                }
        }
        } catch (_: Exception) { null }
    }
}
