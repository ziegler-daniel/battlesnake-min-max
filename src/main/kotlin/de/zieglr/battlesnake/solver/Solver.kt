package de.zieglr.battlesnake.solver

import java.util.concurrent.*

class MoveData(
    val previousHealth: Int,
    val previousTail: Int,
)

class Solver constructor(private val timeout: Long) {
    private val noMove = 0
    private val depths = listOf(2, 5, 8, 11, 14, 17)
    private val workerPool: ExecutorService = Executors.newFixedThreadPool(2 * depths.size)
    private val healthThreshold = 15

    fun solve(board: Board): Int {
        println("\nStart")
        println(board)

        val possibleMoves = board.moves.filter { isMovePossible(board, it, board.mySnake, board.otherSnake, true) }

        return when (possibleMoves.size) {
            0 -> {
                println("\tNo possible move found. Return left.")
                -1
            }
            1 -> {
                println("\tOnly one move possible.")
                possibleMoves.first()
            }
            else -> {
                val move = doMinMaxWithIterativeDeepening(board)
                return if (move == noMove) {
                    possibleMoves.first()
                } else {
                    move
                }
            }
        }
    }

    private fun doMinMaxWithIterativeDeepening(board: Board): Int {
        val start = System.currentTimeMillis()
        var bestMove = noMove
        val futures = mutableListOf<Future<Pair<Int, Int>>>()

        depths.forEach {
            futures.add(workerPool.submit<Pair<Int, Int>> { doMinMax(board.deepCopy(), it) })
        }

        for (i in futures.indices) {
            try {
                val remainingTime = kotlin.math.max(0, timeout - (System.currentTimeMillis() - start))
                val result = futures[i].get(remainingTime, TimeUnit.MILLISECONDS)
                if (result.second != noMove) {
                    bestMove = result.second
                }
                println("\tDepth ${depths[i]}: Time ${System.currentTimeMillis() - start} ms, move ${result.second}, score ${result.first}")
            } catch (e: TimeoutException) {
                println("\tDepth ${depths[i]}: Timeout after ${System.currentTimeMillis() - start} ms")
                futures.forEach { it.cancel(true) }
                return bestMove
            } catch (e: Exception) {
                println(e)
            }
        }

        return bestMove
    }

    fun doMinMax(board: Board, maxDepth: Int): Pair<Int, Int> {
        return max(board, maxDepth, Int.MIN_VALUE, Int.MAX_VALUE)
    }

    // my snake
    private fun max(board: Board, remainDepth: Int, alpha: Int, beta: Int): Pair<Int, Int> {
        if (Thread.currentThread().isInterrupted) {
            return Pair(0, noMove)
        }

        if (remainDepth == 0) {
            return Pair(evaluate(board), noMove)
        }

        var bestScore = Int.MIN_VALUE
        var bestMove = 0

        for (m in board.moves) {
            if (isMovePossible(board, m, board.mySnake, board.otherSnake, true)) {
                val moveResult = doMove(board, m, board.mySnake)
                val score = min(board, remainDepth - 1, kotlin.math.max(alpha, bestScore), beta)
                revertMove(board, board.mySnake, moveResult)

                if (score >= bestScore) {
                    bestScore = score
                    bestMove = m
                }

                if (bestScore >= beta) {
                    return Pair(bestScore, m)
                }
            }
        }

        return Pair(bestScore, bestMove)
    }

    // other snake
    private fun min(board: Board, remainDepth: Int, alpha: Int, beta: Int): Int {
        if (Thread.currentThread().isInterrupted) {
            return 0
        }

        var value = Int.MAX_VALUE

        for (m in board.moves) {
            if (isMovePossible(board, m, board.otherSnake, board.mySnake, false)) {
                val moveResult = doMove(board, m, board.otherSnake)

                // check head to head
                if (board.mySnake.body[board.mySnake.headIndex] == board.otherSnake.body[board.otherSnake.headIndex]) {
                    // head to head
                    var mySnakeSize = board.mySnake.size
                    if (board.mySnake.health == 100) {
                        mySnakeSize--
                    }

                    value = if (mySnakeSize < board.otherSnake.size) {
                        // my snake lost
                        kotlin.math.min(value, Int.MIN_VALUE + 10)
                    } else if (mySnakeSize == board.otherSnake.size) {
                        kotlin.math.min(value, -10000)
                    } else {
                        // my snake won
                        kotlin.math.min(value, Int.MAX_VALUE)
                    }
                } else {
                    value = kotlin.math.min(value, max(board, remainDepth, alpha, kotlin.math.min(beta, value)).first)
                }

                revertMove(board, board.otherSnake, moveResult)

                if (value < alpha) {
                    return value
                }
            }
        }

        return value
    }

    private fun doMove(board: Board, move: Int, snake: BattleSnake): MoveData {
        val head = snake.body[snake.headIndex]
        val tail = snake.body[snake.tailIndex]
        val previousHealth = snake.health

        // add new head
        snake.headIndex = if (snake.headIndex == 0) snake.body.size - 1 else snake.headIndex - 1
        snake.body[snake.headIndex] = head + move

        // remove tail
        snake.tailIndex = if (snake.tailIndex == 0) snake.body.size - 1 else snake.tailIndex - 1

        // adjust health
        --snake.health

        // consume possible food
        if (board.food.remove(snake.body[snake.headIndex])) {
            snake.health = 100

            val newTailIndex = if (snake.tailIndex == snake.body.size - 1) 0 else snake.tailIndex + 1
            snake.body[newTailIndex] = snake.body[snake.tailIndex]
            snake.tailIndex = newTailIndex
            ++snake.size
        }

        return MoveData(previousHealth, tail)
    }

    private fun revertMove(board: Board, snake: BattleSnake, moveResult: MoveData) {
        if (snake.health == 100) {
            // remove tail
            snake.tailIndex = if (snake.tailIndex == 0) snake.body.size - 1 else snake.tailIndex - 1
            --snake.size
            board.food.add(snake.body[snake.headIndex])
        }

        // remove head
        snake.headIndex = if (snake.headIndex == snake.body.size - 1) 0 else snake.headIndex + 1
        // add old tail
        snake.tailIndex = if (snake.tailIndex == snake.body.size - 1) 0 else snake.tailIndex + 1
        snake.body[snake.tailIndex] = moveResult.previousTail
        snake.health = moveResult.previousHealth
    }

    private fun isMovePossible(
        board: Board,
        move: Int,
        snake: BattleSnake,
        otherSnake: BattleSnake,
        ignoreHeadToHead: Boolean
    ): Boolean {
        val head = snake.body[snake.headIndex]
        val nextHead = head + move

        // bounds (left, top, right, bottom)
        if ((move == -1 && head % board.width == 0) || nextHead >= board.size || (move == 1 && (head + 1) % board.width == 0) || nextHead < 0) {
            return false
        }

        // collision with own body (ignore tail)
        for (i in 0 until snake.size - 1) {
            if (snake.body[(snake.headIndex + i) % snake.body.size] == nextHead) {
                return false
            }
        }

        // health
        if (snake.health == 1 && !board.food.contains(nextHead)) {
            return false
        }

        // collision with other snake head => checked in min step

        // collision with other snake body (ignore tail)
        val start = if (ignoreHeadToHead) 0 else 1
        for (i in start until otherSnake.size - 1) {
            if (otherSnake.body[(otherSnake.headIndex + i) % otherSnake.body.size] == nextHead) {
                return false
            }
        }

        return true
    }

    private fun evaluate(board: Board): Int {
        val healthPenaltyMySnake = healthThreshold - kotlin.math.min(board.mySnake.health, healthThreshold)
        val healthPenaltyOtherSnake = healthThreshold - kotlin.math.min(board.otherSnake.health, healthThreshold)

        val possibleMovesMySnake =
            board.moves.count { isMovePossible(board, it, board.mySnake, board.otherSnake, false) }
        val possibleMovesOtherSnake =
            board.moves.count { isMovePossible(board, it, board.otherSnake, board.mySnake, false) }

        return 5 * (possibleMovesMySnake - possibleMovesOtherSnake) - healthPenaltyMySnake + healthPenaltyOtherSnake +
                (board.mySnake.size - board.otherSnake.size)
    }
}
