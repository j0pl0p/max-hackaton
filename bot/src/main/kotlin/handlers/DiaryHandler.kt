package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import ru.max.botapi.model.MessageCreatedUpdate
import java.time.LocalDate

/**
 * Обработчик сценария "Дневник"
 */
class DiaryHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager
) : Handler {
    
    // Временное хранилище для данных дневника (в реальности должно быть в БД или кэше)
    private val diaryData = mutableMapOf<Long, DiaryEntryData>()
    
    data class DiaryEntryData(
        var selectedDate: LocalDate? = null,
        var pullLevel: Int? = null
    )
    
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
        
        val entryData = diaryData.getOrPut(userId) { DiaryEntryData() }
        
        when (currentState) {
            BotState.DIARY_CALENDAR -> {
                return handleCalendar(update, payload)
            }
            BotState.DIARY_ENTER_PULL_LEVEL -> {
                return handlePullLevel(update, payload, entryData, userId)
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
        payload: String?
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
                    val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка")
                    diaryData.getOrPut(userId) { DiaryEntryData() }.selectedDate = date
                    
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
        entryData: DiaryEntryData,
        userId: Long
    ): HandlerResult {
        if (payload?.startsWith("diary_pull_level:") == true) {
            val levelStr = payload.removePrefix("diary_pull_level:")
            val level = levelStr.toIntOrNull()
            if (level != null && level in 0..10) {
                entryData.pullLevel = level
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
        entryData: DiaryEntryData,
        userId: Long
    ): HandlerResult {
        if (text != null && text.isNotBlank() && !text.startsWith("/") && !text.startsWith("diary_")) {
            val date = entryData.selectedDate ?: LocalDate.now()
            val pullLevel = entryData.pullLevel ?: 0
            
            // TODO: вызвать UseCase для сохранения записи в дневник
            // val note = Note(0, userId, date, false, pullLevel, text)
            // saveNoteUseCase.execute(note)
            
            // Очищаем временные данные
            diaryData.remove(userId)
            
            return HandlerResult(
                text = BotTexts.DIARY_SAVED,
                keyboard = Keyboards.backToMenu(),
                newState = BotState.MAIN_MENU
            )
        }
        
        return HandlerResult(
            text = BotTexts.DIARY_ENTER_NOTE,
            keyboard = Keyboards.backToMenu(),
            newState = BotState.DIARY_ENTER_NOTE
        )
    }
}

