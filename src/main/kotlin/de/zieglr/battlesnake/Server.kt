package de.zieglr.battlesnake

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

const val name = "Parseltongue"
const val author = "ziegler-daniel"

val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun main() {
    println("\"$name\" by $author\n")

    val info = BotInfoDto(
        author = author,
        color = "#00ff00",
        head = "default",
        tail = "default",
        version = "v1.0"
    )

    val port = 8000
    val moveService = MoveService()

    embeddedServer(Netty, port = port) {
        // This is so the server will automatically handle json for us
        install(ContentNegotiation) { json(json) }

        // This exists for statistic purposes
        install(DefaultHeaders) {
            header("Server", "770grappenmaker/starter-snake-kotlin")
        }

        routing {
            get("/") {
                call.respond(info)
            }

            post("/start") {
                val request = call.receive<StartRequest>()
                call.respond(HttpStatusCode.OK)
            }

            post("/end") {
                val request = call.receive<EndRequest>()
                call.respond(HttpStatusCode.OK)
            }

            post("/move") {
                val request = call.receive<MoveRequest>()

                call.respond(
                    MoveResponse(
                        shout = "",
                        move = moveService.decideMove(request)
                    )
                )
            }
        }
    }.start(wait = true)
}

