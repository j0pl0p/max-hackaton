package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.StartQuitUseCase
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

/**
 * Обработчик главного меню
 */
class MainMenuHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val startQuitUseCase: StartQuitUseCase
) : Handler {
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val text = UpdateUtils.getText(update)?.trim()?.lowercase()
        val payload = UpdateUtils.getPayload(update)
        
        // Don't handle main_ payloads if already in target state
        if (payload == "main_partner" && currentState == BotState.PARTNER_MENU) return false
        if (payload == "main_diary" && currentState == BotState.DIARY_CALENDAR) return false
        if (payload == "main_statistics" && currentState == BotState.STATISTICS) return false
        if (payload == "main_relapse" && currentState == BotState.RELAPSE) return false
        
        return text == "/start" || text == "start" || payload == "back_to_menu" ||
               payload?.startsWith("main_") == true ||
               (currentState == BotState.MAIN_MENU && payload == null && text == null)
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val payload = UpdateUtils.getPayload(update)
        val userId = UpdateUtils.getUserId(update) ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        // Обработка кнопок главного меню
        return when (payload) {
            "main_quit" -> {
                val started = startQuitUseCase.execute(userId)
                HandlerResult(
                    text = if (started) "Начинаем процесс отказа от курения..." else "Не удалось начать процесс отказа.",
                    keyboard = Keyboards.backToMenu(),
                    newState = BotState.QUIT_START
                )
            }
            "main_partner" -> HandlerResult("", null, BotState.PARTNER_MENU)
            "main_diary" -> HandlerResult("", null, BotState.DIARY_CALENDAR)
            "main_statistics" -> HandlerResult("", null, BotState.STATISTICS)
            "main_relapse" -> HandlerResult("", null, BotState.RELAPSE)
            else -> HandlerResult(
                text = BotTexts.WELCOME_MESSAGE,
                keyboard = Keyboards.mainMenu(),
                newState = BotState.MAIN_MENU
            )
        }
    }
}

