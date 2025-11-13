package org.white_powerbank.models

data class User(
    var id: Long,
    var max_id: Long,
    var state_id: Long,
    var partner_id: Long,
    var last_activity_date: Long,
    var is_quiting: Boolean,


) {


}