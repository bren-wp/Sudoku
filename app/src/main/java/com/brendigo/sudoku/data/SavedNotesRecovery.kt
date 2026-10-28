package com.brendigo.sudoku.data

/**
 * Recover optional pencil marks without discarding a valid, in-progress board.
 *
 * Old or interrupted installs may contain missing, truncated or unexpected
 * notes arrays. Notes are auxiliary user data; a malformed notes entry must
 * not make the whole puzzle (including its entered numbers) unreadable.
 * Bits outside the supported 1..size range are never passed to the UI.
 */
internal object SavedNotesRecovery {
    fun recover(size: Int, stored: IntArray?): IntArray {
        require(size in setOf(4, 6, 9))
        val allowedDigits = ((1 shl (size + 1)) - 1) and 0x7FFFFFFE
        return IntArray(size * size) { index ->
            (stored?.getOrNull(index) ?: 0) and allowedDigits
        }
    }
}
