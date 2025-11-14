package org.white_powerbank.repositories

import org.white_powerbank.models.Award

interface UsersAwardsRepository {

    fun getAwardsByUserId(userId: Long): List<Award>
    
    fun addAwardToUser(userId: Long, awardId: Long)
    
    fun removeAwardFromUser(userId: Long, awardId: Long)

}