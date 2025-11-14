package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для остановки поиска напарника
 */
class StopSearchUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Остановить поиск напарника
     * @param userMaxId ID пользователя в MAX
     * @return true если поиск успешно остановлен
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Если не в поиске, ничего не делаем
        if (user.partnerSearchStatus != PartnerSearchStatus.ACTIVE) {
            return true
        }
        
        // Останавливаем поиск
        user.partnerSearchStatus = PartnerSearchStatus.INACTIVE
        usersRepository.updateUser(user)
        
        return true
    }
}

