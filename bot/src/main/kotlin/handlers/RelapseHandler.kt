package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.RestartQuitUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

class RelapseHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val restartQuitUseCase: RestartQuitUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        val result = currentState == BotState.RELAPSE || payload?.startsWith("relapse_") == true
        println("RelapseHandler.canHandle: currentState=$currentState, payload=$payload, result=$result")
        return result
    }
    
    override suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        val result = currentState == BotState.RELAPSE || payload?.startsWith("relapse_") == true
        println("RelapseHandler.canHandleCallback: currentState=$currentState, payload=$payload, result=$result")
        return result
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId
        val payload = MessageUtils.getPayload(update)
        return processRelapse(userId, payload)
    }
    
    override suspend fun handleCallback(update: MessageCallbackUpdate, currentState: BotState): HandlerResult {
        val userId = update.callback?.user?.userId
        val payload = MessageUtils.getPayload(update)
        println("RelapseHandler.handleCallback: userId=$userId, payload=$payload")
        return processRelapse(userId, payload)
    }
    
    private suspend fun processRelapse(userId: Long?, payload: String?): HandlerResult {
        println("RelapseHandler.processRelapse: userId=$userId, payload=$payload")
        when (payload) {
            "relapse_diary" -> {
                return HandlerResult(
                    text = "",
                    keyboard = null,
                    newState = BotState.DIARY_CALENDAR
                )
            }
            "relapse_restart" -> {
                val uid = userId ?: return HandlerResult("Ошибка")
                val restarted = restartQuitUseCase.execute(uid)
                return HandlerResult(
                    text = if (restarted) "Начинаем заново. Ты справишься!" else "Не удалось начать заново.",
                    keyboard = Keyboards.mainMenu(),
                    newState = BotState.MAIN_MENU
                )
            }
        }
        
        val result = HandlerResult(
            text = BotTexts.RELAPSE_MESSAGE,
            keyboard = Keyboards.relapse(),
            newState = BotState.RELAPSE
        )
        println("RelapseHandler.processRelapse: returning text='${result.text}'")
        return result
    }
}
