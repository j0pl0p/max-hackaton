package org.white_powerbank.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.NotesTable
import org.white_powerbank.models.Note
import java.util.Date
import java.time.LocalDate
import java.time.ZoneId

class NotesRepositoryImpl : NotesRepository {

    override fun getNotesByUserId(userId: Long): List<Note> = transaction {
        NotesTable.select { NotesTable.userId eq userId }
            .orderBy(NotesTable.date, SortOrder.DESC)
            .map { it.toNote() }
    }

    override fun getNoteById(noteId: Long): Note? = transaction {
        NotesTable.select { NotesTable.id eq noteId }
            .map { it.toNote() }
            .singleOrNull()
    }

    override fun getNoteByUserIdAndDate(userId: Long, date: Date): Note? = transaction {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        NotesTable.select {
            (NotesTable.userId eq userId) and (NotesTable.date eq localDate)
        }
            .map { it.toNote() }
            .singleOrNull()
    }

    override fun addNote(note: Note): Long = transaction {
        NotesTable.insertAndGetId { it.fillWith(note) }.value
    }

    override fun updateNote(note: Note): Unit = transaction {
        NotesTable.update({ NotesTable.id eq note.id }) { it.fillWith(note) }
        Unit
    }

    override fun deleteNote(noteId: Long): Unit = transaction {
        NotesTable.deleteWhere { NotesTable.id eq noteId }
        Unit
    }

    override fun getNotesByUserIdAndDates(userId: Long, dateFrom: Date, dateTo: Date): List<Note> = transaction {
        val localDateFrom = dateFrom.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val localDateTo = dateTo.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        NotesTable.select {
            (NotesTable.userId eq userId) and
            (NotesTable.date greaterEq localDateFrom) and
            (NotesTable.date lessEq localDateTo)
        }
        .orderBy(NotesTable.date, SortOrder.DESC)
        .map { it.toNote() }
    }

}

fun UpdateBuilder<*>.fillWith(note: Note) {
    this[NotesTable.userId] = note.userId
    this[NotesTable.date] = note.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    this[NotesTable.pullLevel] = note.pullLevel
    this[NotesTable.failed] = note.failed
    this[NotesTable.body] = note.body
}

fun ResultRow.toNote() = Note(
    id = this[NotesTable.id].value,
    userId = this[NotesTable.userId],
    date = Date.from(this[NotesTable.date].atStartOfDay(ZoneId.systemDefault()).toInstant()),
    pullLevel = this[NotesTable.pullLevel],
    failed = this[NotesTable.failed],
    body = this[NotesTable.body]
)