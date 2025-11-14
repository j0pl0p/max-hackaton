package org.white_powerbank.bot.keyboards

import ru.max.bot.builders.attachments.InlineKeyboardBuilder
import ru.max.botapi.model.CallbackButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object Keyboards {
    // Главное меню
    fun mainMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("main_quit", "Бросить вейпить")
            )
            .addRow(
                CallbackButton("main_partner", "Напарник")
            )
            .addRow(
                CallbackButton("main_diary", "Дневник")
            )
            .addRow(
                CallbackButton("main_statistics", "Статистика")
            )
            .addRow(
                CallbackButton("main_relapse", "Сорвался")
            )
    }

    // Напарник - нет напарника
    fun partnerNoPartner(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("partner_search", "Найти напарника")
            )
            .addRow(
                CallbackButton("back_to_menu", "В меню")
            )
    }

    // Напарник - есть напарник
    fun partnerWithPartner(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("partner_change", "Сменить напарника")
            )
            .addRow(
                CallbackButton("back_to_menu", "В меню")
            )
    }

    // Статистика
    fun statistics(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("back_to_menu", "В меню")
            )
            .addRow(
                CallbackButton("main_menu", "Главное меню")
            )
    }

    // Дневник - календарь
    fun diaryCalendar(year: Int = LocalDate.now().year, month: Int = LocalDate.now().monthValue): InlineKeyboardBuilder {
        val yearMonth = YearMonth.of(year, month)
        val firstDay = yearMonth.atDay(1)
        val lastDay = yearMonth.atEndOfMonth()
        val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 0 = Monday, 6 = Sunday
        
        val builder = InlineKeyboardBuilder.empty()
        
        // Заголовок с месяцем и годом
        val monthName = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("ru")))
        builder.addRow(CallbackButton("diary_calendar_header", monthName))
        
        // Дни недели
        builder.addRow(
            CallbackButton("diary_calendar_weekday", "Пн"),
            CallbackButton("diary_calendar_weekday", "Вт"),
            CallbackButton("diary_calendar_weekday", "Ср"),
            CallbackButton("diary_calendar_weekday", "Чт"),
            CallbackButton("diary_calendar_weekday", "Пт"),
            CallbackButton("diary_calendar_weekday", "Сб"),
            CallbackButton("diary_calendar_weekday", "Вс")
        )
        
        // Пустые ячейки до первого дня месяца
        val firstWeek = mutableListOf<CallbackButton>()
        repeat(firstDayOfWeek) {
            firstWeek.add(CallbackButton("diary_calendar_empty", " "))
        }
        
        // Дни месяца
        var currentWeek = firstWeek.toMutableList()
        for (day in 1..lastDay.dayOfMonth) {
            val dateStr = "${year}-${String.format("%02d", month)}-${String.format("%02d", day)}"
            currentWeek.add(CallbackButton("diary_calendar_date:$dateStr", day.toString()))
            
            if (currentWeek.size == 7) {
                builder.addRow(*currentWeek.toTypedArray())
                currentWeek = mutableListOf()
            }
        }
        
        // Дополняем последнюю неделю пустыми ячейками
        while (currentWeek.size < 7 && currentWeek.isNotEmpty()) {
            currentWeek.add(CallbackButton("diary_calendar_empty", " "))
        }
        if (currentWeek.isNotEmpty()) {
            builder.addRow(*currentWeek.toTypedArray())
        }
        
        // Навигация по месяцам
        val prevMonth = yearMonth.minusMonths(1)
        val nextMonth = yearMonth.plusMonths(1)
        builder.addRow(
            CallbackButton("diary_calendar_month:${prevMonth.year}:${prevMonth.monthValue}", "◀"),
            CallbackButton("diary_calendar_month:${nextMonth.year}:${nextMonth.monthValue}", "▶")
        )
        
        // Кнопка "В меню"
        builder.addRow(CallbackButton("back_to_menu", "В меню"))
        
        return builder
    }

    // Дневник - выбор уровня тяги (0-10)
    fun diaryPullLevel(): InlineKeyboardBuilder {
        val builder = InlineKeyboardBuilder.empty()
        for (i in 0..10 step 2) {
            val row = mutableListOf<CallbackButton>()
            row.add(CallbackButton("diary_pull_level:$i", i.toString()))
            if (i < 10) {
                row.add(CallbackButton("diary_pull_level:${i + 1}", (i + 1).toString()))
            }
            builder.addRow(*row.toTypedArray())
        }
        builder.addRow(CallbackButton("back_to_menu", "В меню"))
        return builder
    }

    // Сорвался
    fun relapse(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("relapse_diary", "Заметка в дневник"))
            .addRow(CallbackButton("relapse_restart", "Начать заново"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    // Простая кнопка "В меню"
    fun backToMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }
}

