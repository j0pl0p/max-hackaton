package org.white_powerbank.bot.handlers

import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Вспомогательные функции для работы с сообщениями
 */
object MessageUtils {
    /**
     * Получить payload из сообщения (для callback кнопок)
     * В MAX SDK при нажатии на callback кнопку payload приходит в тексте сообщения
     * как строка, которую мы передали в CallbackButton
     */
    fun getPayload(update: MessageCreatedUpdate): String? {
        // В MAX SDK payload от callback кнопок приходит в тексте сообщения
        val text = update.message?.body?.text
        if (text != null && text.isNotEmpty() && !text.startsWith("/")) {
            // Проверяем, является ли это payload (не команда и не обычный текст)
            // Payload обычно короткие строки без пробелов или с определенными префиксами
            if (!text.contains(" ") && text.length < 200) {
                return text
            }
        }
        
        return null
    }
    
    /**
     * Проверить, является ли сообщение нажатием на callback кнопку
     * В MAX SDK это определяется по наличию текста без пробелов и определенной длины
     */
    fun isCallbackButton(update: MessageCreatedUpdate): Boolean {
        val text = update.message?.body?.text
        return text != null && 
               text.isNotEmpty() && 
               !text.startsWith("/") && 
               !text.contains(" ") && 
               text.length < 200
    }
}

