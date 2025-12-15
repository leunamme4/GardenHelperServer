package app.routes

import app.dto.LoginRequest
import app.dto.RegisterRequest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.SQLException

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

        post("/register") {
            val req = call.receive<RegisterRequest>()

            if (req.password != req.confirmPassword) {
                call.respond(HttpStatusCode.BadRequest, "Passwords do not match")
                return@post
            }

            val connection = dataSource.connection
            connection.use {
                try {
                    val stmt = it.prepareStatement(
                        "INSERT INTO users (email, password) VALUES (?, ?)"
                    )
                    stmt.setString(1, req.email)
                    stmt.setString(2, req.password)
                    stmt.executeUpdate()

                    call.respond(HttpStatusCode.Created, "User registered successfully")
                } catch (e: SQLException) {
                    call.respond(HttpStatusCode.Conflict, "User with this email already exists")
                }
            }
        }

        post("/login") {
            val req = call.receive<LoginRequest>()

            val connection = dataSource.connection
            connection.use {
                val stmt = it.prepareStatement(
                    "SELECT id, password FROM users WHERE email = ?"
                )
                stmt.setString(1, req.email)

                val rs = stmt.executeQuery()
                if (rs.next()) {
                    val storedPassword = rs.getString("password")
                    if (storedPassword == req.password) {
                        val token = JWT.create()
                            .withIssuer("ktor-app")
                            .withClaim("email", req.email)
                            .sign(Algorithm.HMAC256("super-secret"))

                        call.respond(mapOf("token" to token))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid email or password")
                }
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal!!.payload.getClaim("email").asString()
                call.respond(mapOf("email" to email))
            }
        }
    }
}

fun Application.configureSecurity() {
    val jwtSecret = "super-secret" // лучше вынести в env
    val issuer = "ktor-app"

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("email").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
