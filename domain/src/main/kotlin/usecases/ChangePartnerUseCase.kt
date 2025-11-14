package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для смены напарника
 */
class ChangePartnerUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Сменить напарника (удалить текущего и начать поиск нового)
     * @param userMaxId ID пользователя в MAX
     * @return true если смена успешно начата
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Удаляем текущего напарника
        user.partnerId = null
        user.partnerSearchStatus = PartnerSearchStatus.ACTIVE
        
        // Также нужно удалить связь у напарника
        // TODO: если у напарника был этот пользователь, нужно обновить и его
        
        usersRepository.updateUser(user)
        
        // TODO: начать поиск нового напарника
        
        return true
    }
}

