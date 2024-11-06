package com.kohan.websocket.exception.error

import com.linecorp.armeria.common.websocket.WebSocketCloseStatus

enum class WebSocketErrorCode(
    val webSocketCloseStatus: WebSocketCloseStatus,
) {
    UNAUTHORIZED(
        WebSocketCloseStatus.ofPrivateUse(3000, "Unauthorized"),
    ),
}
