package org.white_powerbank.usecases

import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для обработки срыва - обнуляет стрик и останавливает процесс отказа
 */
class RelapseUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Обработать срыв пользователя
     * @param userMaxId ID пользователя в MAX
     * @return true если срыв успешно обработан
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Обнуляем стрик и останавливаем процесс отказа
        user.isQuitting = false
        user.lastStart = null
        user.lastActivity = java.time.LocalDateTime.now()
        
        usersRepository.updateUser(user)
        
        return true
    }
}