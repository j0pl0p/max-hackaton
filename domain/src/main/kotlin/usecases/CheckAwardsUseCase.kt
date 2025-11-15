package org.white_powerbank.usecases

import org.white_powerbank.repositories.AwardsRepository
import org.white_powerbank.repositories.UsersAwardsRepository
import org.white_powerbank.repositories.UsersRepository

/**
 * UseCase для проверки и выдачи наград по достижению стрика
 */
class CheckAwardsUseCase(
    private val usersRepository: UsersRepository,
    private val awardsRepository: AwardsRepository,
    private val usersAwardsRepository: UsersAwardsRepository
) {
    // Пороги для получения наград (в днях)
    private val awardThresholds = mapOf(
        1L to 1,    // Первый день
        2L to 7,    // Неделя силы  
        3L to 30,   // Месяц победы
        4L to 90    // Квартал чемпиона
    )
    
    /**
     * Проверить и выдать награды пользователю на основе его текущего стрика
     * @param userMaxId ID пользователя в MAX
     * @param currentStreak текущий стрик пользователя
     * @return список новых наград (названия)
     */
    suspend fun execute(userMaxId: Long, currentStreak: Int): List<String> {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return emptyList()
        
        // Получаем уже имеющиеся награды пользователя
        val existingAwards = usersAwardsRepository.getAwardsByUserId(user.id)
        val existingAwardIds = existingAwards.map { it.id }.toSet()
        
        val newAwards = mutableListOf<String>()
        
        // Проверяем каждый порог
        for ((awardId, threshold) in awardThresholds) {
            if (currentStreak >= threshold && !existingAwardIds.contains(awardId)) {
                // Пользователь достиг порога и еще не имеет эту награду
                try {
                    usersAwardsRepository.addAwardToUser(user.id, awardId)
                    val award = awardsRepository.getAward(awardId)
                    if (award != null) {
                        newAwards.add(award.name)
                    }
                } catch (e: Exception) {
                    // Игнорируем ошибки (возможно награда уже была выдана)
                }
            }
        }
        
        return newAwards
    }
}