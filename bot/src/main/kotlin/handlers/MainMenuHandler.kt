package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.StartQuitUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

class MainMenuHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val startQuitUseCase: StartQuitUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val text = update.message?.body?.text?.trim()?.lowercase()
        val payload = MessageUtils.getPayload(update)
        
        if (text == "/start" || text == "start" || payload == "back_to_menu") return true
        
        // Не обрабатываем main_* payload если уже в соответствующем состоянии
        if (payload == "main_partner" && currentState == BotState.PARTNER_MENU) return false
        if (payload == "main_diary" && currentState == BotState.DIARY_CALENDAR) return false
        if (payload == "main_statistics" && currentState == BotState.STATISTICS) return false
        if (payload == "main_relapse" && currentState == BotState.RELAPSE) return false
        
        return payload?.startsWith("main_") == true
    }
    
    override suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        
        if (payload == "back_to_menu") return true
        
        // Не обрабатываем main_* payload если уже в соответствующем состоянии
        if (payload == "main_partner" && currentState == BotState.PARTNER_MENU) return false
        if (payload == "main_diary" && currentState == BotState.DIARY_CALENDAR) return false
        if (payload == "main_statistics" && currentState == BotState.STATISTICS) return false
        if (payload == "main_relapse" && currentState == BotState.RELAPSE) return false
        
        return payload?.startsWith("main_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        return processPayload(payload, update.message?.sender?.userId)
    }
    
    override suspend fun handleCallback(update: MessageCallbackUpdate, currentState: BotState): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        return processPayload(payload, update.callback?.user?.userId)
    }
    
    private suspend fun processPayload(payload: String?, userId: Long?): HandlerResult {
        when (payload) {
            "main_quit" -> {
                val uid = userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
                val started = startQuitUseCase.execute(uid)
                return HandlerResult(
                    text = if (started) "Начинаем процесс отказа от курения..." else "Не удалось начать процесс отказа.",
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
        
        return HandlerResult(
            text = BotTexts.WELCOME_MESSAGE,
            keyboard = Keyboards.mainMenu(),
            newState = BotState.MAIN_MENU
        )
    }
}
