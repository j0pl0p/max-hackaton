package org.white_powerbank.usecases

import org.white_powerbank.models.User
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.repositories.NotesRepository
import java.util.Date

/**
 * UseCase для получения статистики пользователя
 */
data class UserStatistics(
    val currentStreak: Int, // Текущий стрик (дни без курения с lastStart)
    val maxStreak: Int, // Максимальный стрик за всю историю
    val totalRelapses: Int // Количество срывов
)

class GetStatisticsUseCase(
    private val usersRepository: UsersRepository,
    private val notesRepository: NotesRepository,
    private val checkAwardsUseCase: CheckAwardsUseCase
) {
    /**
     * Получить статистику пользователя
     * @param userMaxId ID пользователя в MAX
     * @return UserStatistics
     */
    suspend fun execute(userMaxId: Long): UserStatistics {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return UserStatistics(0, 0, 0)
        
        // Текущий стрик - дни с момента lastStart
        val currentStreak = if (user.lastStart != null && user.isQuitting) {
            calculateDaysBetween(user.lastStart!!, java.time.LocalDate.now())
        } else {
            0
        }
        
        // Обновляем максимальный стрик если текущий больше
        if (currentStreak > user.maxStreak) {
            user.maxStreak = currentStreak
            usersRepository.updateUser(user)
        }
        
        // Проверяем и выдаем новые награды
        checkAwardsUseCase.execute(userMaxId, currentStreak)
        
        // Получаем все записи пользователя для подсчета срывов
        val notes = notesRepository.getNotesByUserId(user.id)
        val totalRelapses = notes.count { it.failed == true }
        
        return UserStatistics(
            currentStreak = currentStreak,
            maxStreak = user.maxStreak,
            totalRelapses = totalRelapses
        )
    }
    
    private fun calculateDaysBetween(date1: java.time.LocalDate, date2: java.time.LocalDate): Int {
        return java.time.temporal.ChronoUnit.DAYS.between(date1, date2).toInt().coerceAtLeast(0)
    }
}

