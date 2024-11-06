package com.kohan.message.router.util

import com.kohan.message.router.client.redis.RedisClient
import com.kohan.message.router.manager.WebSocketGrpcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RouteUtil {
    fun route(
        recipientIds: List<String>,
        message: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val routingMap = RedisClient.getReceivers(recipientIds)
            WebSocketGrpcManager.routing(routingMap, message)
        }
    }
}
