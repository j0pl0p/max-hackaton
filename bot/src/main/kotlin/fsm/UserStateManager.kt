package org.white_powerbank.bot.fsm

import org.white_powerbank.models.BotStates
import org.white_powerbank.domain.repositories.UserReposytory
import org.white_powerbank.models.User

/**
 * Управление состояниями пользователя
 * Работает с репозиториями для получения и обновления состояния
 */

class UserStateManager (
    private val userRepository: UserRepository
) {

    /**
     * Получить текущее состояние пользователя
     */
    fun getState(userId: Long): BotStates {
        val user = userRepository.getUser(userId)
        return user?.state
    }

    /**
     * Установить новое состояние пользователя
     */
    fun setState(userId: Long, state: BotStates) {
        val user = userRepository.getUser(userId)
        if (user != null) {
            user.state = state
            userRepository.updateUser(user)
        }
    }
}


