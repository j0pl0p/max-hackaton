package org.white_powerbank.bot.handlers

import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import ru.max.botapi.model.BotStartedUpdate

/**
 * Утилиты для работы с Update объектами
 */
object UpdateUtils {
    /**
     * Получить ID пользователя из любого типа Update
     */
    fun getUserId(update: Update): Long? = when (update) {
        is MessageCreatedUpdate -> update.message?.sender?.userId
        is MessageCallbackUpdate -> update.callback?.user?.userId
        is BotStartedUpdate -> {
            println("BotStartedUpdate user: ${update.user}")
            println("BotStartedUpdate userId: ${update.user?.userId}")
            update.user?.userId
        }
        else -> null
    }
    
    /**
     * Получить payload из любого типа Update
     */
    fun getPayload(update: Update): String? = when (update) {
        is MessageCreatedUpdate -> MessageUtils.getPayload(update)
        is MessageCallbackUpdate -> update.callback?.payload
        else -> null
    }
    
    /**
     * Получить текст сообщения (только для MessageCreatedUpdate)
     */
    fun getText(update: Update): String? = when (update) {
        is MessageCreatedUpdate -> update.message?.body?.text
        else -> null
    }
}