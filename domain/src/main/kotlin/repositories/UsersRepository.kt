package org.white_powerbank.repositories

import org.white_powerbank.models.User
import org.white_powerbank.models.PartnerSearchStatus

interface UsersRepository {

    fun getUserById(id: Long): User?

    fun getUserByMaxId(id: Long): User?

    fun updateUser(user: User)

    fun  addUser(user: User)
    
    fun getUsersBySearchStatus(status: PartnerSearchStatus): List<User>

}
