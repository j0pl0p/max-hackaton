package org.white_powerbank.bot.keyboards

import ru.max.bot.builders.attachments.InlineKeyboardBuilder
import ru.max.botapi.model.CallbackButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object Keyboards {
    // Приветственное меню (когда не бросает)
    fun welcomeMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("main_quit", "Бросить вейпить")
            )
    }
    
    // Главное меню (когда бросает)
    fun mainMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
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
                CallbackButton("statistics_awards", "🏆 Мои награды")
            )
            .addRow(
                CallbackButton("back_to_menu", "В меню")
            )
    }
    
    // Награды
    fun awards(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(
                CallbackButton("back_to_menu", "В меню")
            )
    }

    // Дневник - календарь
    fun diaryCalendar(year: Int = LocalDate.now().year, month: Int = LocalDate.now().monthValue): InlineKeyboardBuilder {
        val yearMonth = YearMonth.of(year, month)
        val daysInMonth = yearMonth.lengthOfMonth()
        val monthName = yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("ru"))
        
        var builder = InlineKeyboardBuilder.empty()
        
        // Заголовок с месяцем и годом
        builder = builder.addRow(CallbackButton("diary_month_info", "$monthName $year"))
        
        // Дни месяца по 7 в ряд
        var currentRow = mutableListOf<CallbackButton>()
        for (day in 1..daysInMonth) {
            val dateStr = String.format("%04d-%02d-%02d", year, month, day)
            currentRow.add(CallbackButton("diary_calendar_date:$dateStr", day.toString()))
            
            if (currentRow.size == 7 || day == daysInMonth) {
                builder = builder.addRow(*currentRow.toTypedArray())
                currentRow.clear()
            }
        }
        
        // Навигация по месяцам
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYear = if (month == 1) year - 1 else year
        val nextMonth = if (month == 12) 1 else month + 1
        val nextYear = if (month == 12) year + 1 else year
        
        builder = builder.addRow(
            CallbackButton("diary_calendar_month:$prevYear:$prevMonth", "◀ Пред"),
            CallbackButton("diary_calendar_month:$nextYear:$nextMonth", "След ▶")
        )
        
        builder = builder.addRow(CallbackButton("back_to_menu", "В меню"))
        
        return builder
    }

    // Дневник - выбор уровня тяги (0-10)
    fun diaryPullLevel(): InlineKeyboardBuilder {
        var builder = InlineKeyboardBuilder.empty()
        for (i in 0..10 step 2) {
            val row = mutableListOf<CallbackButton>()
            row.add(CallbackButton("diary_pull_level:$i", i.toString()))
            if (i < 10) {
                row.add(CallbackButton("diary_pull_level:${i + 1}", (i + 1).toString()))
            }
            builder = builder.addRow(*row.toTypedArray())
        }
        builder = builder.addRow(CallbackButton("back_to_menu", "В меню"))
        return builder
    }

    // Сорвался
    fun relapse(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("relapse_diary", "Заметка в дневник"))
            .addRow(CallbackButton("relapse_restart", "Начать заново"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    // Просмотр заметки
    fun diaryViewNote(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("diary_edit_note", "Редактировать заметку"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }
    
    // Нет заметки - предложение добавить
    fun diaryNoNote(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("diary_add_note", "Добавить заметку"))
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }

    // Простая кнопка "В меню"
    fun backToMenu(): InlineKeyboardBuilder {
        return InlineKeyboardBuilder.empty()
            .addRow(CallbackButton("back_to_menu", "В меню"))
    }
}

