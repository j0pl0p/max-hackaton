package org.white_powerbank.models

enum class BotState(val id: Long) {
    WELCOME(1),
    MAIN_MENU(2),
    PARTNER_MENU(3),
    STATISTICS(4),
    DIARY_CALENDAR(5),
    DIARY_SELECT_DATE(6),
    DIARY_ENTER_PULL_LEVEL(7),
    DIARY_ENTER_NOTE(8),
    DIARY_VIEW_NOTE(9),
    RELAPSE(10),
    QUIT_START(11),
    AWARDS(12),
    ENTER_PROFILE_LINK(13);

    companion object {
        fun fromId(id: Long): BotState {
            return entries.firstOrNull { it.id == id } ?: WELCOME
        }
    }
}