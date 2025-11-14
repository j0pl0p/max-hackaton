package org.white_powerbank.bot.handlers

import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

object MessageUtils {
    fun getPayload(update: MessageCreatedUpdate): String? {
        val text = update.message?.body?.text
        if (text != null && text.isNotEmpty() && !text.startsWith("/")) {
            if (!text.contains(" ") && text.length < 200) {
                return text
            }
        }
        return null
    }
    
    fun getPayload(update: MessageCallbackUpdate): String? {
        return update.callback?.payload
    }
}
