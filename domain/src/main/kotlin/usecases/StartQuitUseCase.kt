package org.white_powerbank.usecases

import org.white_powerbank.repositories.UsersRepository
import java.util.Date

/**
 * UseCase для начала процесса отказа от курения
 */
class StartQuitUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Начать процесс отказа от курения
     * @param userMaxId ID пользователя в MAX
     * @return true если процесс успешно начат
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Устанавливаем флаг начала процесса отказа
        user.isQuiting = true
        user.lastStart = Date()
        user.lastActivityDate = System.currentTimeMillis()
        
        usersRepository.updateUser(user)
        
        return true
    }
}

