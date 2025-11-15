package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.ChangePartnerUseCase
import org.white_powerbank.usecases.GetPartnerInfoUseCase
import org.white_powerbank.usecases.SearchPairUseCase
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate

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
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val payload = UpdateUtils.getPayload(update)
        if (payload == "back_to_menu") return false
        
        // Обрабатываем ENTER_PROFILE_LINK только для MessageCreatedUpdate
        if (currentState == BotState.ENTER_PROFILE_LINK) {
            return update is ru.max.botapi.model.MessageCreatedUpdate
        }
        
        return currentState == BotState.PARTNER_MENU || payload?.startsWith("partner_") == true
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val userId = UpdateUtils.getUserId(update) ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val user = usersRepository.getUserByMaxId(userId)
        val payload = UpdateUtils.getPayload(update)
        val text = UpdateUtils.getText(update)
        
        // Обработка ввода ссылки профиля (только для MessageCreatedUpdate)
        if (currentState == BotState.ENTER_PROFILE_LINK && update is ru.max.botapi.model.MessageCreatedUpdate) {
            if (text != null && text.isNotBlank() && !text.startsWith("/")) {
                val trimmedText = text.trim()
                
                // Проверяем формат ссылки
                if (trimmedText.startsWith("https://max.ru/u/")) {
                    user?.let {
                        it.profileLink = trimmedText
                        usersRepository.updateUser(it)
                    }
                    return HandlerResult(
                        text = "Ссылка на профиль сохранена! Теперь можно искать напарника.",
                        keyboard = Keyboards.partnerNoPartner(),
                        newState = BotState.PARTNER_MENU
                    )
                } else {
                    return HandlerResult(
                        text = "Ссылка неправильная. Отправьте ссылку в формате: https://max.ru/u/...",
                        keyboard = Keyboards.backToMenu(),
                        newState = BotState.ENTER_PROFILE_LINK
                    )
                }
            }
            // Если текст некорректный, просим еще раз
            return HandlerResult(
                text = "Отправьте ссылку на ваш профиль в MAX:",
                keyboard = Keyboards.backToMenu(),
                newState = BotState.ENTER_PROFILE_LINK
            )
        }
        
        // Обработка кнопок
        when (payload) {
            "partner_search" -> {
                // Проверяем наличие ссылки профиля
                if (user?.profileLink.isNullOrBlank()) {
                    return HandlerResult(
                        text = "Для поиска напарника нужна ссылка на ваш профиль.\n\nОтправьте ссылку на ваш профиль в MAX:",
                        keyboard = Keyboards.backToMenu(),
                        newState = BotState.ENTER_PROFILE_LINK
                    )
                }
                
                val searchStarted = searchPairUseCase.execute(userId)
                if (!searchStarted) {
                    return HandlerResult(
                        text = "Не удалось начать поиск. Возможно, у вас уже есть напарник.",
                        keyboard = Keyboards.partnerNoPartner(),
                        newState = BotState.PARTNER_MENU
                    )
                }
                
                // Проверяем, был ли найден партнер сразу
                val updatedUser = usersRepository.getUserByMaxId(userId)
                val hasPartner = updatedUser?.partnerId != null && updatedUser.partnerId!! > 0
                
                if (hasPartner) {
                    // Партнер найден! Получаем информацию о нем
                    val partnerInfo = getPartnerInfoUseCase.execute(userId)
                    if (partnerInfo != null) {
                        return HandlerResult(
                            text = "Напарник найден!\n\n${BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking, partnerInfo.profileLink)}",
                            keyboard = Keyboards.partnerWithPartner(),
                            newState = BotState.PARTNER_MENU
                        )
                    }
                }
                
                return HandlerResult(
                    text = "Ищем напарника... Напарник пока не найден: зайдите пойже",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
            "partner_change" -> {
                val changeStarted = changePartnerUseCase.execute(userId)
                if (!changeStarted) {
                    return HandlerResult(
                        text = "Не удалось сменить напарника.",
                        keyboard = Keyboards.partnerNoPartner(),
                        newState = BotState.PARTNER_MENU
                    )
                }
                
                // После смены напарника начинаем поиск нового
                val searchStarted = searchPairUseCase.execute(userId)
                
                // Проверяем, был ли найден новый партнер сразу
                val updatedUser = usersRepository.getUserByMaxId(userId)
                val hasPartner = updatedUser?.partnerId != null && updatedUser.partnerId!! > 0
                
                if (hasPartner) {
                    // Новый партнер найден! Получаем информацию о нем
                    val partnerInfo = getPartnerInfoUseCase.execute(userId)
                    if (partnerInfo != null) {
                        return HandlerResult(
                            text = "🎉 Новый напарник найден!\n\n${BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking, partnerInfo.profileLink)}",
                            keyboard = Keyboards.partnerWithPartner(),
                            newState = BotState.PARTNER_MENU
                        )
                    }
                }
                
                return HandlerResult(
                    text = "Смена напарника... Ищем нового напарника. Мы уведомим вас, когда найдем подходящего напарника.",
                    keyboard = Keyboards.partnerNoPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        }
        
        // Проверяем, есть ли напарник
        val hasPartner = user?.partnerId != null && user.partnerId!! > 0
        
        return if (hasPartner) {
            // Получаем информацию о напарнике через UseCase
            val partnerInfo = getPartnerInfoUseCase.execute(userId)
            
            if (partnerInfo != null) {
                HandlerResult(
                    text = BotTexts.getPartnerInfo(partnerInfo.name, partnerInfo.daysWithoutSmoking, partnerInfo.profileLink),
                    keyboard = Keyboards.partnerWithPartner(),
                    newState = BotState.PARTNER_MENU
                )
            } else {
                HandlerResult(
                    text = "Не удалось получить информацию о напарнике.",
                    keyboard = Keyboards.partnerWithPartner(),
                    newState = BotState.PARTNER_MENU
                )
            }
        } else {
            HandlerResult(
                text = BotTexts.NO_PARTNER_MESSAGE,
                keyboard = Keyboards.partnerNoPartner(),
                newState = BotState.PARTNER_MENU
            )
        }
    }
}

