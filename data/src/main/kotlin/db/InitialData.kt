package org.white_powerbank.db

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.white_powerbank.db.tables.AwardsTable

object InitialData {
    fun insertAwards() {
        transaction {
            // Проверяем, есть ли уже награды
            if (AwardsTable.selectAll().count() == 0L) {
                AwardsTable.insert {
                    it[name] = "Первый день"
                    it[description] = "Поздравляем! Вы продержались первый день без курения!"
                    it[picturePath] = null
                }
                AwardsTable.insert {
                    it[name] = "Неделя силы"
                    it[description] = "Отлично! Целая неделя без курения - это серьезное достижение!"
                    it[picturePath] = null
                }
                AwardsTable.insert {
                    it[name] = "Месяц победы"
                    it[description] = "Невероятно! Месяц без курения - вы на правильном пути!"
                    it[picturePath] = null
                }
                AwardsTable.insert {
                    it[name] = "Квартал чемпиона"
                    it[description] = "Фантастика! 3 месяца без курения - вы настоящий чемпион!"
                    it[picturePath] = null
                }
            }
        }
    }
}