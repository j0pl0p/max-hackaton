package org.white_powerbank.models

enum class BotState(val id: Long) {
    MAIN_MENU(1),
    PARTNER_MENU(2),
    STATISTICS(3),
    DIARY_CALENDAR(4),
    DIARY_SELECT_DATE(5),
    DIARY_ENTER_PULL_LEVEL(6),
    DIARY_ENTER_NOTE(7),
    RELAPSE(8),
    QUIT_START(9)
}