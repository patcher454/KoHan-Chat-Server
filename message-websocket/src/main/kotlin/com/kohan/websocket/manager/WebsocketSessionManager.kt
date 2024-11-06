package com.kohan.websocket.manager

import com.kohan.proto.websocket.v1.Websocket
import com.kohan.shared.armeria.kafka.topicMessage.push.KafkaPushMessageProducer
import com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat.UnsentChatMessage
import com.kohan.websocket.client.redis.RedisClient
import com.linecorp.armeria.common.websocket.WebSocketCloseStatus
import com.linecorp.armeria.common.websocket.WebSocketWriter
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object WebsocketSessionManager {
    val serverUUID = UUID.randomUUID().toString()
    private val connections = mutableMapOf<String, WebSocketWriter>()

    suspend fun connectDevice(
        userId: String,
        deviceToken: String,
        writer: WebSocketWriter,
    ) {
        connections[deviceToken] = writer
        RedisClient.connectDevice(userId, deviceToken, serverUUID)
    }

    suspend fun sendMessage(
        receivers: List<Websocket.receiver>,
        message: String,
    ) {
        receivers.forEach { receiver ->
            connections[receiver.deviceToken]?.let { session ->
                if (session.isOpen) {
                    sendMessageToSession(session, message)
                } else {
                    handleClosedSession(receiver.userId, receiver.deviceToken, session, message)
                }
            } ?: run {
                handleMissingSession(receiver.userId, receiver.deviceToken, message)
            }
        }
    }

    private fun sendMessageToSession(
        session: WebSocketWriter,
        message: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            session.write(message)
        }
    }

    private suspend fun handleClosedSession(
        userId: String,
        deviceToken: String,
        session: WebSocketWriter,
        message: String,
    ) {
        disconnectDevice(userId, deviceToken, session)
        KafkaPushMessageProducer.sendUnsentMessage(
            UnsentChatMessage(message, userId),
            dotenv()["KAFKA_BOOTSTRAP_SERVER"],// TODO: consul로 변경
            dotenv()["KAFKA_TOPIC_NAME"]
        )
    }

    private suspend fun handleMissingSession(
        userId: String,
        deviceToken: String,
        message: String,
    ) {
        disconnectDevice(userId, deviceToken, null)
        KafkaPushMessageProducer.sendUnsentMessage(
            UnsentChatMessage(message, userId),
            dotenv()["KAFKA_BOOTSTRAP_SERVER"],// TODO: consul로 변경
            dotenv()["KAFKA_TOPIC_NAME"]
        )    }

    private suspend fun disconnectDevice(
        userId: String,
        deviceToken: String,
        writer: WebSocketWriter?,
    ) {
        writer?.let { closeWriterIfOpen(it) }
        connections.remove(deviceToken)
        RedisClient.disconnectDevice(userId, deviceToken)
    }

    private fun closeWriterIfOpen(writer: WebSocketWriter) {
        if (writer.isOpen) {
            writer.close(WebSocketCloseStatus.NORMAL_CLOSURE)
        }
    }
}
