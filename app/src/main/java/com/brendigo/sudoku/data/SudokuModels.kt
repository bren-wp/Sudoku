package com.brendigo.sudoku.data

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
    val totalSeconds: Long = 0L, // legacy: time spent in completed puzzles
    val totalPlayedSeconds: Long = 0L, // all gameplay, including unfinished games
    val best: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastDaily: String = "",
    val flawless: Int = 0,
    val winsByLevel: List<Int> = List(6) { 0 },
    val history: List<Completed> = emptyList(),
    val dailyWins: Int = 0,
    val dailyPoints: Int = 0,
    val completedDailyDates: List<String> = emptyList(),
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
