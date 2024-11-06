package com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat

import com.linecorp.armeria.internal.common.JacksonUtil
import org.apache.kafka.common.serialization.Deserializer

class UnsentChatMessageDeserializer : Deserializer<UnsentChatMessage> {
    override fun deserialize(topic: String?, data: ByteArray?): UnsentChatMessage {
        return JacksonUtil.newDefaultObjectMapper().readValue(data, UnsentChatMessage::class.java)
    }
}