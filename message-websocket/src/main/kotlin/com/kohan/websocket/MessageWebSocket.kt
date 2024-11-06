package com.kohan.websocket

import com.ecwid.consul.v1.agent.AgentConsulClient
import com.ecwid.consul.v1.agent.model.NewService
import com.kohan.shared.armeria.config.TokenValidationService
import com.kohan.websocket.handler.WebSocketConnectionHandler
import com.kohan.websocket.manager.WebsocketSessionManager
import com.kohan.websocket.service.grpc.WebSocketGrpcService
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.server.websocket.WebSocketService
import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory

class MessageWebSocket

private val logger = LoggerFactory.getLogger(MessageWebSocket::class.java)

private val dotenv = dotenv()

fun main(args: Array<String>) {
    val serverUUID = WebsocketSessionManager.serverUUID

    val server = newServer(dotenv["WEB_SOCKET_SERVER_PORT"].toInt())
    server.closeOnJvmShutdown()
    server.start().join()
    logger.info(
        "Server has been started. Serving DocService at http://127.0.0.1:{}/docs",
        server.activeLocalPort(),
    )

    if (!dotenv["DEV_MODE"].toBoolean()) {
        try {
            val agentClient =
                AgentConsulClient(
                    dotenv["CONSUL_AGENT_HOST"],
                    dotenv["CONSUL_SERVER_PORT"]?.toInt() ?: 8500,
                )

            agentClient.agentServiceRegister(newConsulService(server.activeLocalPort()))
            logger.info(
                "Consul Agent Connected. ServerUUID={}, Server port={}",
                serverUUID,
                server.activeLocalPort(),
            )
        } catch (e: Exception) {
            logger.error("Consul Agent Connection Fail {}", e.message)
            server.closeOnJvmShutdown()
        }
    }
}

private fun newServer(port: Int): Server {
    val serverBuilder = Server.builder()
    serverBuilder.http(port)
    configureServices(serverBuilder)
    return serverBuilder.build()
}

private fun configureServices(serverBuilder: ServerBuilder) {
    serverBuilder.service(
        "/receiveMessage",
        WebSocketService
            .builder(WebSocketConnectionHandler())
            .allowedOrigins("*")
            .build()
            .decorate { delegate ->
                TokenValidationService(delegate)
            },
    )

    serverBuilder.service(
        "prefix:/grpc/v1",
        GrpcService
            .builder()
            .addService(WebSocketGrpcService())
            .enableUnframedRequests(true)
            .build(),
    )
    serverBuilder.serviceUnder("/docs", DocService())
    serverBuilder.decorator(LoggingService.newDecorator())
    serverBuilder.accessLogWriter(AccessLogWriter.combined(), false)
}

private fun newConsulService(port: Int): NewService {
    val service = NewService()
    service.id = WebsocketSessionManager.serverUUID
    service.port = port
    service.name = dotenv["MESSAGE_WEBSOCKET_SERVICE_NAME"]
    service.tags = listOf("websocket", "v0.0.1")
    return service
}
