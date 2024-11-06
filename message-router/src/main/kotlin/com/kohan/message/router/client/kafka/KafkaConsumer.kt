package com.kohan.message.router.client.kafka

import com.kohan.message.router.util.RouteUtil
import com.kohan.shared.armeria.kafka.topicMessage.chat.KafkaChatMessage
import com.kohan.shared.armeria.kafka.topicMessage.chat.KafkaChatMessageDeserializer
import com.linecorp.armeria.internal.common.JacksonUtil
import io.grpc.internal.JsonUtil
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaConsumer {

    fun start(bootstrapServer: String, topic: String) : Job{
        val configs = Properties()
        configs[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        configs[ConsumerConfig.GROUP_ID_CONFIG] = UUID.randomUUID().toString()
        configs[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        configs[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaChatMessageDeserializer::class.java.name

        return CoroutineScope(Dispatchers.IO).launch {
            val consumer = KafkaConsumer<String, KafkaChatMessage>(configs)
            consumer.use {
                consumer.subscribe(listOf(topic))
                while (true) {
                    consumer
                        .poll(java.time.Duration.ofSeconds(10))
                        .forEach { record ->
                            RouteUtil.route(
                                record.value().recipientIds,
                                record.value().content
                            )
                        }
                }
            }
        }
    }

}