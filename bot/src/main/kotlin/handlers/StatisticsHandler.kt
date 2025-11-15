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
        
        // Обработка кнопки "Мои награды" и навигации по наградам
        if (payload == "statistics_awards" || currentState == BotState.AWARDS || payload?.startsWith("awards_") == true) {
            return handleAwards(userId, payload)
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
    
    private suspend fun handleAwards(userId: Long, payload: String?): HandlerResult {
        val page = when {
            payload?.startsWith("awards_next_") == true -> payload.removePrefix("awards_next_").toIntOrNull() ?: 0
            payload?.startsWith("awards_prev_") == true -> payload.removePrefix("awards_prev_").toIntOrNull() ?: 0
            else -> 0
        }
        
        val (awards, hasMore) = getAchievementsUseCase.getAchievementsWithPagination(userId, page)
        
        return if (awards.isEmpty()) {
            HandlerResult(
                text = BotTexts.NO_AWARDS_MESSAGE,
                keyboard = Keyboards.awards(),
                newState = BotState.AWARDS
            )
        } else {
            val award = awards.first()
            val text = "🏆 ${award.name}\n\n${award.description}"
            HandlerResult(
                text = text,
                keyboard = Keyboards.awardsNavigation(page, hasMore, page > 0),
                newState = BotState.AWARDS
            )
        }
    }
}

