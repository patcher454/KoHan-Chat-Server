package com.kohan.shared.armeria.kafka.topicMessage.chat

import com.linecorp.armeria.internal.common.JacksonUtil
import org.apache.kafka.common.serialization.Serializer

class KafkaChatMessageSerializer : Serializer<KafkaChatMessage> {
    override fun serialize(topic: String?, data: KafkaChatMessage): ByteArray {
        return JacksonUtil.newDefaultObjectMapper().writeValueAsBytes(data)
    }
}