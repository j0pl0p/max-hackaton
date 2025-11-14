package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для поиска напарника
 */
class SearchPairUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Начать поиск напарника для пользователя
     * @param userMaxId ID пользователя в MAX
     * @return true если поиск успешно начат, false если пользователь уже в поиске или имеет напарника
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Если уже есть напарник, нельзя начать поиск
        if (user.partnerId != null && user.partnerId!! > 0) {
            return false
        }
        
        if (user.partnerSearchStatus == PartnerSearchStatus.ACTIVE) {
            return true
        }
        
        user.partnerSearchStatus = PartnerSearchStatus.ACTIVE
        user.partnerId = null // Убеждаемся, что напарника нет
        usersRepository.updateUser(user)
        
        // TODO: здесь должна быть логика поиска подходящего напарника
        // Пока просто устанавливаем статус ACTIVE
        
        return true
    }
}

