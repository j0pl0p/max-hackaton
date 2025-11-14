package org.white_powerbank.db.tables

import org.jetbrains.exposed.sql.Table

object UsersAwardsTable : Table("users_awards") {
    val userId = long("user_id").references(UsersTable.id).index()
    val awardId = long("award_id").references(AwardsTable.id).index()

    override val primaryKey = PrimaryKey(userId, awardId)
}