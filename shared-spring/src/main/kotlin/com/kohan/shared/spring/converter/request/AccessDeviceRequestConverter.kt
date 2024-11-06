package com.kohan.shared.spring.converter.request

import com.kohan.shared.spring.converter.request.parser.AccessInfoParser
import com.linecorp.armeria.common.AggregatedHttpRequest
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.RequestConverterFunction
import java.lang.reflect.ParameterizedType

class AccessDeviceRequestConverter : RequestConverterFunction {
    override fun convertRequest(
        ctx: ServiceRequestContext,
        request: AggregatedHttpRequest,
        expectedResultType: Class<*>,
        expectedParameterizedResultType: ParameterizedType?,
    ): Any {
        val ip = ctx.remoteAddress().address.hostAddress
        val userAgent = request.headers().get("User-Agent") ?: "Unknown"

        return AccessInfoParser().parse(ip, userAgent)
    }
}
