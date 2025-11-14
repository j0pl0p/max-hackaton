package org.white_powerbank.usecases

import org.white_powerbank.repositories.UsersRepository
import java.util.Date

/**
 * UseCase для получения информации о напарнике
 */
data class PartnerInfo(
    val name: String,
    val daysWithoutSmoking: Int,
    val totalDaysWithoutSmoking: Int
)

class GetPartnerInfoUseCase(
    private val usersRepository: UsersRepository,
    private val notesRepository: org.white_powerbank.repositories.NotesRepository
) {
    /**
     * Получить информацию о напарнике пользователя
     * @param userMaxId ID пользователя в MAX
     * @return PartnerInfo или null если напарника нет
     */
    suspend fun execute(userMaxId: Long): PartnerInfo? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        
        val partnerId = user.partnerId ?: return null
        if (partnerId <= 0) return null
        
        // Получаем напарника по его внутреннему ID
        val partner = usersRepository.getUserById(partnerId) ?: return null
        
        // Вычисляем дни без курения
        val daysWithoutSmoking = calculateDaysWithoutSmoking(partner)
        val totalDaysWithoutSmoking = calculateTotalDaysWithoutSmoking(partner)
        
        // Имя напарника - используем maxId как идентификатор, так как имени нет в модели
        val partnerName = "Напарник #${partner.maxId}"
        
        return PartnerInfo(
            name = partnerName,
            daysWithoutSmoking = daysWithoutSmoking,
            totalDaysWithoutSmoking = totalDaysWithoutSmoking
        )
    }
    
    /**
     * Вычислить текущий стрик (дни без курения с последнего срыва)
     */
    private fun calculateDaysWithoutSmoking(user: org.white_powerbank.models.User): Int {
        val lastStart = user.lastStart ?: return 0
        val now = Date()
        val diff = now.time - lastStart.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * Вычислить общее количество дней без курения
     */
    private fun calculateTotalDaysWithoutSmoking(user: org.white_powerbank.models.User): Int {
        // Получаем все записи пользователя
        val notes = notesRepository.getNotesByUserId(user.id)
        
        // Считаем дни между записями
        // Упрощенная логика: считаем дни с первой записи до последней
        if (notes.isEmpty()) return 0
        
        val sortedNotes = notes.sortedBy { it.date }
        val firstNote = sortedNotes.first()
        val lastNote = sortedNotes.last()
        
        val diff = lastNote.date.time - firstNote.date.time
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}

