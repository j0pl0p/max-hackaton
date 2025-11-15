package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.GetStatisticsUseCase
import org.white_powerbank.usecases.GetAchievementsUseCase
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

/**
 * Обработчик сценария "Статистика"
 */
class StatisticsHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val getStatisticsUseCase: GetStatisticsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : Handler {
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val payload = UpdateUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.STATISTICS || payload == "statistics_awards" || currentState == BotState.AWARDS
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val userId = UpdateUtils.getUserId(update) ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val payload = UpdateUtils.getPayload(update)
        
        // Обработка кнопки "Мои награды"
        if (payload == "statistics_awards" || currentState == BotState.AWARDS) {
            return HandlerResult(
                text = BotTexts.NO_AWARDS_MESSAGE,
                keyboard = Keyboards.awards(),
                newState = BotState.AWARDS
            )
        }
        
        // Получаем статистику через UseCase
        val statistics = getStatisticsUseCase.execute(userId)
        
        val statisticsText = BotTexts.getStatistics(statistics.currentStreak, statistics.maxStreak)
        
        return HandlerResult(
            text = statisticsText,
            keyboard = Keyboards.statistics(),
            newState = BotState.STATISTICS
        )
    }
}

