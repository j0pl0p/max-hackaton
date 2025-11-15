package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.models.BotState
import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.models.User
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import java.time.LocalDateTime

class MessageRouter(
    private val stateManager: UserStateManager,
    private val usersRepository: UsersRepository,
    private val handlers: List<Handler>
) {
    suspend fun route(update: Update): HandlerResult {
        val userId = when (update) {
            is MessageCreatedUpdate -> update.message?.sender?.userId
            is MessageCallbackUpdate -> update.callback?.user?.userId
            else -> null
        } ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        ensureUserExists(userId)
        return processHandlers(update, userId)
    }

    private suspend fun ensureUserExists(userId: Long) {
        if (usersRepository.getUserByMaxId(userId) == null) {
            usersRepository.addUser(User(
                id = 0,
                maxId = userId,
                state = BotState.WELCOME,
                partnerId = null,
                tempNoteId = null,
                partnerSearchStatus = PartnerSearchStatus.INACTIVE,
                lastActivity = LocalDateTime.now(),
                isQuitting = false,
                lastStart = null,
                averageMonthlyExpenses = 0L,
                maxStreak = 0,
                profileLink = null
            ))
        }
    }

    private suspend fun processHandlers(update: Update, userId: Long): HandlerResult {
        var currentState = stateManager.getState(userId) ?: BotState.WELCOME
        var lastResult: HandlerResult? = null

        var iterations = 0
        while (iterations < 10) {
            iterations++

            var handlerFound = false
            for (handler in handlers) {
                if (handler.canHandle(update, currentState)) {
                    val result = handler.handle(update, currentState)
                    lastResult = result

                    result.newState?.let { newState ->
                        if (newState != currentState) {
                            stateManager.setState(userId, newState)
                            currentState = newState
                            
                            // If empty text, continue processing for new state
                            if (result.text.isEmpty()) {
                                handlerFound = true
                                break // Continue loop to find handler for new state
                            }
                        }
                    }

                    if (result.text.isNotEmpty()) {
                        return result
                    }

                    handlerFound = true
                    break
                }
            }

            if (!handlerFound) {
                break
            }
        }

        return lastResult ?: HandlerResult(
            text = "Неизвестная команда. Возвращаюсь в главное меню.",
            keyboard = org.white_powerbank.bot.keyboards.Keyboards.welcomeMenu(),
            newState = BotState.WELCOME
        )
    }
}
