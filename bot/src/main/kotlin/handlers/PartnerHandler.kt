package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.ChangePartnerUseCase
import org.white_powerbank.usecases.GetPartnerInfoUseCase
import org.white_powerbank.usecases.SearchPairUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

class PartnerHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val usersRepository: UsersRepository,
    private val searchPairUseCase: SearchPairUseCase,
    private val changePartnerUseCase: ChangePartnerUseCase,
    private val getPartnerInfoUseCase: GetPartnerInfoUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.PARTNER_MENU || payload?.startsWith("partner_") == true
    }
    
    override suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        return currentState == BotState.PARTNER_MENU || payload?.startsWith("partner_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка")
        val payload = MessageUtils.getPayload(update)
        return processPartner(userId, payload)
    }
    
    override suspend fun handleCallback(update: MessageCallbackUpdate, currentState: BotState): HandlerResult {
        val userId = update.callback?.user?.userId ?: return HandlerResult("Ошибка")
        val payload = MessageUtils.getPayload(update)
        return processPartner(userId, payload)
    }
    
    private suspend fun processPartner(userId: Long, payload: String?): HandlerResult {
        when (payload) {
            "partner_search" -> {
                searchPairUseCase.execute(userId)
                val updatedUser = usersRepository.getUserByMaxId(userId)
                val hasPartner = updatedUser?.partnerId != null && updatedUser.partnerId!! > 0
                
                if (hasPartner) {
                    val partnerInfo = getPartnerInfoUseCase.execute(userId)
                    if (partnerInfo != null) {
                        return HandlerResult(
                            text = "Напарник найден!\n\n${BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking)}",
                            keyboard = Keyboards.partnerWithPartner(),
                            newState = BotState.PARTNER_MENU
                        )
                    }
                }
                
                return HandlerResult(
                    text = "Ищем напарника...",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
            "partner_change" -> {
                changePartnerUseCase.execute(userId)
                searchPairUseCase.execute(userId)
                
                val updatedUser = usersRepository.getUserByMaxId(userId)
                val hasPartner = updatedUser?.partnerId != null && updatedUser.partnerId!! > 0
                
                if (hasPartner) {
                    val partnerInfo = getPartnerInfoUseCase.execute(userId)
                    if (partnerInfo != null) {
                        return HandlerResult(
                            text = "🎉 Новый напарник найден!\n\n${BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking)}",
                            keyboard = Keyboards.partnerWithPartner(),
                            newState = BotState.PARTNER_MENU
                        )
                    }
                }
                
                return HandlerResult(
                    text = "Ищем нового напарника...",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        }
        
        val user = usersRepository.getUserByMaxId(userId)
        val hasPartner = user?.partnerId != null && user.partnerId!! > 0
        
        if (hasPartner) {
            val partnerInfo = getPartnerInfoUseCase.execute(userId)
            if (partnerInfo != null) {
                return HandlerResult(
                    text = BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking),
                    keyboard = Keyboards.partnerWithPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        }
        
        return HandlerResult(
            text = BotTexts.NO_PARTNER_MESSAGE,
            keyboard = Keyboards.partnerNoPartner(),
            newState = BotState.PARTNER_MENU
        )
    }
}
