package app.routes

import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.apiRoutes(dataSource: HikariDataSource) {
    routing {
        get("/") {
            val connection = dataSource.connection
            connection.use {
                val rs = it.createStatement().executeQuery("SELECT 1")
                rs.next()
                val result = rs.getInt(1)
                call.respondText("Ktor + Postgres работает! Test query result: $result")
            }
        }
    }
}
