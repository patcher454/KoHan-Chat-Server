package com.kohan.message.router

import com.kohan.message.router.util.RouteUtil
import com.linecorp.armeria.server.annotation.Param
import com.linecorp.armeria.server.annotation.Post

class TestAPI {
    @Post("/send/{token}")
    fun send(
        @Param("token") token: String,
    ) {
        RouteUtil.route(listOf(token), "안녕하세요")
    }
}
