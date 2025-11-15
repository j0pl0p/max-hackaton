package org.white_powerbank.usecases

import org.white_powerbank.models.Award
import org.white_powerbank.repositories.UsersAwardsRepository
import org.white_powerbank.repositories.UsersRepository

data class AchievementInfo(
    val name: String,
    val description: String
)

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
        
        val awards = try {
            usersAwardsRepository.getAwardsByUserId(user.id)
        } catch (e: Exception) {
            emptyList<Award>()
        }
        
        return awards.map { it.name }
    }
    
    /**
     * Получить полную информацию о достижениях с пагинацией
     */
    suspend fun getAchievementsWithPagination(userMaxId: Long, page: Int = 0): Pair<List<AchievementInfo>, Boolean> {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return Pair(emptyList(), false)
        
        val awards = try {
            usersAwardsRepository.getAwardsByUserId(user.id)
        } catch (e: Exception) {
            emptyList<Award>()
        }
        
        val pageSize = 1
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, awards.size)
        
        val pageAwards = if (startIndex < awards.size) {
            awards.subList(startIndex, endIndex).map { 
                AchievementInfo(it.name, it.description) 
            }
        } else {
            emptyList()
        }
        
        val hasMore = endIndex < awards.size
        return Pair(pageAwards, hasMore)
    }
}

