package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик сценария "Статистика"
 */
class StatisticsHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        return currentState == BotState.STATISTICS
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        // TODO: вызвать UseCase для получения статистики
        // Пока используем заглушку
        val lastDay = 28 // TODO: получить из UseCase
        val totalDays = 14 // TODO: получить из UseCase
        
        // TODO: получить достижения через UseCase
        val achievements = listOf<String>() // TODO: получить из UseCase
        
        val statisticsText = buildString {
            appendLine(BotTexts.getStatistics(lastDay, totalDays))
            if (achievements.isNotEmpty()) {
                appendLine()
                appendLine(BotTexts.ACHIEVEMENTS_TITLE)
                achievements.forEach { achievement ->
                    appendLine("• $achievement")
                }
            } else {
                appendLine()
                appendLine("Пока нет достижений")
            }
        }
        
        return HandlerResult(
            text = statisticsText,
            keyboard = Keyboards.statistics(),
            newState = BotState.STATISTICS
        )
    }
}

