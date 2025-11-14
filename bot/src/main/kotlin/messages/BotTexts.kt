package org.white_powerbank.bot.messages

object BotTexts {
    // Главное меню
    val WELCOME_MESSAGE = """
        Привет! Я помогу тебе бросить курить. 
        Выбери действие из меню ниже.
    """.trimIndent()

    // Напарник
    val NO_PARTNER_MESSAGE = "У вас пока нет напарника"
    fun getPartnerInfo(partnerName: String, days: Int) = """
        Ваш напарник: $partnerName
        Дней без курения: $days
    """.trimIndent()

    // Статистика
    fun getStatistics(lastDay: Int, totalDays: Int) = """
        Среднее: последний день: $lastDay дней
        Всего дней без курения: $totalDays
    """.trimIndent()

    val ACHIEVEMENTS_TITLE = "Достижения:"

    // Дневник
    val DIARY_CALENDAR_TITLE = "Выберите дату для записи:"
    val DIARY_ENTER_PULL_LEVEL = "Оцените тягу к курению от 0 до 10:"
    val DIARY_ENTER_NOTE = "Введите заметку:"
    val DIARY_SAVED = "Запись сохранена!"

    // Сорвался
    val RELAPSE_MESSAGE = "Не унывай даун"

    // Общие
    val INVALID_INPUT = "Некорректный ввод. Попробуйте еще раз."
    val ERROR_OCCURRED = "Произошла ошибка. Попробуйте позже."
}

