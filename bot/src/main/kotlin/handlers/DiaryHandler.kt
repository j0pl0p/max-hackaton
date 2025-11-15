package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.NoteUseCase
import ru.max.botapi.model.Update
import java.time.LocalDate

/**
 * Обработчик сценария "Дневник"
 */
class DiaryHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val usersRepository: UsersRepository,
    private val noteUseCase: NoteUseCase
) : Handler {
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val payload = UpdateUtils.getPayload(update)
        return currentState in listOf(
            BotState.DIARY_CALENDAR,
            BotState.DIARY_SELECT_DATE,
            BotState.DIARY_ENTER_PULL_LEVEL,
            BotState.DIARY_ENTER_NOTE,
            BotState.DIARY_VIEW_NOTE
        ) || payload?.startsWith("diary_") == true
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val userId = UpdateUtils.getUserId(update) ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val payload = UpdateUtils.getPayload(update)
        val text = UpdateUtils.getText(update)
        
        when (currentState) {
            BotState.DIARY_CALENDAR -> return handleCalendar(payload, userId)
            BotState.DIARY_ENTER_PULL_LEVEL -> return handlePullLevel(payload, userId)
            BotState.DIARY_ENTER_NOTE -> return handleNote(text, payload, userId)
            BotState.DIARY_VIEW_NOTE -> return handleViewNote(payload, userId)
            else -> return HandlerResult(
                text = BotTexts.DIARY_CALENDAR_TITLE,
                keyboard = Keyboards.diaryCalendar(),
                newState = BotState.DIARY_CALENDAR
            )
        }
    }
    
    private suspend fun handleCalendar(payload: String?, userId: Long): HandlerResult {
        if (payload == "diary_future_date") {
            return HandlerResult(
                text = "Нельзя создавать заметки на будущие даты.",
                keyboard = Keyboards.diaryCalendar(),
                newState = BotState.DIARY_CALENDAR
            )
        }
        
        if (payload?.startsWith("diary_calendar_date:") == true) {
            val dateStr = payload.removePrefix("diary_calendar_date:")
            val dateParts = dateStr.split("-")
            if (dateParts.size == 3) {
                val date = LocalDate.of(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
                val existingNote = noteUseCase.getNoteByDate(userId, date)
                
                if (existingNote != null) {
                    // Есть заметка - показываем и сохраняем ID для редактирования
                    val user = usersRepository.getUserByMaxId(userId)
                    user?.let {
                        it.tempNoteId = existingNote.id
                        usersRepository.updateUser(it)
                    }
                    val dateFormatted = "${date.dayOfMonth}.${date.monthValue}.${date.year}"
                    return HandlerResult(
                        text = BotTexts.getDiaryNoteView(dateFormatted, existingNote.pullLevel ?: 0, existingNote.body ?: ""),
                        keyboard = Keyboards.diaryViewNote(),
                        newState = BotState.DIARY_VIEW_NOTE
                    )
                } else {
                    // Нет заметки - создаем пустую и сохраняем ID
                    val noteId = noteUseCase.createEmptyNote(userId, date)
                    val user = usersRepository.getUserByMaxId(userId)
                    user?.let {
                        it.tempNoteId = noteId
                        usersRepository.updateUser(it)
                    }
                    return HandlerResult(
                        text = BotTexts.DIARY_NO_NOTE,
                        keyboard = Keyboards.diaryNoNote(),
                        newState = BotState.DIARY_VIEW_NOTE
                    )
                }
            }
        }
        
        if (payload?.startsWith("diary_calendar_month:") == true) {
            val monthData = payload.removePrefix("diary_calendar_month:")
            val parts = monthData.split(":")
            if (parts.size == 2) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                return HandlerResult(
                    text = BotTexts.DIARY_CALENDAR_TITLE,
                    keyboard = Keyboards.diaryCalendar(year, month),
                    newState = BotState.DIARY_CALENDAR
                )
            }
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_CALENDAR_TITLE,
            keyboard = Keyboards.diaryCalendar(),
            newState = BotState.DIARY_CALENDAR
        )
    }
    
    private suspend fun handlePullLevel(payload: String?, userId: Long): HandlerResult {
        if (payload?.startsWith("diary_pull_level:") == true) {
            val level = payload.removePrefix("diary_pull_level:").toIntOrNull()
            if (level != null && level in 0..10) {
                // Обновляем уровень тяги в заметке
                val user = usersRepository.getUserByMaxId(userId)
                val noteId = user?.tempNoteId
                if (noteId != null) {
                    noteUseCase.updatePullLevel(noteId, level)
                }
                
                return HandlerResult(
                    text = BotTexts.DIARY_ENTER_NOTE,
                    keyboard = Keyboards.backToMenu(),
                    newState = BotState.DIARY_ENTER_NOTE
                )
            }
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_ENTER_PULL_LEVEL,
            keyboard = Keyboards.diaryPullLevel(),
            newState = BotState.DIARY_ENTER_PULL_LEVEL
        )
    }
    
    private suspend fun handleNote(text: String?, payload: String?, userId: Long): HandlerResult {
        if (payload == "back_to_menu") {
            val user = usersRepository.getUserByMaxId(userId)
            user?.let {
                it.tempNoteId = null
                usersRepository.updateUser(it)
            }
            return HandlerResult("", null, BotState.MAIN_MENU)
        }
        
        if (text != null && text.isNotBlank() && !text.startsWith("/")) {
            val user = usersRepository.getUserByMaxId(userId)
            val noteId = user?.tempNoteId
            
            if (noteId != null) {
                try {
                    noteUseCase.updateNoteText(noteId, text)
                    
                    // Очищаем tempNoteId
                    user.tempNoteId = null
                    usersRepository.updateUser(user)
                    
                    return HandlerResult(
                        text = BotTexts.DIARY_SAVED,
                        keyboard = Keyboards.backToMenu(),
                        newState = BotState.MAIN_MENU
                    )
                } catch (e: Exception) {
                    return HandlerResult(
                        text = "Ошибка: ${e.message}",
                        keyboard = Keyboards.backToMenu(),
                        newState = BotState.MAIN_MENU
                    )
                }
            }
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_ENTER_NOTE,
            keyboard = Keyboards.backToMenu(),
            newState = BotState.DIARY_ENTER_NOTE
        )
    }
    
    private suspend fun handleViewNote(payload: String?, userId: Long): HandlerResult {
        when (payload) {
            "diary_add_note" -> {
                return HandlerResult(
                    text = BotTexts.DIARY_ENTER_PULL_LEVEL,
                    keyboard = Keyboards.diaryPullLevel(),
                    newState = BotState.DIARY_ENTER_PULL_LEVEL
                )
            }
            "diary_edit_note" -> {
                return HandlerResult(
                    text = BotTexts.DIARY_ENTER_PULL_LEVEL,
                    keyboard = Keyboards.diaryPullLevel(),
                    newState = BotState.DIARY_ENTER_PULL_LEVEL
                )
            }
            "back_to_menu" -> {
                val user = usersRepository.getUserByMaxId(userId)
                user?.let {
                    it.tempNoteId = null
                    usersRepository.updateUser(it)
                }
                return HandlerResult("", null, BotState.MAIN_MENU)
            }
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_NO_NOTE,
            keyboard = Keyboards.diaryNoNote(),
            newState = BotState.DIARY_VIEW_NOTE
        )
    }
}

