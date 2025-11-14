package org.white_powerbank.repositories

import org.white_powerbank.models.User
import org.white_powerbank.models.PartnerSearchStatus

interface UsersRepository {

    fun getUserById(id: Long): User?

    fun getUserByMaxId(id: Long): User?

    fun updateUser(user: User)

    fun  addUser(user: User)

    /**
     * Получить всех пользователей с указанным статусом поиска партнера
     * @param status статус поиска партнера
     * @return список пользователей с указанным статусом
     */
    fun getUsersBySearchStatus(status: PartnerSearchStatus): List<User>

}