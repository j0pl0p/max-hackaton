package org.white_powerbank.bot.fsm

import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository

/**
 * Управление состояниями пользователя
 * Работает с репозиториями для получения и обновления состояния
 */

class UserStateManager (
    private val userRepository: UsersRepository
) {

    /**
     * Получить текущее состояние пользователя
     */
    fun getState(userMaxId: Long): BotState? {
        val user = userRepository.getUserByMaxId(userMaxId)
        return user?.state
    }

    /**
     * Установить новое состояние пользователя
     */
    fun setState(userMaxId: Long, state: BotState) {
        val user = userRepository.getUserById(userMaxId)
        if (user != null) {
            user.state = state
            userRepository.updateUser(user)
        }
    }
}


