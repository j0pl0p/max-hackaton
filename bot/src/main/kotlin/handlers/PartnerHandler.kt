package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.ChangePartnerUseCase
import org.white_powerbank.usecases.GetPartnerInfoUseCase
import org.white_powerbank.usecases.SearchPairUseCase
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Обработчик сценария "Напарник"
 */
class PartnerHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val usersRepository: UsersRepository,
    private val searchPairUseCase: SearchPairUseCase,
    private val changePartnerUseCase: ChangePartnerUseCase,
    private val getPartnerInfoUseCase: GetPartnerInfoUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") {
            return false
        }
        return currentState == BotState.PARTNER_MENU ||
               payload?.startsWith("partner_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val user = usersRepository.getUserByMaxId(userId)
        val payload = MessageUtils.getPayload(update)
        
        // Обработка кнопок
        when (payload) {
            "partner_search" -> {
                val searchStarted = searchPairUseCase.execute(userId)
                return HandlerResult(
                    text = if (searchStarted) "Ищем напарника..." else "Не удалось начать поиск. Возможно, у вас уже есть напарник.",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
            "partner_change" -> {
                val changeStarted = changePartnerUseCase.execute(userId)
                return HandlerResult(
                    text = if (changeStarted) "Смена напарника... Ищем нового напарника." else "Не удалось сменить напарника.",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        }
        
        // Проверяем, есть ли напарник
        val hasPartner = user?.partnerId != null && user.partnerId!! > 0
        
        if (hasPartner) {
            // Получаем информацию о напарнике через UseCase
            val partnerInfo = getPartnerInfoUseCase.execute(userId)
            
            if (partnerInfo != null) {
                return HandlerResult(
                    text = BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking),
                    keyboard = Keyboards.partnerWithPartner(),
                    newState = BotState.PARTNER_MENU
                )
            } else {
                // Если не удалось получить информацию о напарнике
                return HandlerResult(
                    text = "Не удалось получить информацию о напарнике.",
                    keyboard = Keyboards.partnerWithPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        } else {
            return HandlerResult(
                text = BotTexts.NO_PARTNER_MESSAGE,
                keyboard = Keyboards.partnerNoPartner(),
                newState = BotState.PARTNER_MENU
            )
        }
    }
}

