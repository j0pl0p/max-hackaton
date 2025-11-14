package org.white_powerbank.app

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.white_powerbank.app.di.appModule
import org.white_powerbank.bot.MaxBotService
import org.white_powerbank.db.DatabaseFactory

fun main() {
    val config = Config.load()
    
    startKoin {
        modules(appModule)
        properties(mapOf(
            "bot.token" to config.botToken
        ))
    }
    
    DatabaseFactory.init(config.dbUrl, config.dbUser, config.dbPassword)
    
    val app = App()
    app.start()
}

class App : KoinComponent {
    private val botService: MaxBotService by inject()
    
    fun start() {
        println("Starting StreetImpact bot...")
        botService.start()
        println("Bot is running. Press Ctrl+C to stop.")
        
        // Keep the application running
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down bot...")
            botService.stop()
        })
        
        // Block main thread
        Thread.currentThread().join()
    }
}