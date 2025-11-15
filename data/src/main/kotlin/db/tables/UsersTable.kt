package org.white_powerbank.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val maxId = long("max_id").uniqueIndex()
    val state = integer("state").index()
    val partnerId = long("partner_id").nullable().index()
    val tempNoteId = long("temp_note_id").nullable()
    val partnerSearchStatus = integer("partner_search_status").index()
    val lastActivity = datetime("last_activity").nullable().index()
    val isQuitting = bool("is_quitting")
    val lastStart = date("last_start").nullable()
    val averageMonthlyExpenses = long("average_monthly_expenses")
    val maxStreak = integer("max_streak").default(0)

    override val primaryKey = PrimaryKey(id)
}
