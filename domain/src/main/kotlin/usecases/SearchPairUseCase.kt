package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для поиска напарника
 */
class SearchPairUseCase(
    private val usersRepository: UsersRepository,
    private val matchPartnersUseCase: MatchPartnersUseCase
) {
    /**
     * Начать поиск напарника для пользователя
     * @param userMaxId ID пользователя в MAX
     * @return true если поиск успешно начат или партнер найден, false если пользователь уже в поиске или имеет напарника
     */
    suspend fun execute(userMaxId: Long): Boolean {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return false
        
        // Если уже есть напарник, нельзя начать поиск
        if (user.partnerId != null && user.partnerId!! > 0) {
            return false
        }
        
        // Если уже в поиске, пытаемся найти партнера
        if (user.partnerSearchStatus == PartnerSearchStatus.ACTIVE) {
            matchPartnersUseCase.execute(userMaxId)
            return true
        }
        
        // Начинаем поиск
        user.partnerSearchStatus = PartnerSearchStatus.ACTIVE
        user.partnerId = null // Убеждаемся, что напарника нет
        usersRepository.updateUser(user)
        
        // Пытаемся найти подходящего партнера
        matchPartnersUseCase.execute(userMaxId)
        
        return true
    }
}

