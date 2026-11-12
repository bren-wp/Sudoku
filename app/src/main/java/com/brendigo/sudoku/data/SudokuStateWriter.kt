package com.brendigo.sudoku.data

import android.content.SharedPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * A process-wide, ordered IO writer. Neither JSON serialization nor disk commit()
 * runs on the Android main thread. A single queue also prevents an old Activity
 * from overwriting newer progress after configuration changes.
 *
 * Every queued entry is a detached snapshot; Compose state is never read on IO.
 * Checkpoints await all earlier entries and return the result of their own
 * durable commit (unlike SharedPreferences.apply(), which reports no failure).
 */
internal object SudokuStateWriter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queueLock = Any()

    // Save bursts can otherwise enqueue dozens of expensive fsync() calls
    // during rapid keypad interaction. Keep the latest queued auto-save; never
    // combine two durable checkpoints or replace an in-flight disk commit.
    private class WriteRequest(
        val preferences: SharedPreferences,
        var snapshot: SudokuDiskSnapshot,
        val checkpoint: CompletableDeferred<Boolean>? = null,
    )

    private val queue = ArrayDeque<WriteRequest>()
    private var active: WriteRequest? = null
    private var draining = false

    /**
     * Read only the newest not-yet-written data for a real Activity recreate.
     * The caller opts in explicitly; a fresh controller reads disk instead.
     */
    fun pendingSnapshotFor(preferences: SharedPreferences): SudokuDiskSnapshot? =
        synchronized(queueLock) {
            queue.lastOrNull { it.preferences === preferences }?.snapshot
                ?: active?.takeIf { it.preferences === preferences }?.snapshot
        }

    fun enqueue(preferences: SharedPreferences, snapshot: SudokuDiskSnapshot) {
        synchronized(queueLock) {
            val latest = queue.lastOrNull()
            if (latest != null && latest.checkpoint == null &&
                latest.preferences === preferences) {
                latest.snapshot = snapshot
            } else {
                queue.addLast(WriteRequest(preferences, snapshot))
            }
            startDrainingLocked()
        }
    }

    /**
     * Never conflate an explicit durability barrier with a later auto-save.
     * All older writes finish first and this exact snapshot is committed.
     */
    suspend fun checkpoint(preferences: SharedPreferences, snapshot: SudokuDiskSnapshot): Boolean {
        val completion = CompletableDeferred<Boolean>()
        synchronized(queueLock) {
            queue.addLast(WriteRequest(preferences, snapshot, completion))
            startDrainingLocked()
        }
        return completion.await()
    }

    /** Must only be called while holding queueLock. */
    private fun startDrainingLocked() {
        if (draining) return
        draining = true
        scope.launch {
            while (true) {
                val item = synchronized(queueLock) {
                    if (queue.isEmpty()) {
                        draining = false
                        active = null
                        null
                    } else {
                        queue.removeFirst().also { active = it }
                    }
                } ?: break
                // Individual IO failures must not kill the worker or strand
                // later checkpoints; return false to the waiting operation.
                val saved = runCatching {
                    write(item.preferences, item.snapshot)
                }.getOrDefault(false)
                synchronized(queueLock) {
                    if (active === item) active = null
                }
                item.checkpoint?.complete(saved)
            }
        }
    }

    private fun write(prefs: SharedPreferences, state: SudokuDiskSnapshot): Boolean {
        val editor = prefs.edit()
        editor.putString("options", JSONObject().apply {
            val next = state.options
            put("dark", next.dark)
            put("animations", next.animations)
            put("autoNotes", next.autoNotes)
            put("checkMistakes", next.checkMistakes)
            put("highlightEquals", next.highlightEquals)
            put("sound", next.sound)
            put("vibration", next.vibration)
            put("language", next.language)
        }.toString())

        val stats = state.statistics
        val statsJson = JSONObject().apply {
            put("played", stats.played)
            put("finished", stats.finished)
            put("totalSeconds", stats.totalSeconds)
            put("totalPlayedSeconds", stats.totalPlayedSeconds)
            put("best", stats.best)
            put("streak", stats.streak)
            put("bestStreak", stats.bestStreak)
            put("lastDaily", stats.lastDaily)
            put("flawless", stats.flawless)
            put("dailyWins", stats.dailyWins)
            put("dailyPoints", stats.dailyPoints)
            put("completedDailyDates", JSONArray(stats.completedDailyDates))
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
        }.toString()
        // Both redundant stats keys, the game and its undo data share one
        // atomic commit. A crashed process cannot award a win separately.
        editor.putString("stats", statsJson).putString("stats_backup", statsJson)

        state.session?.let { game ->
            val gameJson = JSONObject().apply {
                put("size", game.size)
                put("boxRows", game.boxRows)
                put("boxColumns", game.boxColumns)
                put("level", game.level)
                put("solution", JSONArray(game.solution.toList()))
                put("givens", JSONArray(game.givens.toList()))
                put("board", JSONArray(game.board.toList()))
                put("notes", JSONArray(game.notes.toList()))
                put("selected", game.selected)
                put("elapsed", game.elapsed)
                put("mistakes", game.mistakes)
                put("hints", game.hints)
                put("notesMode", game.notesMode)
                put("paused", game.paused)
                put("daily", game.daily)
                put("day", game.day)
                put("completed", game.completed)
                put("failed", game.failed)
            }.toString()
            val undoJson = JSONArray().apply {
                state.moves.forEach { move ->
                    put(JSONObject().apply {
                        put("cell", move.cell)
                        put("value", move.oldValue)
                        put("notes", move.oldNotes)
                    })
                }
            }.toString()
            editor.putString("game", gameJson).putString("undo", undoJson)
            if (game.daily) {
                editor.putString("daily_game", gameJson).putString("daily_undo", undoJson)
            } else {
                editor.putString("regular_game", gameJson).putString("regular_undo", undoJson)
            }
        }
        return editor.commit()
    }
}

internal data class SudokuDiskMove(val cell: Int, val oldValue: Int, val oldNotes: Int)

internal data class SudokuDiskSnapshot(
    val session: Session?,
    val statistics: Statistics,
    val options: Options,
    val moves: List<SudokuDiskMove>,
)
