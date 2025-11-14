package org.white_powerbank.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.AwardsTable
import org.white_powerbank.models.Award

class AwardsRepositoryImpl : AwardsRepository {
    override fun getAwards(): List<Award> = transaction {
        AwardsTable.selectAll().map { it.toAward() }
    }

    override fun getAward(id: Long): Award? = transaction {
        AwardsTable.select { AwardsTable.id eq id }
            .map { it.toAward() }
            .singleOrNull()
    }


}

fun ResultRow.toAward() = Award(
    id = this[AwardsTable.id],
    name = this[AwardsTable.name],
    description = this[AwardsTable.description],
    picture_path = this[AwardsTable.picturePath]
)