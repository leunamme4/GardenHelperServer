package app

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import app.routes.apiRoutes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun main() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${System.getenv("DB_HOST")}:${System.getenv("DB_PORT") ?: "5432"}/${System.getenv("DB_NAME")}"
        username = System.getenv("DB_USER")
        password = System.getenv("DB_PASSWORD")
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)


    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        apiRoutes(dataSource)
    }.start(wait = true)
}
