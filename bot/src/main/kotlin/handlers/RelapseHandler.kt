package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.RestartQuitUseCase
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик сценария "Сорвался"
 */
class RelapseHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val restartQuitUseCase: RestartQuitUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") {
            return false
        }
        return currentState == BotState.RELAPSE ||
               payload?.startsWith("relapse_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        
        when (payload) {
            "relapse_diary" -> {
                // Переход к дневнику
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.DIARY_CALENDAR
                )
            }
            "relapse_restart" -> {
                val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
                val restarted = restartQuitUseCase.execute(userId)
                return HandlerResult(
                    text = if (restarted) "Начинаем заново. Ты справишься!" else "Не удалось начать заново.",
                    keyboard = Keyboards.mainMenu(),
                    newState = BotState.MAIN_MENU
                )
            }
        }
        
        // Показываем сообщение о срыве
        return HandlerResult(
            text = BotTexts.RELAPSE_MESSAGE,
            keyboard = Keyboards.relapse(),
            newState = BotState.RELAPSE
        )
    }
}

