package com.kohan.shared.armeria.kafka.topicMessage.chat

import com.linecorp.armeria.internal.common.JacksonUtil
import org.apache.kafka.common.serialization.Deserializer

class KafkaChatMessageDeserializer: Deserializer<KafkaChatMessage> {
    override fun deserialize(topic: String?, data: ByteArray?): KafkaChatMessage {
        return JacksonUtil.newDefaultObjectMapper().readValue(data, KafkaChatMessage::class.java)
    }
}