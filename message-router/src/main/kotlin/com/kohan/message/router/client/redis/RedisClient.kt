package com.kohan.message.router.client.redis

import com.kohan.proto.websocket.v1.Websocket
import io.github.cdimascio.dotenv.dotenv
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RedisClient {
    private val redisUri = RedisURI.Builder.redis(dotenv()["REDIS_HOST"]).build()
    private val client = RedisClient.create(redisUri)
    private val commands: RedisAsyncCommands<String, String> = client.connect().async()

    suspend fun getReceivers(userIds: List<String>): Map<String, List<Websocket.receiver>> =
        withContext(Dispatchers.IO) {
            userIds
                .flatMap { userId ->
                    getRouteInfo(userId).entries
                }.groupBy(
                    { it.key },
                    { it.value },
                ).mapValues { (_, lists) -> lists.flatten() }
        }

    private suspend fun getRouteInfo(userId: String): Map<String, List<Websocket.receiver>> =
        withContext(Dispatchers.IO) {
            commands.hgetall(userId).get().entries.groupBy(
                { it.value },
                { toReceiver(userId, it.key) },
            )
        }

    private fun toReceiver(
        userId: String,
        deviceToken: String,
    ): Websocket.receiver =
        Websocket.receiver
            .newBuilder()
            .setUserId(userId)
            .setDeviceToken(deviceToken)
            .build()
}
