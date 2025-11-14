package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.domain.repositories.UserReposytory
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик сценария "Напарник"
 */
class PartnerHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val userReposytory: UserReposytory
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        return currentState == BotState.PARTNER_MENU ||
               payload?.startsWith("partner_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val user = userReposytory.getUser(userId)
        val payload = MessageUtils.getPayload(update)
        
        // Обработка кнопок
        when (payload) {
            "partner_search" -> {
                // TODO: вызвать SearchPairUseCase
                return HandlerResult(
                    text = "Ищем напарника...",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
            "partner_change" -> {
                // TODO: вызвать UseCase для смены напарника
                return HandlerResult(
                    text = "Смена напарника...",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        }
        
        // Проверяем, есть ли напарник
        val hasPartner = user?.partner_id != null && user.partner_id > 0
        
        if (hasPartner) {
            // TODO: получить информацию о напарнике через UseCase
            // Пока используем заглушку
            val partnerName = "Напарник" // TODO: получить из UseCase
            val partnerDays = 14 // TODO: получить из UseCase
            
            return HandlerResult(
                text = BotTexts.getPartnerInfo(partnerName, partnerDays),
                keyboard = Keyboards.partnerWithPartner(),
                newState = BotState.PARTNER_MENU
            )
        } else {
            return HandlerResult(
                text = BotTexts.NO_PARTNER_MESSAGE,
                keyboard = Keyboards.partnerNoPartner(),
                newState = BotState.PARTNER_MENU
            )
        }
    }
}

