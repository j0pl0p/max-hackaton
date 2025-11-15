package org.white_powerbank.usecases

import org.white_powerbank.models.PartnerSearchStatus
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для подбора партнеров
 * Ищет подходящего партнера для пользователя, который находится в поиске
 */
class MatchPartnersUseCase(
    private val usersRepository: UsersRepository
) {
    /**
     * Попытаться найти и связать партнеров
     * @param userMaxId ID пользователя в MAX, который ищет партнера
     * @return ID найденного партнера (maxId) или null если партнер не найден
     */
    suspend fun execute(userMaxId: Long): Long? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        
        // Проверяем, что пользователь действительно ищет партнера
        if (user.partnerSearchStatus != PartnerSearchStatus.ACTIVE) {
            return null
        }
        
        // Если уже есть партнер, не ищем нового
        if (user.partnerId != null && user.partnerId!! > 0) {
            return null
        }
        
        // Ищем подходящего партнера среди всех пользователей, которые тоже ищут партнера
        val searchingUsers = usersRepository.getUsersBySearchStatus(PartnerSearchStatus.ACTIVE)
        
        // Ищем первого подходящего партнера (не самого пользователя и без партнера)
        val partner = searchingUsers.firstOrNull { 
            it.id != user.id && 
            it.partnerId == null && 
            it.partnerSearchStatus == PartnerSearchStatus.ACTIVE 
        }
        
        if (partner != null) {
            // Нашли партнера - связываем их атомарно
            return if (usersRepository.matchPartners(user.id, partner.id)) {
                partner.maxId
            } else null
        }
        
        // Партнер не найден - статус остается ACTIVE, чтобы другие могли найти этого пользователя
        return null
    }
}

