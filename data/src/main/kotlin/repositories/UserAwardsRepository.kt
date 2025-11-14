package org.white_powerbank.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.AwardsTable
import org.white_powerbank.db.tables.UsersAwardsTable
import org.white_powerbank.models.Award

class UsersAwardsRepositoryImpl : UsersAwardsRepository {
    override fun getAwardsByUserId(userId: Long): List<Award> = transaction {
        (UsersAwardsTable innerJoin AwardsTable)
            .select { UsersAwardsTable.userId eq userId }
            .map {
                Award(
                    id = it[AwardsTable.id],
                    name = it[AwardsTable.name],
                    description = it[AwardsTable.description],
                    picture_path = it[AwardsTable.picturePath]
                )
            }
    }
    
    override fun addAwardToUser(userId: Long, awardId: Long): Unit = transaction {
        UsersAwardsTable.insert {
            it[UsersAwardsTable.userId] = userId
            it[UsersAwardsTable.awardId] = awardId
        }
        Unit
    }
    
    override fun removeAwardFromUser(userId: Long, awardId: Long): Unit = transaction {
        UsersAwardsTable.deleteWhere {
            (UsersAwardsTable.userId eq userId) and (UsersAwardsTable.awardId eq awardId)
        }
        Unit
    }
}
