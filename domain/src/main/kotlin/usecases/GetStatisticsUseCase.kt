package org.white_powerbank.usecases

import org.white_powerbank.models.User
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.repositories.NotesRepository
import java.util.Date

/**
 * UseCase для получения статистики пользователя
 */
data class UserStatistics(
    val lastSmokingDay: Int, // Дней с последнего дня курения
    val totalDaysWithoutSmoking: Int, // Всего дней без курения
    val currentStreak: Int, // Текущий стрик (дни подряд без курения)
    val totalRelapses: Int // Количество срывов
)

class GetStatisticsUseCase(
    private val usersRepository: UsersRepository,
    private val notesRepository: NotesRepository
) {
    /**
     * Получить статистику пользователя
     * @param userMaxId ID пользователя в MAX
     * @return UserStatistics
     */
    suspend fun execute(userMaxId: Long): UserStatistics {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return UserStatistics(0, 0, 0, 0)
        
        // Получаем все записи пользователя
        val notes = notesRepository.getNotesByUserId(user.id)
        
        if (notes.isEmpty()) {
            // Если нет записей, но пользователь начал процесс отказа
            val lastStart = user.lastStart
            if (lastStart != null) {
                val daysSinceStart = calculateDaysBetween(lastStart, Date())
                return UserStatistics(
                    lastSmokingDay = daysSinceStart,
                    totalDaysWithoutSmoking = daysSinceStart,
                    currentStreak = daysSinceStart,
                    totalRelapses = 0
                )
            }
            return UserStatistics(0, 0, 0, 0)
        }
        
        val sortedNotes = notes.sortedBy { it.date }
        val firstNote = sortedNotes.first()
        val lastNote = sortedNotes.last()
        
        // Дней с последнего дня курения (с момента последней записи)
        val lastSmokingDay = calculateDaysBetween(lastNote.date, Date())
        
        // Всего дней без курения (от первой записи до последней)
        val totalDaysWithoutSmoking = calculateDaysBetween(firstNote.date, lastNote.date)
        
        // Текущий стрик - дни с последней записи
        val currentStreak = lastSmokingDay
        
        // Количество срывов - упрощенно, считаем по количеству записей с высоким уровнем тяги
        // TODO: добавить поле failed в Note для более точного подсчета
        val totalRelapses = sortedNotes.count { it.pullLevel >= 8 }
        
        return UserStatistics(
            lastSmokingDay = lastSmokingDay,
            totalDaysWithoutSmoking = totalDaysWithoutSmoking,
            currentStreak = currentStreak,
            totalRelapses = totalRelapses
        )
    }
    
    private fun calculateDaysBetween(date1: Date, date2: Date): Int {
        val diff = date2.time - date1.time
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }
}

