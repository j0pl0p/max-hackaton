package org.white_powerbank.bot.handlers

import ru.max.botapi.model.Update
import ru.max.bot.builders.attachments.InlineKeyboardBuilder

/**
 * Базовый интерфейс для обработчиков сообщений
 */
interface Handler {
    /**
     * Может ли этот обработчик обработать данное обновление
     */
    suspend fun canHandle(update: Update, currentState: org.white_powerbank.models.BotState): Boolean

    /**
     * Обработать обновление и вернуть ответ
     */
    suspend fun handle(update: Update, currentState: org.white_powerbank.models.BotState): HandlerResult
}

/**
 * Результат обработки сообщения
 */
data class HandlerResult(
    val text: String,
    val keyboard: InlineKeyboardBuilder? = null,
    val newState: org.white_powerbank.models.BotState? = null
)

