package org.white_powerbank.usecases

import org.white_powerbank.repositories.UsersRepository
import java.util.Date

/**
 * UseCase для получения информации о напарнике
 */
data class PartnerInfo(
    val name: String,
    val daysWithoutSmoking: Int
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
        
        // Вычисляем стрик напарника (дни с lastStart)
        val daysWithoutSmoking = if (partner.lastStart != null && partner.isQuitting) {
            java.time.temporal.ChronoUnit.DAYS.between(partner.lastStart, java.time.LocalDate.now()).toInt().coerceAtLeast(0)
        } else {
            0
        }
        
        // Имя напарника
        val partnerName = "Напарник #${partner.maxId}"
        
        return PartnerInfo(
            name = partnerName,
            daysWithoutSmoking = daysWithoutSmoking
        )
    }
    

}

