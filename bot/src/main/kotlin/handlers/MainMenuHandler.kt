package org.white_powerbank.bot.handlers

import org.white_powerbank.bot.keyboards.Keyboards
import org.white_powerbank.bot.messages.BotTexts
import org.white_powerbank.models.BotState
import org.white_powerbank.usecases.StartQuitUseCase
import org.white_powerbank.usecases.RelapseUseCase
import org.white_powerbank.repositories.UsersRepository
import ru.max.botapi.model.Update
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import ru.max.botapi.model.BotStartedUpdate

/**
 * Обработчик главного меню
 */
class MainMenuHandler(
    private val stateManager: org.white_powerbank.bot.fsm.UserStateManager,
    private val startQuitUseCase: StartQuitUseCase,
    private val relapseUseCase: RelapseUseCase,
    private val usersRepository: UsersRepository
) : Handler {
    
    override suspend fun canHandle(update: Update, currentState: BotState): Boolean {
        val text = UpdateUtils.getText(update)?.trim()?.lowercase()
        val payload = UpdateUtils.getPayload(update)
        
        // Handle bot_started event
        if (update is BotStartedUpdate) return true
        
        // Don't handle main_ payloads if already in target state
        if (payload == "main_partner" && currentState == BotState.PARTNER_MENU) return false
        if (payload == "main_diary" && currentState == BotState.DIARY_CALENDAR) return false
        if (payload == "main_statistics" && currentState == BotState.STATISTICS) return false
        if (payload == "main_relapse" && currentState == BotState.RELAPSE) return false
        
        return text == "/start" || text == "start" || payload == "back_to_menu" ||
               payload?.startsWith("main_") == true ||
               (currentState in listOf(BotState.WELCOME, BotState.MAIN_MENU) && payload == null && text == null)
    }
    
    override suspend fun handle(update: Update, currentState: BotState): HandlerResult {
        val payload = UpdateUtils.getPayload(update)
        val userId = UpdateUtils.getUserId(update) ?: return HandlerResult("Ошибка: не удалось определить пользователя")
        
        // Получаем пользователя для проверки состояния
        val user = usersRepository.getUserByMaxId(userId)
        val isQuitting = user?.isQuitting == true
        
        // Обработка кнопок главного меню
        return when (payload) {
            "main_quit" -> {
                if (isQuitting) {
                    HandlerResult(
                        text = "Вы уже в процессе отказа от курения!",
                        keyboard = Keyboards.mainMenu(),
                        newState = BotState.MAIN_MENU
                    )
                } else {
                    val started = startQuitUseCase.execute(userId)
                    HandlerResult(
                        text = if (started) "Начинаем процесс отказа от курения..." else "Не удалось начать процесс отказа.",
                        keyboard = Keyboards.mainMenu(),
                        newState = BotState.MAIN_MENU
                    )
                }
            }
            "main_partner" -> HandlerResult("", null, BotState.PARTNER_MENU)
            "main_diary" -> HandlerResult("", null, BotState.DIARY_CALENDAR)
            "main_statistics" -> HandlerResult("", null, BotState.STATISTICS)
            "main_relapse" -> {
                if (!isQuitting) {
                    HandlerResult(
                        text = "Вы не находитесь в процессе отказа от курения.",
                        keyboard = Keyboards.welcomeMenu(),
                        newState = BotState.WELCOME
                    )
                } else {
                    relapseUseCase.execute(userId)
                    HandlerResult("", null, BotState.RELAPSE)
                }
            }
            else -> {
                if (isQuitting) {
                    HandlerResult(
                        text = BotTexts.WELCOME_MESSAGE,
                        keyboard = Keyboards.mainMenu(),
                        newState = BotState.MAIN_MENU
                    )
                } else {
                    HandlerResult(
                        text = BotTexts.HELLO_MESSAGE,
                        keyboard = Keyboards.welcomeMenu(),
                        newState = BotState.WELCOME
                    )
                }
            }
        }
    }
}

