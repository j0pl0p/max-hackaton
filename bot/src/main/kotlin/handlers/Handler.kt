package org.white_powerbank.bot.handlers

import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import ru.max.bot.builders.attachments.InlineKeyboardBuilder

interface Handler {
    suspend fun canHandle(update: MessageCreatedUpdate, currentState: org.white_powerbank.models.BotState): Boolean
    suspend fun handle(update: MessageCreatedUpdate, currentState: org.white_powerbank.models.BotState): HandlerResult
    
    suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: org.white_powerbank.models.BotState): Boolean = false
    
    suspend fun handleCallback(update: MessageCallbackUpdate, currentState: org.white_powerbank.models.BotState): HandlerResult = 
        HandlerResult("Callback не поддерживается")
}

data class HandlerResult(
    val text: String,
    val keyboard: InlineKeyboardBuilder? = null,
    val newState: org.white_powerbank.models.BotState? = null
)
