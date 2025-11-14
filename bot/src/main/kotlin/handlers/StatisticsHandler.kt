package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.GetStatisticsUseCase
import org.white_powerbank.usecases.GetAchievementsUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

class StatisticsHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.STATISTICS
    }
    
    override suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.STATISTICS
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка")
        return getStatistics(userId)
    }
    
    override suspend fun handleCallback(update: MessageCallbackUpdate, currentState: BotState): HandlerResult {
        val userId = update.callback?.user?.userId ?: return HandlerResult("Ошибка")
        return getStatistics(userId)
    }
    
    private suspend fun getStatistics(userId: Long): HandlerResult {
        val statistics = getStatisticsUseCase.execute(userId)
        val achievements = getAchievementsUseCase.execute(userId)
        
        val statisticsText = buildString {
            appendLine(BotTexts.getStatistics(statistics.lastSmokingDay, statistics.totalDaysWithoutSmoking))
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
