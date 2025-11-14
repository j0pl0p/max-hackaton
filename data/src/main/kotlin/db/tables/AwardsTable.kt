package org.white_powerbank.db.tables

import org.jetbrains.exposed.sql.Table

object AwardsTable : Table("awards") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).index()
    val description = text("description")
    val picturePath = varchar("picture_path", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}