package org.white_powerbank.usecases

import org.white_powerbank.models.Note
import org.white_powerbank.repositories.NotesRepository
import org.white_powerbank.repositories.UsersRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class GetNoteByDateUseCase(
    private val notesRepository: NotesRepository,
    private val usersRepository: UsersRepository
) {
    suspend fun execute(userMaxId: Long, date: LocalDate): Note? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        val internalUserId = user.id
        
        val dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        
        return try {
            val note = notesRepository.getNoteByUserIdAndDate(internalUserId, dateAsDate)
            // Возвращаем только полные заметки (не временные)
            if (note?.body?.isNotEmpty() == true) note else null
        } catch (e: Exception) {
            null
        }
    }
}