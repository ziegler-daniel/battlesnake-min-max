package de.zieglr.battlesnake

import de.zieglr.battlesnake.solver.*

class MoveService {
    private val solver = Solver(getTimeout())

    fun decideMove(request: MoveRequest): Direction {
        if (request.board.snakes.size <= 1) {
            return Direction.LEFT
        }

        val otherSnake = request.board.snakes.first { it.id != request.you.id }
        val board = mapBoard(request.board, request.you, otherSnake)
        val move = solver.solve(board)

        return board.moveToDirection(move)
    }

    private fun mapBoard(boardDto: BoardDto, mySnake: BattleSnakeDto, otherSnake: BattleSnakeDto): Board {
        return Board(
            width = boardDto.width,
            height = boardDto.height,
            food = boardDto.food.map { mapPosition(it, boardDto.width) }.toMutableSet(),
            mySnake = mapSnake(mySnake, boardDto),
            otherSnake = mapSnake(otherSnake, boardDto),
        )
    }

    private fun mapSnake(snakeDto: BattleSnakeDto, boardDto: BoardDto): BattleSnake {
        return BattleSnake(
            health = snakeDto.health,
            body = IntArray(snakeDto.body.size + boardDto.food.size + 2) {
                if (it < snakeDto.body.size) mapPosition(
                    snakeDto.body[it],
                    boardDto.width
                ) else 0
            },
            headIndex = 0,
            tailIndex = snakeDto.length - 1,
            size = snakeDto.length
        )
    }

    private fun mapPosition(position: Position, width: Int): Int {
        return position.x + position.y * width
    }

    private fun getTimeout(): Long {
        val env = System.getenv("BATTLESNAKE_TIMEOUT")
        val timeout = if (env != null && env.isNotBlank()) env.toLong() else 450
        println("Use timeout: $timeout ms.")
        return timeout
    }
}
