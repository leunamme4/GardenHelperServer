package app

import com.zaxxer.hikari.HikariDataSource

fun initDatabase(dataSource: HikariDataSource) {
    val connection = dataSource.connection
    connection.use {
        it.createStatement().executeUpdate(
            """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            );
            """.trimIndent()
        )
    }
}