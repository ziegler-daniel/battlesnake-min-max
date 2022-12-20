package de.zieglr.battlesnake

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This file contains data structures that you get from the api
// You can read more about the api here: https://docs.battlesnake.com/references/api

@Serializable
data class BotInfoDto(
    @SerialName("apiversion") val apiVersion: String = "1",
    val author: String,
    val color: String,
    val head: String,
    val tail: String,
    val version: String
)

@Serializable
data class StartRequest(
    val game: GameDto,
    val turn: Int,
    val board: BoardDto,
    val you: BattleSnakeDto
)

typealias MoveRequest = StartRequest

@Serializable
data class MoveResponse(
    val shout: String?,
    val move: Direction
)

typealias EndRequest = StartRequest

@Serializable
data class GameDto(
    val id: String,
    val ruleset: RulesetDto,
    val map: String? = null,
    val timeout: Long,
    val source: GameSource
)

@Serializable
data class RulesetDto(
    val name: String,
    val version: String,
    val settings: RulesetSettings
)

@Serializable
data class RulesetSettings(
    val foodSpawnChance: Int,
    val minimumFood: Int,
    val hazardDamagePerTurn: Int,
    val royale: RoyaleSettingsDto?,
    val squad: SquadSettingsDto?
)

@Serializable
data class SquadSettingsDto(
    val allowBodyCollisions: Boolean,
    val sharedElimination: Boolean,
    val sharedHealth: Boolean,
    val sharedLength: Boolean
)

@Serializable
data class RoyaleSettingsDto(val shrinkEveryNTurns: Int)

@Serializable
enum class GameSource {
    @SerialName("tournament") TOURNAMENT,
    @SerialName("league") LEAGUE,
    @SerialName("arena") ARENA,
    @SerialName("challenge") CHALLENGE,
    @SerialName("custom") CUSTOM,
    @SerialName("ladder") LADDER,
    @SerialName("") UNKNOWN
}

@Serializable
data class BoardDto(
    val height: Int,
    val width: Int,
    val food: List<Position>,
    val hazards: List<Position>,
    val snakes: List<BattleSnakeDto>
)

@Serializable
data class BattleSnakeDto(
    val id: String,
    val name: String,
    val health: Int,
    val body: List<Position>,
    val latency: String,
    val head: Position,
    val length: Int,
    val shout: String,
    val squad: String,
    val customizations: SnakeCustomizationDto
)

@Serializable
data class SnakeCustomizationDto(
    val color: String,
    val head: String,
    val tail: String
)
