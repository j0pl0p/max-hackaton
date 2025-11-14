package org.white_powerbank.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.*

object DatabaseFactory {

    fun init(jdbcUrl: String, user: String, password: String) {
        try {
            val config = HikariConfig().apply {
                this.jdbcUrl = jdbcUrl
                username = user
                this.password = password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 20
                minimumIdle = 5
                connectionTimeout = 30000
                idleTimeout = 600000
                maxLifetime = 1800000
                validate()
            }

            val dataSource = HikariDataSource(config)
            Database.connect(dataSource)

            transaction {
                SchemaUtils.createMissingTablesAndColumns(
                    UsersTable, NotesTable, AwardsTable, UsersAwardsTable
                )
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize database: ${e.message}", e)
        }
    }
}