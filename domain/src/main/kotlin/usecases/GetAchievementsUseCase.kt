package org.white_powerbank.usecases

import org.white_powerbank.models.Award
import org.white_powerbank.repositories.UsersAwardsRepository
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для получения достижений пользователя
 */
class GetAchievementsUseCase(
    private val usersAwardsRepository: UsersAwardsRepository,
    private val usersRepository: UsersRepository
) {
    /**
     * Получить список достижений пользователя
     * @param userMaxId ID пользователя в MAX
     * @return список названий достижений
     */
    suspend fun execute(userMaxId: Long): List<String> {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return emptyList()
        
        // Получаем достижения пользователя
        val awards = try {
            usersAwardsRepository.getAwardsByUserId(user.id)
        } catch (e: Exception) {
            emptyList<Award>()
        }
        
        return awards.map { it.name }
    }
}

