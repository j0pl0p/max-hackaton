package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для смены напарника
 */
class ChangePartnerUseCase(
    private val usersRepository: UsersRepository,
    private val matchPartnersUseCase: MatchPartnersUseCase
) {
    /**
     * Сменить напарника (удалить текущего и начать поиск нового)
     * @param userMaxId ID пользователя в MAX
     * @return true если смена успешно начата
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        val oldPartnerId = user.partnerId
        
        if (oldPartnerId == null || oldPartnerId <= 0) return false
        
        // Атомарно разрываем связь с текущим партнером
        if (!usersRepository.unmatchPartners(user.id, oldPartnerId)) return false
        
        // Пытаемся сразу найти нового напарника
        matchPartnersUseCase.execute(userMaxId)
        
        return true
    }
}

