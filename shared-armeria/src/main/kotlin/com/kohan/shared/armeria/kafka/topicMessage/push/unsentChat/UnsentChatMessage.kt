package com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat

class UnsentChatMessage(
    val content: String,
    val recipientId: String,
)