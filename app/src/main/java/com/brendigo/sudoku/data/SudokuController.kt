package com.brendigo.sudoku.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.brendigo.sudoku.engine.SudokuEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

enum class Screen { HOME, NEW_GAME, GAME, DAILY, STATS, ACHIEVEMENTS, SETTINGS }

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
)

data class Statistics(
    val played: Int = 0,
    val finished: Int = 0,
    val totalSeconds: Long = 0L,
    val best: Int = 0,
    val streak: Int = 0,
    val lastDaily: String = "",
    val flawless: Int = 0,
    val winsByLevel: List<Int> = List(6) { 0 },
    val history: List<Completed> = emptyList(),
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
    val emptyCells: Int get() = board.count { it == 0 || board.indexOf(it) < 0 }
    fun correctCount(): Int = board.indices.count { board[it] == solution[it] }
    fun isEditable(index: Int): Boolean = index in board.indices && givens[index] == 0
}

private data class Move(val cell: Int, val oldValue: Int, val oldNotes: Int)

class SudokuController(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("sudoku_state_v1", Context.MODE_PRIVATE)
    var page by mutableStateOf(Screen.HOME)
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
    private val undoHistory = ArrayDeque<Move>()

    fun go(destination: Screen) {
        if (page == Screen.GAME && destination != Screen.GAME) saveGame()
        page = destination
        toast = ""
    }

    suspend fun startNew(size: Int, level: Int, daily: Boolean = false) {
        if (isGenerating) return
        val date = LocalDate.now().toString()
        if (daily && game?.daily == true && game?.day == date && game?.completed == false && game?.failed == false) {
            page = Screen.GAME
            return
        }
        isGenerating = true
        try {
            val result = withContext(Dispatchers.Default) {
                SudokuEngine.create(size, level, if (daily) LocalDate.now().toEpochDay().toInt() else kotlin.random.Random.nextInt())
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
                notes = IntArray(size * size),
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
        undoHistory.addLast(Move(cell, s.board[cell], s.notes[cell]))
        if (undoHistory.size > 100) undoHistory.removeFirst()
        val board = s.board.clone()
        val notes = s.notes.clone()
        var errors = s.mistakes
        if (s.notesMode) {
            notes[cell] = notes[cell] xor (1 shl number)
        } else {
            board[cell] = number
            notes[cell] = 0
            if (options.checkMistakes && s.solution[cell] != number && s.board[cell] != number) errors++
            if (options.autoNotes && s.solution[cell] == number) {
                removePeerNotes(notes, s, cell, number)
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
        undoHistory.addLast(Move(cell, s.board[cell], s.notes[cell]))
        val board = s.board.clone()
        val notes = s.notes.clone()
        board[cell] = 0
        notes[cell] = 0
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
        game = s.copy(board = board, notes = notes, selected = previous.cell)
        saveGame()
    }

    fun hint() {
        val s = game ?: return
        if (s.paused || s.completed || s.failed) return
        val cell = s.board.indices.firstOrNull {
            s.givens[it] == 0 && s.board[it] != s.solution[it]
        } ?: return
        undoHistory.addLast(Move(cell, s.board[cell], s.notes[cell]))
        val board = s.board.clone()
        val notes = s.notes.clone()
        board[cell] = s.solution[cell]
        notes[cell] = 0
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

    fun setOptions(next: Options) {
        options = next
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

    private fun removePeerNotes(notes: IntArray, s: Session, cell: Int, value: Int) {
        val row = cell / s.size
        val col = cell % s.size
        for (i in notes.indices) {
            val r = i / s.size
            val c = i % s.size
            if (r == row || c == col || (r / s.boxRows == row / s.boxRows && c / s.boxColumns == col / s.boxColumns)) {
                notes[i] = notes[i] and (1 shl value).inv()
            }
        }
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
        stats = stats.copy(
            finished = stats.finished + 1,
            totalSeconds = stats.totalSeconds + s.elapsed,
            best = if (stats.best == 0) s.elapsed else minOf(stats.best, s.elapsed),
            streak = streak,
            lastDaily = lastDaily,
            flawless = stats.flawless + if (s.mistakes == 0) 1 else 0,
            winsByLevel = levels,
            history = (listOf(Completed(s.level, s.size, s.elapsed, today.toString())) + stats.history).take(30),
        )
        saveStats()
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
            lastDaily = j.optString("lastDaily", ""),
            flawless = j.optInt("flawless"),
            winsByLevel = List(6) { levels?.optInt(it) ?: 0 },
            history = if (history == null) emptyList() else (0 until history.length()).mapNotNull { i ->
                history.optJSONObject(i)?.let { entry ->
                    Completed(entry.optInt("level"), entry.optInt("size", 9), entry.optInt("seconds"), entry.optString("date"))
                }
            },
        )
    } catch (_: Exception) { Statistics() }

    private fun saveStats() {
        prefs.edit().putString("stats", JSONObject().apply {
            put("played", stats.played)
            put("finished", stats.finished)
            put("totalSeconds", stats.totalSeconds)
            put("best", stats.best)
            put("streak", stats.streak)
            put("lastDaily", stats.lastDaily)
            put("flawless", stats.flawless)
            put("levels", JSONArray(stats.winsByLevel))
            put("history", JSONArray().apply {
                stats.history.forEach { entry ->
                    put(JSONObject().apply {
                        put("level", entry.level)
                        put("size", entry.size)
                        put("seconds", entry.seconds)
                        put("date", entry.date)
                    })
                }
            })
        }.toString()).apply()
    }

    private fun saveGame() {
        val s = game ?: return
        prefs.edit().putString("game", JSONObject().apply {
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
        }.toString()).apply()
    }

    private fun readGame(): Session? = try {
        val raw = prefs.getString("game", null) ?: return null
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
        ).takeIf { it.boxRows * it.boxColumns == size && it.level in 0..5 }
    } catch (_: Exception) { null }
}
