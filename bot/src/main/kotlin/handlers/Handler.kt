package org.white_powerbank.bot.handlers

import ru.max.botapi.model.Message
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.bot.builders.attachments.InlineKeyboardBuilder

/**
 * Базовый интерфейс для обработчиков сообщений
 */
interface Handler {
    /**
     * Может ли этот обработчик обработать данное сообщение
     */
    suspend fun canHandle(update: MessageCreatedUpdate, currentState: org.white_powerbank.models.BotStates): Boolean

    /**
     * Обработать сообщение и вернуть ответ
     */
    suspend fun handle(update: MessageCreatedUpdate, currentState: org.white_powerbank.models.BotStates): HandlerResult
}

/**
 * Результат обработки сообщения
 */
data class HandlerResult(
    val text: String,
    val keyboard: InlineKeyboardBuilder? = null,
    val newState: org.white_powerbank.models.BotStates? = null
)

