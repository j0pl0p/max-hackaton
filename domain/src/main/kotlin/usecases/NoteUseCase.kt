package org.white_powerbank.usecases

import org.white_powerbank.models.Note
import org.white_powerbank.repositories.NotesRepository
import org.white_powerbank.repositories.UsersRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class NoteUseCase(
    private val notesRepository: NotesRepository,
    private val usersRepository: UsersRepository
) {
    /**
     * Создать пустую заметку для даты
     */
    suspend fun createEmptyNote(userMaxId: Long, date: LocalDate): Long? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        val dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        
        // Проверяем, есть ли уже заметка на эту дату
        val existing = notesRepository.getNoteByUserIdAndDate(user.id, dateAsDate)
        if (existing != null) return existing.id
        
        val note = Note(
            id = 0,
            userId = user.id,
            date = dateAsDate,
            pullLevel = null,
            failed = null,
            body = null
        )
        
        return notesRepository.addNote(note)
    }
    
    /**
     * Обновить уровень тяги в заметке
     */
    suspend fun updatePullLevel(noteId: Long, pullLevel: Int) {
        val note = notesRepository.getNoteById(noteId) ?: return
        note.pullLevel = pullLevel
        notesRepository.updateNote(note)
    }
    
    /**
     * Обновить текст заметки
     */
    suspend fun updateNoteText(noteId: Long, text: String) {
        val note = notesRepository.getNoteById(noteId) ?: return
        note.body = text
        note.failed = false
        notesRepository.updateNote(note)
    }
    
    /**
     * Получить заметку по ID
     */
    suspend fun getNoteById(noteId: Long): Note? {
        return notesRepository.getNoteById(noteId)
    }
    
    /**
     * Получить заметку по дате (только с текстом)
     */
    suspend fun getNoteByDate(userMaxId: Long, date: LocalDate): Note? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        val dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val note = notesRepository.getNoteByUserIdAndDate(user.id, dateAsDate)
        return if (note?.body != null && note.body!!.isNotEmpty()) note else null
    }
}