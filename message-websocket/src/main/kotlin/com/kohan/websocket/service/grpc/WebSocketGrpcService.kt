package com.kohan.websocket.service.grpc

import com.google.protobuf.Empty
import com.kohan.proto.websocket.v1.WebSocketServiceGrpcKt
import com.kohan.proto.websocket.v1.Websocket
import com.kohan.websocket.manager.WebsocketSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WebSocketGrpcService : WebSocketServiceGrpcKt.WebSocketServiceCoroutineImplBase() {
    override suspend fun sendMessage(request: Websocket.sendRequest): Empty {
        CoroutineScope(Dispatchers.IO).launch {
            WebsocketSessionManager.sendMessage(request.receiverList, request.message)
        }
        return Empty.getDefaultInstance()
    }
}
