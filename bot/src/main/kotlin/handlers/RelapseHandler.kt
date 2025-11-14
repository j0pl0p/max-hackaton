package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.RestartQuitUseCase
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

/**
 * Обработчик сценария "Сорвался"
 */
class RelapseHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val restartQuitUseCase: RestartQuitUseCase
) : Handler {
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val payload = UpdateUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.RELAPSE || payload?.startsWith("relapse_") == true
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val payload = UpdateUtils.getPayload(update)
        val userId = UpdateUtils.getUserId(update)
        
        return when (payload) {
            "relapse_diary" -> HandlerResult("", null, BotState.DIARY_CALENDAR)
            "relapse_restart" -> {
                userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
                val restarted = restartQuitUseCase.execute(userId)
                HandlerResult(
                    text = if (restarted) "Начинаем заново. Ты справишься!" else "Не удалось начать заново.",
                    keyboard = Keyboards.mainMenu(),
                    newState = BotState.MAIN_MENU
                )
            }
            else -> HandlerResult(
                text = BotTexts.RELAPSE_MESSAGE,
                keyboard = Keyboards.relapse(),
                newState = BotState.RELAPSE
            )
        }
    }
}

