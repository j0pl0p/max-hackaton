package org.white_powerbank.models

import java.time.LocalDate
import java.time.LocalDateTime

data class User(
    var id: Long,
    var maxId: Long,
    var state: BotState,
    var partnerId: Long?,
    var tempNoteId: Long?,
    var partnerSearchStatus: PartnerSearchStatus,
    var lastActivity: LocalDateTime,
    var isQuitting: Boolean,
    var lastStart: LocalDate?,
    var averageMonthlyExpenses: Long,
    var maxStreak: Int
)