package org.white_powerbank.db.tables

import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.date

object NotesTable : IdTable<Long>("notes") {
    override val id = long("id").autoIncrement().entityId()
    val userId = long("user_id").index()
    val date = date("date").index()
    val pullLevel = integer("pull_level").nullable()
    val failed = bool("failed").nullable()
    val body = text("body").nullable()
    
    init {
        index(false, userId, date)
    }
}