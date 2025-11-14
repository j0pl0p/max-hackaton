package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик главного меню
 */
class MainMenuHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val text = update.message?.body?.text?.trim()?.lowercase()
        val payload = MessageUtils.getPayload(update)
        
        // Команда /start или кнопка "В меню"
        return text == "/start" || 
               text == "start" ||
               payload == "back_to_menu" ||
               (currentState == BotState.MAIN_MENU && payload == null && text == null)
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        
        // Обработка кнопок главного меню
        when (payload) {
            "main_quit" -> {
                // TODO: вызвать UseCase для начала процесса отказа
                return HandlerResult(
                    text = "Начинаем процесс отказа от курения...",
                    keyboard = Keyboards.backToMenu(),
                    newState = BotState.QUIT_START
                )
            }
            "main_partner" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.PARTNER_MENU
                )
            }
            "main_diary" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.DIARY_CALENDAR
                )
            }
            "main_statistics" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.STATISTICS
                )
            }
            "main_relapse" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.RELAPSE
                )
            }
        }
        
        // Показываем главное меню
        return HandlerResult(
            text = BotTexts.WELCOME_MESSAGE,
            keyboard = Keyboards.mainMenu(),
            newState = BotState.MAIN_MENU
        )
    }
}

