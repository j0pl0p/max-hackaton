package org.white_powerbank.bot

import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.bot.handlers.*
import org.white_powerbank.repositories.UsersRepository
import org.white_powerbank.repositories.NotesRepository
import org.white_powerbank.repositories.UsersAwardsRepository
import org.white_powerbank.usecases.*
import ru.max.bot.longpolling.LongPollingBot
import ru.max.bot.annotations.UpdateHandler
import ru.max.bot.builders.NewMessageBodyBuilder
import ru.max.bot.builders.attachments.AttachmentsBuilder
import ru.max.botapi.exceptions.ClientException
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.MessageCallbackUpdate
import ru.max.botapi.model.Update
import ru.max.botapi.queries.SendMessageQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MaxBotService(
    token: String,
    private val stateManager: UserStateManager,
    private val userRepository: UsersRepository,
    private val notesRepository: NotesRepository,
    private val usersAwardsRepository: UsersAwardsRepository,
    private val awardsRepository: org.white_powerbank.repositories.AwardsRepository,
    private val relapseUseCase: RelapseUseCase
) : LongPollingBot(token) {

    private val matchPartnersUseCase = MatchPartnersUseCase(userRepository)
    private val searchPairUseCase = SearchPairUseCase(userRepository, matchPartnersUseCase)
    private val changePartnerUseCase = ChangePartnerUseCase(userRepository, matchPartnersUseCase)
    private val getPartnerInfoUseCase = GetPartnerInfoUseCase(userRepository, notesRepository)
    private val checkAwardsUseCase = CheckAwardsUseCase(userRepository, awardsRepository, usersAwardsRepository)
    private val getStatisticsUseCase = GetStatisticsUseCase(userRepository, notesRepository, checkAwardsUseCase)
    private val getAchievementsUseCase = GetAchievementsUseCase(usersAwardsRepository, userRepository)
    private val startQuitUseCase = StartQuitUseCase(userRepository)
    private val saveNoteUseCase = SaveNoteUseCase(notesRepository)
    private val restartQuitUseCase = RestartQuitUseCase(userRepository)
    private val noteUseCase = NoteUseCase(notesRepository, userRepository)

    private val router: MessageRouter by lazy {
        val handlers = listOf<Handler>(
            MainMenuHandler(stateManager, startQuitUseCase, relapseUseCase, userRepository),
            PartnerHandler(stateManager, userRepository, searchPairUseCase, changePartnerUseCase, getPartnerInfoUseCase),
            StatisticsHandler(stateManager, getStatisticsUseCase, getAchievementsUseCase),
            DiaryHandler(stateManager, userRepository, noteUseCase),
            RelapseHandler(stateManager, restartQuitUseCase)
        )
        MessageRouter(stateManager, userRepository, handlers)
    }

    override fun start() {
        super.start()
        println("MaxBotService started")
    }

    override fun onUpdate(update: Update) {
        super.onUpdate(update)
        println("Received update: $update")
    }

    override fun stop() {
        super.stop()
        println("MaxBotService stopped")
    }

    @UpdateHandler
    @Throws(ClientException::class)
    fun onMessageCreated(update: MessageCreatedUpdate) {
        println("Received message update: $update")
        val chatId = update.message?.recipient?.chatId ?: return
        processUpdate(update, chatId)
    }

    @UpdateHandler
    @Throws(ClientException::class)
    fun onMessageCallback(update: MessageCallbackUpdate) {
        println("Received callback update: $update")
        val chatId = update.message?.recipient?.chatId ?: return
        processUpdate(update, chatId)
    }

    private fun processUpdate(update: Update, chatId: Long) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val result = router.route(update)

                // Skip sending message if text is empty (state transitions only)
                if (result.text.isNotEmpty()) {
                    val messageBody = if (result.keyboard != null) {
                        NewMessageBodyBuilder.ofText(result.text)
                            .withAttachments(AttachmentsBuilder.inlineKeyboard(result.keyboard))
                            .build()
                    } else {
                        NewMessageBodyBuilder.ofText(result.text).build()
                    }

                    SendMessageQuery(client, messageBody).chatId(chatId).execute()
                    println("Sent message: ${result.text}")
                } else {
                    println("State transition only, no message sent")
                }
            } catch (e: Exception) {
                println("Error processing message: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
