package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotStates
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик сценария "Сорвался"
 */
class RelapseHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotStates): Boolean {
        val payload = MessageUtils.getPayload(update)
        return currentState == BotStates.RELAPSE ||
               payload?.startsWith("relapse_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotStates): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        
        when (payload) {
            "relapse_diary" -> {
                // Переход к дневнику
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotStates.DIARY_CALENDAR
                )
            }
            "relapse_restart" -> {
                // TODO: вызвать UseCase для начала заново
                return HandlerResult(
                    text = "Начинаем заново. Ты справишься!",
                    keyboard = Keyboards.mainMenu(),
                    newState = BotStates.MAIN_MENU
                )
            }
        }
        
        // Показываем сообщение о срыве
        return HandlerResult(
            text = BotTexts.RELAPSE_MESSAGE,
            keyboard = Keyboards.relapse(),
            newState = BotStates.RELAPSE
        )
    }
}

