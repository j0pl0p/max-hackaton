package org.white_powerbank.repositories

import org.white_powerbank.models.User

interface UsersRepository {

    fun getUserById(id: Long): User?

    fun getUserByMaxId(id: Long): User?

    fun updateUser(user: User)

    fun  addUser(user: User)

}