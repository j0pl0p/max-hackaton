package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.usecases.DiaryEntryDataUseCase
import org.white_powerbank.usecases.SaveNoteUseCase
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

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
    
    override suspend fun canHandleCallback(update: MessageCallbackUpdate, currentState: BotState): Boolean {
        val payload = MessageUtils.getPayload(update)
        return currentState in listOf(
            BotState.DIARY_CALENDAR,
            BotState.DIARY_SELECT_DATE,
            BotState.DIARY_ENTER_PULL_LEVEL,
            BotState.DIARY_ENTER_NOTE
        ) || payload?.startsWith("diary_") == true
    }
    
    override suspend fun handle(update: MessageCreatedUpdate, currentState: BotState): HandlerResult {
        val userId = update.message?.sender?.userId ?: return HandlerResult("Ошибка")
        val payload = MessageUtils.getPayload(update)
        val text = update.message?.body?.text
        return processDiary(userId, payload, text, currentState)
    }
    
    override suspend fun handleCallback(update: MessageCallbackUpdate, currentState: BotState): HandlerResult {
        val userId = update.callback?.user?.userId ?: return HandlerResult("Ошибка")
        val payload = MessageUtils.getPayload(update)
        return processDiary(userId, payload, null, currentState)
    }
    
    private suspend fun processDiary(userId: Long, payload: String?, text: String?, currentState: BotState): HandlerResult {
        when (currentState) {
            BotState.DIARY_CALENDAR -> return handleCalendar(payload, userId)
            BotState.DIARY_ENTER_PULL_LEVEL -> return handlePullLevel(payload, userId)
            BotState.DIARY_ENTER_NOTE -> return handleNote(payload, text, userId)
            else -> return HandlerResult(
                text = BotTexts.DIARY_CALENDAR_TITLE,
                keyboard = Keyboards.diaryCalendar(),
                newState = BotState.DIARY_CALENDAR
            )
        }
    }
    
    private suspend fun handleCalendar(payload: String?, userId: Long): HandlerResult {
        when {
            payload?.startsWith("diary_calendar_date:") == true -> {
                val dateStr = payload.removePrefix("diary_calendar_date:")
                val dateParts = dateStr.split("-")
                if (dateParts.size == 3) {
                    val date = LocalDate.of(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
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
                    return HandlerResult(
                        text = BotTexts.DIARY_CALENDAR_TITLE,
                        keyboard = Keyboards.diaryCalendar(parts[0].toInt(), parts[1].toInt()),
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
    
    private suspend fun handlePullLevel(payload: String?, userId: Long): HandlerResult {
        if (payload?.startsWith("diary_pull_level:") == true) {
            val level = payload.removePrefix("diary_pull_level:").toIntOrNull()
            if (level != null && level in 0..10) {
                val entryData = diaryEntryDataUseCase.getTemporaryData(userId)
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
    
    private suspend fun handleNote(payload: String?, text: String?, userId: Long): HandlerResult {
        if (payload == "back_to_menu") {
            diaryEntryDataUseCase.deleteTemporaryData(userId)
            return HandlerResult(text = "", keyboard = null, newState = BotState.MAIN_MENU)
        }
        
        if (text != null && text.isNotBlank() && !text.startsWith("/") && payload == null) {
            val entryData = diaryEntryDataUseCase.getTemporaryData(userId)
            val date = entryData?.selectedDate ?: LocalDate.now()
            val pullLevel = entryData?.pullLevel ?: 0
            val user = usersRepository.getUserByMaxId(userId)
            val internalUserId = user?.id ?: userId
            val dateAsDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
            
            try {
                saveNoteUseCase.execute(internalUserId, dateAsDate, pullLevel, text, false)
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
