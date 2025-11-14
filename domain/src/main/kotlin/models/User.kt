package org.white_powerbank.models

import java.util.Date

data class User(
    var id: Long,
    var maxId: Long,
    var state: BotState,
    var partnerId: Long?,
    var partnerSearchStatus: PartnerSearchStatus,
    var lastActivityDate: Long,
    var isQuiting: Boolean,
    var lastStart: Date?,
    var averageMonthlyExpenses: Long,
)