package de.zieglr.battlesnake.solver

data class BattleSnake(
    var health: Int,
    val body: IntArray,
    var headIndex: Int,
    var tailIndex: Int,
    var size: Int,
) {
    fun deepCopy(): BattleSnake {
        return BattleSnake(
            health,
            body.copyOf(),
            headIndex,
            tailIndex,
            size
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BattleSnake) {
            return false
        }

        if (health != other.health || headIndex != other.headIndex || tailIndex != other.tailIndex || size != other.size || body.size != other.body.size) {
            return false
        }

        for (i in 0 until other.size - 1) {
            val index = (other.headIndex + i) % other.body.size
            if (body[index] != other.body[index]) {
                return false
            }
        }

        return true
    }

}
