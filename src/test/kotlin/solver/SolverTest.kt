package solver

import de.zieglr.battlesnake.solver.BattleSnake
import de.zieglr.battlesnake.solver.Board
import de.zieglr.battlesnake.solver.Solver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverTest {

    @Test
    fun shouldGoUp() {
        val board = Board(
            width = 8,
            height = 8,
            food = mutableSetOf(55, 62, 3, 6),
            mySnake = BattleSnake(
                health = 81,
                body = intArrayOf(
                    39,
                    38,
                    37,
                    45,
                    53,
                    52,
                    44,
                    43,
                    42,
                    41,
                    33,
                    25,
                    17,
                    9,
                    8,
                    16,
                    24,
                    32,
                    40,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                ),
                headIndex = 0,
                tailIndex = 19,
                size = 19
            ),
            otherSnake = BattleSnake(
                health = 100,
                body = intArrayOf(18, 19, 20, 28, 29, 30, 22, 23, 15, 14, 13, 5, 5, 0, 0, 0, 0, 0, 0),
                headIndex = 0,
                tailIndex = 13,
                size = 13
            )
        )

        val boardCopy = board.deepCopy()
        val resultOpt = Solver(450).doMinMax(board, 4)
        assertEquals(8, resultOpt.second)
        assertEquals(boardCopy, board)
    }

    @Test
    fun shouldGoDown() {
        val board = Board(
            width = 8,
            height = 8,
            food = mutableSetOf(10, 0),
            mySnake = BattleSnake(
                health = 93,
                body = intArrayOf(61, 60, 59, 58, 57, 49, 48, 0, 0, 0, 0),
                headIndex = 0,
                tailIndex = 7,
                size = 7
            ),
            otherSnake = BattleSnake(
                health = 79,
                body = intArrayOf(47, 55, 54, 46, 38, 39, 0, 0, 0, 0),
                headIndex = 0,
                tailIndex = 6,
                size = 6
            )
        )

        val boardCopy = board.deepCopy()
        val result = Solver(450).doMinMax(board, 15)
        assertEquals(-8, result.second)
        assertEquals(boardCopy, board)
    }

    @Test
    fun shouldGoToFood() {
        val board = Board(
            width = 11,
            height = 11,
            food = mutableSetOf(16, 115, 107, 87, 82),
            mySnake = BattleSnake(
                health = 2,
                body = intArrayOf(88, 89, 90, 91, 0, 0, 0, 0, 0, 0, 0),
                headIndex = 0,
                tailIndex = 3,
                size = 4
            ),
            otherSnake = BattleSnake(
                health = 96,
                body = intArrayOf(
                    6,
                    7,
                    8,
                    9,
                    10,
                    21,
                    20,
                    19,
                    18,
                    17,
                    16,
                    15,
                    14,
                    13,
                    2,
                    1,
                    0,
                    11,
                    12,
                    23,
                    34,
                    35,
                    36,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0
                ),
                headIndex = 0,
                tailIndex = 22,
                size = 23
            )
        )

        val boardCopy = board.deepCopy()
        val result = Solver(450).solve(board)
        assertEquals(-11, result)
        assertEquals(boardCopy, board)
    }

}
