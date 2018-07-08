package com.coffee.raven

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.github.bassaer.chatmessageview.util.ChatBot
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val ACCESS_TOKEN = "c958c18974944cab959689124d0413ba"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FuelManager.instance.baseHeaders = mapOf(
                "Authorization" to "Bearer $ACCESS_TOKEN"
        )

        FuelManager.instance.basePath =
                "https://api.dialogflow.com/v1/"

        FuelManager.instance.baseParams = listOf(
                "v" to "20180616",                  // latest protocol
                "sessionId" to UUID.randomUUID(),   // random ID
                "lang" to "en"                      // English language
        )

        val user = ChatUser(1,
                "You",
                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_icon))

        val agent = ChatUser(2,
                "Raven",
                BitmapFactory.decodeResource(resources, R.mipmap.ic_raven_icon))

        chat_view.setOnClickSendButtonListener(
                View.OnClickListener {
                    chat_view.send(Message.Builder()
                            .setUser(user)
                            .setText(chat_view.inputText)
                            .build())

                    Fuel.get("/query",
                            listOf("query" to chat_view.inputText))
                            .responseJson { _, _, result ->
                                val reply = result.get().obj()
                                        .getJSONObject("result")
                                        .getJSONObject("fulfillment")
                                        .getString("speech")

                                chat_view.send(Message.Builder()
                                        .setRight(true)
                                        .setUser(agent)
                                        .setText(reply)
                                        .build())

                                val intent: String? = result.get().obj()
                                        .getJSONObject("result")
                                        .optJSONObject("metadata")
                                        .optString("intentName")

                                if(intent!! == "Add Note") {
                                    val note = result.get().obj()
                                            .getJSONObject("result")
                                            .getJSONObject("parameters")
                                            .getString("any")

                                    chat_view.send(Message.Builder()
                                            .setRight(true)
                                            .setUser(agent)
                                            .setText("The note $note has been saved")
                                            .build())
                                }
                            }
                    chat_view.inputText = ""

                }
        )
    }
}
