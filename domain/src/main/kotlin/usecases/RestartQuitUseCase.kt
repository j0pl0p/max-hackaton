package org.white_powerbank.usecases

import org.white_powerbank.repositories.UsersRepository
import java.util.Date

/**
 * UseCase для начала заново после срыва
 */
class RestartQuitUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Начать заново после срыва
     * @param userMaxId ID пользователя в MAX
     * @return true если процесс успешно перезапущен
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Обновляем дату начала заново
        user.lastStart = java.time.LocalDate.now()
        user.lastActivity = java.time.LocalDateTime.now()
        user.isQuitting = true // Убеждаемся, что флаг установлен
        
        usersRepository.updateUser(user)
        
        return true
    }
}

