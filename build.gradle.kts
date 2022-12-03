plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("io.ktor.plugin") version "2.2.1"
    application
}

group = "de.zieglr"
version = "1.0.0"

val mainName = "de.zieglr.battlesnake.ServerKt"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.junit.jupiter:junit-jupiter:5.9.0")

    // Ktor dependencies
    serverImplementation("core", "netty", "content-negotiation", "default-headers")
    ktorImplementation("serialization-kotlinx-json")

    implementation("ch.qos.logback:logback-classic:1.4.5")
}

// Utility functions for declaring dependencies
// This can be simplified by typing out the full Ktor module names,
// but because of the modular structure of Ktor, this will probably work a little bit better for most people
fun DependencyHandlerScope.serverImplementation(vararg names: String) =
    names.forEach { ktorImplementation("server-$it") }

fun DependencyHandlerScope.ktorImplementation(name: String) = implementation(
    group = "io.ktor",
    name = "ktor-$name",
    version = "2.0.1"
)

// Declares the creation of a "fat jar"
tasks {
    withType<Jar>().configureEach {
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest { attributes("Main-Class" to mainName) }
    }
}


// Allows you to run using gradlew
// Specifies the main class we specified earlier
application {
    mainClass.set(mainName)
}

tasks.test {
    useJUnitPlatform()
}

ktor {
    docker {
        localImageName.set("battlesnake-min-max")
        imageTag.set("1.0.0")
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                80,
                8000,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}
