package org.white_powerbank

import org.jetbrains.exposed.sql.insert
import org.white_powerbank.db.DatabaseFactory
import org.white_powerbank.repositories.*
import org.white_powerbank.models.*
import java.util.Date
import java.time.LocalDateTime
import java.time.LocalDate

fun main() {
    println("=== ТЕСТ ФУНКЦИОНАЛА БАЗЫ ДАННЫХ ===")
    
    // Инициализация базы данных
    DatabaseFactory.init(
        jdbcUrl = "jdbc:postgresql://localhost:5432/school_reviews",
        user = "user",
        password = "123"
    )

    val usersRepo = UsersRepositoryImpl()
    val notesRepo = NotesRepositoryImpl()
    val awardsRepo = AwardsRepositoryImpl()
    val userAwardsRepo = UsersAwardsRepositoryImpl()

    // 1. ТЕСТ ПОЛЬЗОВАТЕЛЕЙ
    println("\n1. Тестируем пользователей...")
    
    val user1 = User(
        id = 0, maxId = 12345, state = BotState.MAIN_MENU,
        partnerId = null, partnerSearchStatus = PartnerSearchStatus.ACTIVE,
        lastActivity = LocalDateTime.now(), isQuitting = false,
        lastStart = LocalDate.now(), averageMonthlyExpenses = 50000, maxStreak = 0,
        profileLink = null
    )
    
    val user2 = User(
        id = 0, maxId = 67890, state = BotState.DIARY_CALENDAR,
        partnerId = null, partnerSearchStatus = PartnerSearchStatus.INACTIVE,
        lastActivity = LocalDateTime.now().minusDays(1), isQuitting = false,
        lastStart = LocalDate.now().minusDays(5), averageMonthlyExpenses = 75000, maxStreak = 0,
        profileLink = null
    )

    usersRepo.addUser(user1)
    usersRepo.addUser(user2)
    println("✓ Добавлены 2 пользователя")
    
    val foundUser = usersRepo.getUserByMaxId(12345)
    println("✓ Найден пользователь: ${foundUser?.maxId}")
    
    // Обновляем пользователя
    foundUser?.let {
        it.state = BotState.STATISTICS
        usersRepo.updateUser(it)
        println("✓ Обновлен статус пользователя")
    }

    // 2. ТЕСТ ЗАМЕТОК
    println("\n2. Тестируем заметки...")
    
    val note1 = Note(0, 12345, Date(), 8, false, "Отличный день!")
    val note2 = Note(0, 67890, Date(System.currentTimeMillis() - 86400000), 5, true, "Сложный день")
    val note3 = Note(0, 12345, Date(System.currentTimeMillis() - 172800000), 7, false, "Хороший прогресс")
    
    val noteId1 = notesRepo.addNote(note1)
    val noteId2 = notesRepo.addNote(note2)
    val noteId3 = notesRepo.addNote(note3)
    println("✓ Добавлены 3 заметки")
    
    val userNotes = notesRepo.getNotesByUserId(12345)
    println("✓ Найдено ${userNotes.size} заметок для пользователя 12345")
    
    val noteById = notesRepo.getNoteById(noteId1)
    println("✓ Найдена заметка по ID: ${noteById?.body}")
    
    val noteByDate = notesRepo.getNoteByUserIdAndDate(12345, Date())
    println("✓ Найдена заметка по дате: ${noteByDate?.body}")
    
    // Обновляем заметку
    noteById?.let {
        it.body = "Обновленная заметка!"
        notesRepo.updateNote(it)
        println("✓ Заметка обновлена")
    }
    
    // Тест диапазона дат
    val dateFrom = Date(System.currentTimeMillis() - 259200000) // 3 дня назад
    val dateTo = Date()
    val notesInRange = notesRepo.getNotesByUserIdAndDates(12345, dateFrom, dateTo)
    println("✓ Найдено ${notesInRange.size} заметок в диапазоне дат")

    // 3. ТЕСТ НАГРАД (создаем тестовые награды)
    println("\n3. Тестируем награды...")
    
    // Создаем награды напрямую в БД для тестирования
    org.jetbrains.exposed.sql.transactions.transaction {
        org.white_powerbank.db.tables.AwardsTable.insert {
            it[name] = "Первая неделя"
            it[description] = "Продержался неделю без срывов"
            it[picturePath] = "/images/week1.png"
        }
        org.white_powerbank.db.tables.AwardsTable.insert {
            it[name] = "Месяц силы"
            it[description] = "Целый месяц чистоты"
            it[picturePath] = "/images/month1.png"
        }
    }
    
    val awards = awardsRepo.getAwards()
    println("✓ Найдено ${awards.size} наград")
    
    val firstAward = awardsRepo.getAward(1)
    println("✓ Первая награда: ${firstAward?.name}")

    // 4. ТЕСТ ПОЛЬЗОВАТЕЛЬСКИХ НАГРАД
    println("\n4. Тестируем связь пользователей и наград...")
    
    if (awards.isNotEmpty() && foundUser != null) {
        val userId = foundUser.id // Используем реальный id из БД
        userAwardsRepo.addAwardToUser(userId, awards[0].id)
        if (awards.size > 1) {
            userAwardsRepo.addAwardToUser(userId, awards[1].id)
        }
        println("✓ Добавлены награды пользователю")
        
        val userAwards = userAwardsRepo.getAwardsByUserId(userId)
        println("✓ У пользователя ${userAwards.size} наград")
        
        if (userAwards.isNotEmpty()) {
            userAwardsRepo.removeAwardFromUser(userId, userAwards[0].id)
            println("✓ Удалена одна награда")
        }
    }

    // 5. ТЕСТ УДАЛЕНИЯ
    println("\n5. Тестируем удаление...")
    
    notesRepo.deleteNote(noteId3)
    println("✓ Заметка удалена")
    
    val remainingNotes = notesRepo.getNotesByUserId(12345)
    println("✓ Осталось ${remainingNotes.size} заметок")

    println("\n=== ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ УСПЕШНО! ===")
    println("Проверьте данные в pgAdmin: http://localhost:5051")
    println("Логин: 123, Пароль: 123")
}