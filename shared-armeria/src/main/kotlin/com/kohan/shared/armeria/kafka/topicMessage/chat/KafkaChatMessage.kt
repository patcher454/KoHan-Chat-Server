package com.kohan.shared.armeria.kafka.topicMessage.chat

import com.linecorp.armeria.internal.common.JacksonUtil

class KafkaChatMessage(
    private val _content: Any,
    val recipientIds: List<String>,
)
{
    val content: String
        get() = JacksonUtil.newDefaultObjectMapper().writeValueAsString(_content)
}