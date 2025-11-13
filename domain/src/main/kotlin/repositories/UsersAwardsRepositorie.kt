package org.white_powerbank.repositories

import org.white_powerbank.models.Award

interface UsersAwardsRepositorie {

    fun  getAwardsByUserId(userId: Int): List<Award>


}