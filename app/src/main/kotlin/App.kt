package org.white_powerbank.app


import okhttp3.internal.wait
import ru.max.bot.longpolling.LongPollingBot
import ru.max.bot.annotations.UpdateHandler
import ru.max.bot.annotations.CommandHandler
import ru.max.bot.builders.NewMessageBodyBuilder
import ru.max.botapi.exceptions.ClientException
import ru.max.botapi.model.ChatAdmin
import ru.max.botapi.model.ChatAdminPermission
import ru.max.botapi.model.ChatAdminsList
import ru.max.botapi.model.MessageCreatedUpdate
import ru.max.botapi.model.Update
import ru.max.botapi.queries.SendMessageQuery
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import ru.max.botapi.model.Message
import ru.max.botapi.model.TextFormat
import ru.max.botapi.queries.DeleteMessageQuery
import ru.max.botapi.queries.EditMessageQuery
import ru.max.botapi.queries.PostAdminsQuery


import kotlin.jvm.Throws


val token = System.getenv("MAX_TOKEN")


class ReplyBot(val token: String): LongPollingBot(token) {

    override fun start() {
        super.start()
        println("bot started with token: $token")
    }

    override fun onUpdate(update: Update) {
        super.onUpdate(update)
        println("Received update: $update")
    }

    override fun stop() {
        super.stop()
        println("bot stopped")
    }

    @UpdateHandler
    @Throws(ClientException::class)
    fun onMessageCreated(update: MessageCreatedUpdate) {

        println("got message")
        println("Received update: $update")

        val text = update.message?.body?.text

        println("Received message: ${text}")

        val mid = update.message?.body?.mid

        DeleteMessageQuery(client, mid).execute()

        println("message deleted")



        val user_id = update.message?.sender?.userId
        val chat_admins = ChatAdminsList(listOf(ChatAdmin(user_id, setOf<ChatAdminPermission>(ChatAdminPermission.ADD_ADMINS))))
        val chat_id = update.message?.recipient?.chatId

        println("user_id: $user_id\nchat_admins: $chat_admins\nchat_id: $chat_id")

        try {
            val res = PostAdminsQuery(
                client,
                chat_admins,
                chat_id
            ).execute()

            println("PostAdminsResult:\n$res")
        } catch (e: Exception) {
            println("Error: $e")
        }


        val replyMessage = NewMessageBodyBuilder.ofText(text?: "не получил текст:(").build()

        SendMessageQuery(client, replyMessage).chatId(update.message?.recipient?.chatId).execute()


        val mentionMessage = NewMessageBodyBuilder.ofText("[User mention](https://max.ru/%$user_id%) (ссылка откроется, если у вас установлен MAX)", TextFormat.MARKDOWN).build()
        val mention_res = SendMessageQuery(client, mentionMessage).chatId(update.message?.recipient?.chatId).execute()

        println("mention res: $mention_res")

        println("Sent message: ${text?: "не получил текст:("}")
    }
}

fun main() {


    println("Starting bot with token $token")
    val bot = ReplyBot(token)

    try {
        bot.start()
    } catch (e: Exception) {
        println("Error: $e")
    }


    sleep(100000000)

    bot.stop()









}
