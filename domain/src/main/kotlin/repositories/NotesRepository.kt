package org.white_powerbank.repositories

import java.util.Date
import org.white_powerbank.models.Note

interface NotesRepository {

    fun getNotesByUserId(userId: Long): List<Note>

    fun getNoteById(noteId: Long): Note

    fun getNoteByUserIdAndDate(userId: Long, date: Date): Note

    fun addNote(note: Note)

    fun updateNote(note: Note)

    fun deleteNote(noteId: Long)

    // [date_from, date_to]
    fun getNotesByUserIdAndDates(userId: Long, dateFrom: Date, dateTo: Date): List<Note>


}