package com.kohan.push.vo

data class SendNotificationVO(
    val token: String? = null,
    val topic: String? = null,
    val title: String,
    val body: String,
    val image: String? = null,
    val chatRoomId: String,
)
