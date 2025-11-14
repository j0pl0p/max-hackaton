package org.white_powerbank.app

import org.white_powerbank.app.di.appModule
import org.white_powerbank.bot.MaxBotService
import org.white_powerbank.db.DatabaseFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin

fun main() {
    val config = Config.load()
    
    startKoin {
        modules(appModule)
        properties(mapOf("bot.token" to config.botToken))
    }
    
    DatabaseFactory.init(config.dbUrl, config.dbUser, config.dbPassword)
    
    val app = App()
    app.start(config.botToken)
}

class App : KoinComponent {
    private val botService: MaxBotService by inject()
    
    fun start(token: String) {
        println("Starting StreetImpact bot...")
        botService.start()
        println("Bot is running. Press Ctrl+C to stop.")
        
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down bot...")
            botService.stop()
        })
        
        // Keep the application running
        Thread.currentThread().join()
    }
}