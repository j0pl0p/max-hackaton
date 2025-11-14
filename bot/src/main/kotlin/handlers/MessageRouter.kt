package org.white_powerbank.bot.handlers

import handlers.UserNotFoundException
import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.models.BotState
import org.white_powerbank.models.PartnerSearchStatus
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Маршрутизатор сообщений
 * Определяет, какой обработчик должен обработать входящее сообщение
 */
class MessageRouter(
    private val stateManager: UserStateManager,
    private val usersRepository: UsersRepository,
    private val handlers: List<Handler>
) {
    /**
     * Обработать входящее сообщение
     */
    suspend fun route(update: MessageCreatedUpdate): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        // Получаем или создаем пользователя
        val user = usersRepository.getUserByMaxId(userId)
        if (user == null) {
            val newUser = org.white_powerbank.models.User(
                id = 0,
                maxId = userId,
                state = BotState.MAIN_MENU,
                partnerId = null,
                partnerSearchStatus = PartnerSearchStatus.INACTIVE,
                lastActivityDate = System.currentTimeMillis(),
                isQuiting = false,
                lastStart = null,
                averageMonthlyExpenses = 0L
            )
            usersRepository.addUser(newUser)
        }
        
        var currentState = stateManager.getState(userId)?:
                                                throw UserNotFoundException("User not found: can't get state")
        var lastResult: HandlerResult? = null
        
        // Обрабатываем сообщение, возможно несколько раз, если состояние изменилось
        var iterations = 0
        while (iterations < 10) { // Защита от бесконечного цикла
            iterations++
            
            // Ищем подходящий обработчик
            var handlerFound = false
            for (handler in handlers) {
                if (handler.canHandle(update, currentState)) {
                    val result = handler.handle(update, currentState)
                    lastResult = result
                    
                    // Обновляем состояние, если оно изменилось
                    result.newState?.let { newState ->
                        if (newState != currentState) {
                            stateManager.setState(userId, newState)
                            currentState = newState
                            
                            // Если текст пустой, продолжаем обработку с новым состоянием
                            if (result.text.isEmpty()) {
                                handlerFound = true
                                break
                            }
                        }
                    }
                    
                    // Если текст не пустой, возвращаем результат
                    if (result.text.isNotEmpty()) {
                        return result
                    }
                    
                    handlerFound = true
                    break
                }
            }
            
            // Если не нашли обработчик или текст не пустой, выходим
            if (!handlerFound || lastResult?.text?.isNotEmpty() == true) {
                break
            }
        }
        
        // Если после всех итераций текст все еще пустой, возвращаем главное меню
        if (lastResult?.text?.isEmpty() == true) {
            return HandlerResult(
                text = "Неизвестная команда. Возвращаюсь в главное меню.",
                keyboard = org.white_powerbank.bot.keyboards.Keyboards.mainMenu(),
                newState = BotState.MAIN_MENU
            )
        }
        
        return lastResult ?: HandlerResult(
            text = "Неизвестная команда. Возвращаюсь в главное меню.",
            keyboard = org.white_powerbank.bot.keyboards.Keyboards.mainMenu(),
            newState = BotState.MAIN_MENU
        )
    }
}

