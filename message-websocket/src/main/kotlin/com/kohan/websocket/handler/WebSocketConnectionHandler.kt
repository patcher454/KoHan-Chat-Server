package com.kohan.websocket.handler

import com.kohan.websocket.manager.WebsocketSessionManager
import com.linecorp.armeria.common.websocket.WebSocket
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.websocket.WebSocketServiceHandler
import io.netty.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class WebSocketConnectionHandler : WebSocketServiceHandler {
    override fun handle(
        ctx: ServiceRequestContext,
        `in`: WebSocket,
    ): WebSocket {
        val userId: String = ctx.attr(AttributeKey.valueOf("userId"))!!
        val deviceToken = UUID.randomUUID().toString()
        val writer = WebSocket.streaming()

        CoroutineScope(Dispatchers.IO).launch {
            WebsocketSessionManager.connectDevice(userId, deviceToken, writer)
        }
        return writer
    }
}
