package org.white_powerbank.usecases

import org.white_powerbank.models.Note
import org.white_powerbank.repositories.NotesRepository
import org.white_powerbank.repositories.UsersRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * UseCase для работы с временными данными дневника
 * Сохраняет частичные записи (без body) для хранения выбранной даты и уровня тяги
 */
data class DiaryEntryData(
    val selectedDate: LocalDate?,
    val pullLevel: Int?
)

class DiaryEntryDataUseCase(
    private val notesRepository: NotesRepository,
    private val usersRepository: UsersRepository
) {
    /**
     * Сохранить временные данные дневника (дата и уровень тяги)
     * Сохраняется как частичная запись с пустым body
     */
    suspend fun saveTemporaryData(userMaxId: Long, date: LocalDate?, pullLevel: Int?) {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return
        val internalUserId = user.id
        
        // Если оба параметра null, удаляем временную запись
        if (date == null && pullLevel == null) {
            deleteTemporaryData(userMaxId)
            return
        }
        
        // Используем сегодняшнюю дату как ключ для временных данных
        // или дату из параметра, если она указана
        val tempDate = date ?: LocalDate.now()
        val dateAsDate = Date.from(tempDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        
        // Проверяем, есть ли уже временная запись
        val existingNote = try {
            notesRepository.getNoteByUserIdAndDate(internalUserId, dateAsDate)
        } catch (e: Exception) {
            null
        }
        
        val note = if (existingNote != null && existingNote.body.isEmpty()) {
            // Обновляем существующую временную запись
            if (date != null) {
                val newDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                // Если дата изменилась, удаляем старую и создаем новую
                if (existingNote.date != newDate) {
                    notesRepository.deleteNote(existingNote.id)
                    Note(
                        id = 0,
                        userId = internalUserId,
                        date = newDate,
                        pullLevel = pullLevel ?: existingNote.pullLevel,
                        failed = false,
                        body = ""
                    )
                } else {
                    // Обновляем существующую запись
                    existingNote.apply {
                        this.pullLevel = pullLevel ?: this.pullLevel
                    }
                }
            } else {
                // Обновляем только уровень тяги
                existingNote.apply {
                    this.pullLevel = pullLevel ?: this.pullLevel
                }
            }
        } else {
            // Создаем новую временную запись
            Note(
                id = 0,
                userId = internalUserId,
                date = dateAsDate,
                pullLevel = pullLevel ?: 0,
                failed = false,
                body = "" // Пустое body означает временную запись
            )
        }
        
        // Сохраняем или обновляем запись
        if (existingNote != null && existingNote.body.isEmpty()) {
            if (date != null) {
                val newDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                if (existingNote.date != newDate) {
                    // Новая запись уже создана, старую удалили выше
                    notesRepository.addNote(note)
                } else {
                    // Обновляем существующую
                    notesRepository.updateNote(note)
                }
            } else {
                // Обновляем существующую
                notesRepository.updateNote(note)
            }
        } else {
            notesRepository.addNote(note)
        }
    }
    
    /**
     * Получить временные данные дневника для пользователя
     */
    suspend fun getTemporaryData(userMaxId: Long): DiaryEntryData? {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return null
        val internalUserId = user.id
        
        // Получаем все записи пользователя и ищем временную (с пустым body)
        val notes = notesRepository.getNotesByUserId(internalUserId)
        val tempNote = notes.firstOrNull { it.body.isEmpty() }
        
        if (tempNote == null) {
            return DiaryEntryData(null, null)
        }
        
        val selectedDate = tempNote.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        return DiaryEntryData(
            selectedDate = selectedDate,
            pullLevel = tempNote.pullLevel
        )
    }
    
    /**
     * Удалить временные данные дневника
     */
    suspend fun deleteTemporaryData(userMaxId: Long) {
        val user = usersRepository.getUserByMaxId(userMaxId) ?: return
        val internalUserId = user.id
        
        // Получаем все записи пользователя и удаляем временные (с пустым body)
        val notes = notesRepository.getNotesByUserId(internalUserId)
        notes.filter { it.body.isEmpty() }.forEach {
            notesRepository.deleteNote(it.id)
        }
    }
}

