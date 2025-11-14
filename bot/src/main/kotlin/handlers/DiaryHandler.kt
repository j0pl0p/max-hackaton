package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.DiaryEntryDataUseCase
import org.white_powerbank.usecases.SaveNoteUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Обработчик сценария "Дневник"
 */
class DiaryHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val usersRepository: UsersRepository,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val diaryEntryDataUseCase: DiaryEntryDataUseCase
) : Handler {
    
    override suspend fun canHandle(update: MessageCreatedUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        return currentState in listOf(
            BotState.DIARY_CALENDAR,
            BotState.DIARY_SELECT_DATE,
            BotState.DIARY_ENTER_PULL_LEVEL,
            BotState.DIARY_ENTER_NOTE
        ) || payload?.startsWith("diary_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        val payload = MessageUtils.getPayload(update)
        val text = update.message?.body?.text
        
        // Получаем временные данные из репозитория
        val entryData = diaryEntryDataUseCase.getTemporaryData(userId)
        
        when (currentState) {
            BotState.DIARY_CALENDAR -> {
                return handleCalendar(update, payload, userId)
            }
            BotState.DIARY_ENTER_PULL_LEVEL -> {
                return handlePullLevel(update, payload, userId)
            }
            BotState.DIARY_ENTER_NOTE -> {
                return handleNote(update, text, entryData, userId)
            }
            else -> {
                return HandlerResult(
                    text = BotTexts.DIARY_CALENDAR_TITLE,
                    keyboard = Keyboards.diaryCalendar(),
                    newState = BotState.DIARY_CALENDAR
                )
            }
        }
    }
    
    private suspend fun handleCalendar(
        update: MessageCreatedUpdate,
        payload: String?,
        userId: Long
    ): HandlerResult {
        when {
            payload?.startsWith("diary_calendar_date:") == true -> {
                val dateStr = payload.removePrefix("diary_calendar_date:")
                val dateParts = dateStr.split("-")
                if (dateParts.size == 3) {
                    val date = LocalDate.of(
                        dateParts[0].toInt(),
                        dateParts[1].toInt(),
                        dateParts[2].toInt()
                    )
                    // Сохраняем выбранную дату через use case
                    diaryEntryDataUseCase.saveTemporaryData(userId, date, null)
                    
                    return HandlerResult(
                        text = BotTexts.DIARY_ENTER_PULL_LEVEL,
                        keyboard = Keyboards.diaryPullLevel(),
                        newState = BotState.DIARY_ENTER_PULL_LEVEL
                    )
                }
            }
            payload?.startsWith("diary_calendar_month:") == true -> {
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
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_CALENDAR_TITLE,
            keyboard = Keyboards.diaryCalendar(),
            newState = BotState.DIARY_CALENDAR
        )
    }
    
    private suspend fun handlePullLevel(
        update: MessageCreatedUpdate,
        payload: String?,
        userId: Long
    ): HandlerResult {
        if (payload?.startsWith("diary_pull_level:") == true) {
            val levelStr = payload.removePrefix("diary_pull_level:")
            val level = levelStr.toIntOrNull()
            if (level != null && level in 0..10) {
                // Получаем текущие временные данные
                val entryData = diaryEntryDataUseCase.getTemporaryData(userId)
                // Сохраняем уровень тяги через use case
                diaryEntryDataUseCase.saveTemporaryData(userId, entryData?.selectedDate, level)
                
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
    
    private suspend fun handleNote(
        update: MessageCreatedUpdate,
        text: String?,
        entryData: org.white_powerbank.usecases.DiaryEntryData?,
        userId: Long
    ): HandlerResult {
        val payload = MessageUtils.getPayload(update)
        if (payload == "back_to_menu") {
            // Удаляем временные данные через use case
            diaryEntryDataUseCase.deleteTemporaryData(userId)
            return HandlerResult(
                text = "",
                keyboard = null,
                newState = BotState.MAIN_MENU
            )
        }
        
        if (text != null && text.isNotBlank() && !text.startsWith("/") && payload == null) {
            val date = entryData?.selectedDate ?: LocalDate.now()
            val pullLevel = entryData?.pullLevel ?: 0
            
            // Получаем внутренний ID пользователя
            val user = usersRepository.getUserByMaxId(userId)
            val internalUserId = user?.id ?: userId
            
            // Конвертируем LocalDate в Date
            val dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            
            // Сохраняем запись через UseCase
            try {
                saveNoteUseCase.execute(
                    userId = internalUserId,
                    date = dateAsDate,
                    pullLevel = pullLevel,
                    body = text,
                    failed = false
                )
                
                // Очищаем временные данные через use case
                diaryEntryDataUseCase.deleteTemporaryData(userId)
                
                return HandlerResult(
                    text = BotTexts.DIARY_SAVED,
                    keyboard = Keyboards.backToMenu(),
                    newState = BotState.MAIN_MENU
                )
            } catch (e: Exception) {
                return HandlerResult(
                    text = "Ошибка при сохранении записи: ${e.message}",
                    keyboard = Keyboards.backToMenu(),
                    newState = BotState.DIARY_ENTER_NOTE
                )
            }
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_ENTER_NOTE,
            keyboard = Keyboards.backToMenu(),
            newState = BotState.DIARY_ENTER_NOTE
        )
    }
}

