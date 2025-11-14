package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.models.BotState
import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.models.User
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import java.time.LocalDateTime

class MessageRouter(
    private val stateManager: UserStateManager,
    private val usersRepository: UsersRepository,
    private val handlers: List<Handler>
) {
    suspend fun route(update: MessageCreatedUpdate): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка")
        ensureUserExists(userId)
        return processHandlers(update, userId, true)
    }

    suspend fun routeCallback(update: MessageCallbackUpdate): HandlerResult {
        val userId = update.callback?.user?.userId ?: return HandlerResult("Ошибка")
        ensureUserExists(userId)
        return processHandlers(update, userId, true)
    }

    private suspend fun ensureUserExists(userId: Long) {
        if (usersRepository.getUserByMaxId(userId) == null) {
            usersRepository.addUser(User(
                id = 0,
                maxId = userId,
                state = BotState.MAIN_MENU,
                partnerId = null,
                partnerSearchStatus = PartnerSearchStatus.INACTIVE,
                lastActivity = LocalDateTime.now(),
                isQuitting = false,
                lastStart = null,
                averageMonthlyExpenses = 0L
            ))
        }
    }

    private suspend fun processHandlers(update: Any, userId: Long, checkPayload: Boolean): HandlerResult {
        val currentState = stateManager.getState(userId) ?: BotState.MAIN_MENU
        println("MessageRouter.processHandlers: currentState=$currentState, checkPayload=$checkPayload")
        
        for (handler in handlers) {
            val canHandle = when (update) {
                is MessageCreatedUpdate -> handler.canHandle(update, currentState)
                is MessageCallbackUpdate -> handler.canHandleCallback(update, currentState)
                else -> false
            }
            
            println("Handler ${handler::class.simpleName} canHandle=$canHandle")
            
            if (canHandle) {
                val result = when (update) {
                    is MessageCreatedUpdate -> handler.handle(update, currentState)
                    is MessageCallbackUpdate -> handler.handleCallback(update, currentState)
                    else -> return HandlerResult("Неизвестный тип обновления")
                }
                
                println("Result: text='${result.text}', newState=${result.newState}")
                
                result.newState?.let { newState ->
                    if (newState != currentState) {
                        stateManager.setState(userId, newState)
                        println("State changed from $currentState to $newState")
                        
                        if (result.text.isEmpty()) {
                            println("Text is empty, recursing...")
                            return processHandlers(update, userId, false)
                        }
                    }
                }
                
                return result
            }
        }
        
        println("No handler found, returning default")
        return HandlerResult(
            text = "Неизвестная команда. Возвращаюсь в главное меню.",
            keyboard = org.white_powerbank.bot.keyboards.Keyboards.mainMenu(),
            newState = BotState.MAIN_MENU
        )
    }
}
