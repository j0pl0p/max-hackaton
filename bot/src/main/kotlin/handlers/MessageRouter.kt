package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.domain.repositories.UserRepository
import org.white_powerbank.models.BotStates
import ru.max.botapi.model.MessageCreatedUpdate

/**
 * Маршрутизатор сообщений
 * Определяет, какой обработчик должен обработать входящее сообщение
 */
class MessageRouter(
    private val stateManager: UserStateManager,
    private val userRepository: UserRepository,
    private val handlers: List<Handler>
) {
    /**
     * Обработать входящее сообщение
     */
    suspend fun route(update: MessageCreatedUpdate): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        // Получаем или создаем пользователя
        val user = UserRepository.getUser(userId)
        if (user == null) {
            val newUser = org.white_powerbank.models.User(
                id = 0,
                max_id = userId,
                state = BotStates.MAIN_MENU,
                partner_id = -1,
                last_activity_date = System.currentTimeMillis(),
                is_quiting = false
            )
            UserRepository.saveUser(newUser)
        }
        
        var currentState = stateManager.getState(userId)
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
                newState = BotStates.MAIN_MENU
            )
        }
        
        return lastResult ?: HandlerResult(
            text = "Неизвестная команда. Возвращаюсь в главное меню.",
            keyboard = org.white_powerbank.bot.keyboards.Keyboards.mainMenu(),
            newState = BotStates.MAIN_MENU
        )
    }
}

