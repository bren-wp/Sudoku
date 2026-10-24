package com.brendigo.sudoku.engine

/** Pure Sudoku input and candidates rules used by the UI and unit tests. */
object SudokuCandidates {
    fun possibleMask(board: IntArray, size: Int, boxRows: Int, boxColumns: Int, index: Int): Int {
        require(size in setOf(4, 6, 9) && boxRows * boxColumns == size)
        require(board.size == size * size && index in board.indices)
        if (board[index] != 0) return 0
        val row = index / size
        val col = index % size
        var mask = (1 shl (size + 1)) - 2
        fun exclude(value: Int) {
            if (value in 1..size) mask = mask and (1 shl value).inv()
        }
        for (i in 0 until size) {
            exclude(board[row * size + i])
            exclude(board[i * size + col])
        }
        val r0 = row / boxRows * boxRows
        val c0 = col / boxColumns * boxColumns
        for (r in r0 until r0 + boxRows) for (c in c0 until c0 + boxColumns) {
            exclude(board[r * size + c])
        }
        return mask
    }

    fun automaticNotes(board: IntArray, size: Int, boxRows: Int, boxColumns: Int): IntArray =
        IntArray(board.size) { index ->
            possibleMask(board, size, boxRows, boxColumns, index)
        }

    fun sameHouse(a: Int, b: Int, size: Int, boxRows: Int, boxColumns: Int): Boolean {
        val rowA = a / size
        val rowB = b / size
        val columnA = a % size
        val columnB = b % size
        return rowA == rowB || columnA == columnB ||
            (rowA / boxRows == rowB / boxRows && columnA / boxColumns == columnB / boxColumns)
    }
}
