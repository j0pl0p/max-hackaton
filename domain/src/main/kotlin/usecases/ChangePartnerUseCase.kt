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
        
        // Сохраняем ID текущего напарника перед удалением
        val oldPartnerId = user.partnerId
        
        // Удаляем текущего напарника и начинаем поиск нового
        user.partnerId = null
        user.partnerSearchStatus = PartnerSearchStatus.ACTIVE
        usersRepository.updateUser(user)
        
        // Также нужно удалить связь у напарника, если он существует
        if (oldPartnerId != null && oldPartnerId > 0) {
            val partner = usersRepository.getUserById(oldPartnerId)
            if (partner != null && partner.partnerId == user.id) {
                // Если напарник указывал на этого пользователя, удаляем связь
                partner.partnerId = null
                partner.partnerSearchStatus = PartnerSearchStatus.INACTIVE
                usersRepository.updateUser(partner)
            }
        }
        
        return true
    }
}

