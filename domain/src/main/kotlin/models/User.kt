package org.white_powerbank.models

data class User(
    var id: Long,
    var max_id: Long,
    var state: BotStates,
    var partner_id: Long, // if no partner so -1
    var last_activity_date: Long,
    var is_quiting: Boolean,


) {


}