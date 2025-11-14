package org.white_powerbank.repositories

import org.white_powerbank.models.Award

interface AwardsRepository {

    fun getAwards(): List<Award>

    fun getAward(id: Long): Award?

}