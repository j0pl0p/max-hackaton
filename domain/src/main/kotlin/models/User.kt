package org.white_powerbank.models

import java.util.Date

data class User(
    var id: Long,
    var maxId: Long,
    var stateId: Long,
    var partnerId: Long,
    var lastActivityDate: Long,
    var isQuiting: Boolean,
    var lastStart: Date?,
    var averageMonthlyExpenses: Long,
)