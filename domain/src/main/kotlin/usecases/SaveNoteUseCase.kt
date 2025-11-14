package org.white_powerbank.usecases

import org.white_powerbank.models.Note
import org.white_powerbank.repositories.NotesRepository
import java.util.Date

/**
 * UseCase для сохранения записи в дневник
 */
class SaveNoteUseCase(
    private val notesRepository: NotesRepository
) {
    /**
     * Сохранить запись в дневник
     * @param userId ID пользователя
     * @param date Дата записи
     * @param pullLevel Уровень тяги (0-10)
     * @param body Текст заметки
     * @param failed Флаг срыва (по умолчанию false)
     * @return сохраненная запись
     */
    suspend fun execute(
        userId: Long,
        date: Date,
        pullLevel: Int,
        body: String,
        failed: Boolean = false
    ): Note {
        // Проверяем, есть ли уже запись на эту дату
        val existingNote = try {
            notesRepository.getNoteByUserIdAndDate(userId, date)
        } catch (e: Exception) {
            null
        }
        
        val note = if (existingNote != null) {
            // Обновляем существующую запись
            existingNote.apply {
                this.pullLevel = pullLevel
                this.body = body
            }
        } else {
            // Создаем новую запись
            Note(
                id = 0,
                userId = userId,
                date = date,
                pullLevel = pullLevel,
                body = body
            )
        }
        
        if (existingNote != null) {
            notesRepository.updateNote(note)
        } else {
            notesRepository.addNote(note)
        }
        
        return note
    }
}

