package com.kohan.shared.armeria.kafka.topicMessage.push

import com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat.UnsentChatMessage
import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

object KafkaPushMessageProducer {
    fun sendUnsentMessage(unsentChatMessage: UnsentChatMessage, bootstrapServer: String, topicName: String) {
        val configs = Properties()
        configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = UnsentChatMessage::class.java.name

        val producer = KafkaProducer<String, UnsentChatMessage>(configs)
        producer.use {
            val record = ProducerRecord(topicName, "", unsentChatMessage)//todo: key 값을 어떻게 넣어야할지 고민
            producer.send(record)
        }
    }
}