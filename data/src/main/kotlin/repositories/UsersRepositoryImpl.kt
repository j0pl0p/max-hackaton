package org.white_powerbank.repositories

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.UsersTable
import org.white_powerbank.models.*

class UsersRepositoryImpl : UsersRepository {

    override fun getUserById(id: Long): User? = transaction {
        UsersTable.select { UsersTable.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun getUserByMaxId(id: Long): User? = transaction {
        UsersTable.select { UsersTable.maxId eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    override fun addUser(user: User): Unit = transaction {
        UsersTable.insert { it.fillWith(user) }
        Unit
    }

    override fun updateUser(user: User): Unit = transaction {
        UsersTable.update({ UsersTable.id eq user.id }) { it.fillWith(user) }
        Unit
    }

    override fun getUsersBySearchStatus(status: PartnerSearchStatus): List<User> = transaction {
        UsersTable.select { UsersTable.partnerSearchStatus eq status.statusId }
            .map { it.toUser() }
    }
}

fun UpdateBuilder<*>.fillWith(user: User) {
    this[UsersTable.maxId] = user.maxId
    this[UsersTable.state] = user.state.id.toInt()
    this[UsersTable.partnerId] = user.partnerId
    this[UsersTable.partnerSearchStatus] = user.partnerSearchStatus.statusId
    this[UsersTable.lastActivity] = user.lastActivity
    this[UsersTable.isQuitting] = user.isQuitting
    this[UsersTable.lastStart] = user.lastStart
    this[UsersTable.averageMonthlyExpenses] = user.averageMonthlyExpenses
    this[UsersTable.maxStreak] = user.maxStreak
}

fun ResultRow.toUser() = User(
    id = this[UsersTable.id],
    maxId = this[UsersTable.maxId],
    state = BotState.fromId(this[UsersTable.state].toLong()),
    partnerId = this[UsersTable.partnerId],
    partnerSearchStatus = PartnerSearchStatus.fromId(this[UsersTable.partnerSearchStatus]),
    lastActivity = this[UsersTable.lastActivity] ?: throw IllegalStateException("LastActivity cannot be null"),
    isQuitting = this[UsersTable.isQuitting],
    lastStart = this[UsersTable.lastStart],
    averageMonthlyExpenses = this[UsersTable.averageMonthlyExpenses],
    maxStreak = this[UsersTable.maxStreak]
)