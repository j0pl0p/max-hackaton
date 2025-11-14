package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotStates
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик главного меню
 */
class MainMenuHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotStates): Boolean {
        val text = update.message?.body?.text?.trim()?.lowercase()
        val payload = MessageUtils.getPayload(update)
        
        // Команда /start или кнопка "Главное меню" или "В меню"
        return text == "/start" || 
               text == "start" ||
               payload == "main_menu" ||
               payload == "back_to_menu" ||
               (currentState == BotStates.MAIN_MENU && payload == null && text == null)
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotStates): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        
        // Обработка кнопок главного меню
        when (payload) {
            "main_quit" -> {
                // TODO: вызвать UseCase для начала процесса отказа
                return HandlerResult(
                    text = "Начинаем процесс отказа от курения...",
                    keyboard = Keyboards.backToMenu(),
                    newState = BotStates.QUIT_START
                )
            }
            "main_partner" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotStates.PARTNER_MENU
                )
            }
            "main_diary" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotStates.DIARY_CALENDAR
                )
            }
            "main_statistics" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotStates.STATISTICS
                )
            }
            "main_relapse" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotStates.RELAPSE
                )
            }
        }
        
        // Показываем главное меню
        return HandlerResult(
            text = BotTexts.WELCOME_MESSAGE,
            keyboard = Keyboards.mainMenu(),
            newState = BotStates.MAIN_MENU
        )
    }
}

