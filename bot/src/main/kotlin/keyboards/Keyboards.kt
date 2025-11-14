package org.white_powerbank.bot.keyboards

import ru.max.bot.builders.attachments.InlineKeyboardBuilder
import ru.max.botapi.model.CallbackButton

object Keyboards {
    fun mainMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("main_quit", "Бросить вейпить"))
            .addRow(CallbackButton("main_partner", "Напарник"))
            .addRow(CallbackButton("main_diary", "Дневник"))
            .addRow(CallbackButton("main_statistics", "Статистика"))
            .addRow(CallbackButton("main_relapse", "Сорвался"))
    }

    fun partnerNoPartner(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("partner_search", "Найти напарника"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun partnerWithPartner(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("partner_change", "Сменить напарника"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun statistics(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun diaryCalendar(year: Int = 2025, month: Int = 1): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("diary_calendar_date:2025-01-15", "15 января"))
            .addRow(CallbackButton("diary_calendar_date:2025-01-16", "16 января"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun diaryPullLevel(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("diary_pull_level:0", "0"), CallbackButton("diary_pull_level:1", "1"))
            .addRow(CallbackButton("diary_pull_level:2", "2"), CallbackButton("diary_pull_level:3", "3"))
            .addRow(CallbackButton("diary_pull_level:4", "4"), CallbackButton("diary_pull_level:5", "5"))
            .addRow(CallbackButton("diary_pull_level:6", "6"), CallbackButton("diary_pull_level:7", "7"))
            .addRow(CallbackButton("diary_pull_level:8", "8"), CallbackButton("diary_pull_level:9", "9"))
            .addRow(CallbackButton("diary_pull_level:10", "10"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun relapse(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("relapse_diary", "Заметка в дневник"))
            .addRow(CallbackButton("relapse_restart", "Начать заново"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    fun backToMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }
}
