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

    /**
     * Атомарно связать двух пользователей как партнеров
     * @param user1Id ID первого пользователя
     * @param user2Id ID второго пользователя
     * @return true если операция успешна
     */
    fun matchPartners(user1Id: Long, user2Id: Long): Boolean

    /**
     * Атомарно разорвать связь между партнерами
     * @param userId ID пользователя
     * @param partnerId ID партнера
     * @return true если операция успешна
     */
    fun unmatchPartners(userId: Long, partnerId: Long): Boolean

}