package org.white_powerbank.app

import com.typesafe.config.ConfigFactory

data class Config(
    val botToken: String,
    val dbUrl: String,
    val dbUser: String,
    val dbPassword: String
) {
    companion object {
        fun load(): Config {
            val conf = ConfigFactory.load()
            return Config(
                botToken = System.getenv("BOT_TOKEN") ?: conf.getString("bot.token"),
                dbUrl = System.getenv("DB_URL") ?: conf.getString("db.url"),
                dbUser = System.getenv("DB_USER") ?: conf.getString("db.user"),
                dbPassword = System.getenv("DB_PASSWORD") ?: conf.getString("db.password")
            )
        }
    }
}