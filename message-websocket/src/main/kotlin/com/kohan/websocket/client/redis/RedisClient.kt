package com.kohan.websocket.client.redis

import io.github.cdimascio.dotenv.dotenv
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RedisClient {
    private val redisUri =
        RedisURI.Builder
            .redis(dotenv()["REDIS_HOST"])
            .build()

    private val client = RedisClient.create(redisUri)
    private var commands = client.connect().async()

    suspend fun connectDevice(
        userId: String,
        deviceToken: String,
        serverUUID: String,
    ) {
        withContext(Dispatchers.IO) {
            commands.hset(userId, deviceToken, serverUUID).get()
        }
    }

    suspend fun disconnectDevice(
        userId: String,
        deviceToken: String,
    ) {
        withContext(Dispatchers.IO) {
            commands.hdel(userId, deviceToken).get()
        }
    }
}
