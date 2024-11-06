package com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat

import com.linecorp.armeria.internal.common.JacksonUtil
import org.apache.kafka.common.serialization.Serializer

class UnsentChatMessageSerializer : Serializer<UnsentChatMessage> {
    override fun serialize(topic: String?, data: UnsentChatMessage?): ByteArray {
        return JacksonUtil.newDefaultObjectMapper().writeValueAsBytes(data)
    }
}