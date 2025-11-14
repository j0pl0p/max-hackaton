package org.white_powerbank.bot

import org.white_powerbank.bot.fsm.UserStateManager
import org.white_powerbank.bot.handlers.*
import ru.max.bot.longpolling.LongPollingBot
import ru.max.bot.annotations.UpdateHandler
import ru.max.bot.builders.NewMessageBodyBuilder
import ru.max.bot.builders.attachments.AttachmentsBuilder
import ru.max.botapi.exceptions.ClientException
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.Update
import ru.max.botapi.queries.SendMessageQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.max.bot.annotations.CommandHandler

/**
 * Основной сервис бота, интегрирующийся с MAX SDK
 */
class MaxBotService(
    token: String,
    private val stateManager: UserStateManager,
    private val userRepository: UserRepository
) : LongPollingBot(token) {

    private val router: MessageRouter by lazy {
        val handlers = listOf<Handler>(
            MainMenuHandler(stateManager),
            PartnerHandler(stateManager, userRepository),
            StatisticsHandler(stateManager),
            DiaryHandler(stateManager),
            RelapseHandler(stateManager)
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

        val chatId = update.message?.recipient?.chatId
        val messageId = update.message?.body?.mid

        if (chatId == null) {
            println("Error: chatId is null")
            return
        }

        // Обрабатываем сообщение асинхронно
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // Маршрутизируем сообщение
                val result = router.route(update)

                // Формируем ответ
                val messageBody = if (result.keyboard != null) {
                    NewMessageBodyBuilder.ofText(result.text)
                        .withAttachments(AttachmentsBuilder.inlineKeyboard(result.keyboard))
                        .build()
                } else {
                    NewMessageBodyBuilder.ofText(result.text).build()
                }

                // Отправляем ответ
                SendMessageQuery(client, messageBody)
                    .chatId(chatId)
                    .execute()

                println("Sent message: ${result.text}")
            } catch (e: Exception) {
                println("Error processing message: ${e.message}")
                e.printStackTrace()
                
                // Отправляем сообщение об ошибке
                try {
                    val errorMessage = NewMessageBodyBuilder.ofText(
                        "Произошла ошибка при обработке сообщения. Попробуйте позже."
                    ).build()
                    SendMessageQuery(client, errorMessage)
                        .chatId(chatId)
                        .execute()
                } catch (sendError: Exception) {
                    println("Error sending error message: ${sendError.message}")
                }
            }
        }
    }
}

