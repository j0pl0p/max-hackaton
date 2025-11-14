package org.white_powerbank.models

import java.util.Date

data class Note (
    var id: Long,
    var userId: Long,
    var date: Date,
    var pullLevel: Int,
    var failed: Boolean,
    var body: String
)
