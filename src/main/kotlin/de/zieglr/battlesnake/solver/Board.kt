package de.zieglr.battlesnake.solver

import de.zieglr.battlesnake.Direction

data class Board(
    val width: Int,
    val height: Int,
    val food: MutableSet<Int>,
    val mySnake: BattleSnake,
    val otherSnake: BattleSnake,
) {
    // left, top, right, down
    val moves = arrayOf(-1, width, 1, -width)
    val corners = arrayOf(0, width - 1, width * height - 1, width * (height - 1))
    val size = width * height

    fun deepCopy(): Board {
        return Board(
            width,
            height,
            food.toMutableSet(),
            mySnake.deepCopy(),
            otherSnake.deepCopy(),
        )
    }

    fun moveToDirection(move: Int): Direction {
        return when (move) {
            -1 -> {
                Direction.LEFT
            }
            width -> {
                Direction.UP
            }
            1 -> {
                Direction.RIGHT
            }
            else -> {
                Direction.DOWN
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Board) {
            return false
        }

        return width == other.width && height == other.height && food == other.food &&
                mySnake == other.mySnake && otherSnake == other.otherSnake
    }
}
