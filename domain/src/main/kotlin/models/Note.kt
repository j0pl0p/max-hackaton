package org.white_powerbank.models

import kotlinx.datetime.LocalDate

data class Note(
    val id: Long,
    val user_id: Long,
    val date: LocalDate,
    val failed: Boolean,
    val pull_level: Int,
    val body: String
)


