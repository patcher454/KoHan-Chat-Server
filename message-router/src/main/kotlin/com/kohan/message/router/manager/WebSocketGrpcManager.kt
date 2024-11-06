package com.kohan.message.router.manager

import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.catalog.CatalogConsulClient
import com.kohan.shared.armeria.kafka.topicMessage.push.KafkaPushMessageProducer
import com.kohan.proto.websocket.v1.WebSocketServiceGrpcKt
import com.kohan.proto.websocket.v1.Websocket
import com.kohan.shared.armeria.kafka.topicMessage.push.unsentChat.UnsentChatMessage
import com.linecorp.armeria.client.grpc.GrpcClients
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WebSocketGrpcManager {
    private val dotenv = dotenv()
    private val consulApi = createConsulClient()
    private lateinit var session: Map<String, WebSocketServiceGrpcKt.WebSocketServiceCoroutineStub>

    private fun createConsulClient() =
        CatalogConsulClient(
            dotenv["CONSUL_AGENT_HOST"],
            dotenv["CONSUL_SERVER_PORT"]?.toInt() ?: 8500,
        )

    private suspend fun getWebSocketServices() =
        withContext(Dispatchers.IO) {
            consulApi
                .getCatalogService(
                    dotenv["MESSAGE_WEBSOCKET_SERVICE_NAME"],
                    QueryParams.DEFAULT,
                ).value
        }

    private fun createGrpcClient(
        address: String,
        port: Int,
    ) = GrpcClients.newClient(
        "gproto+http://$address:$port/grpc/v1/",
        WebSocketServiceGrpcKt.WebSocketServiceCoroutineStub::class.java,
    )

    private suspend fun updateClientSession(): Map<String, WebSocketServiceGrpcKt.WebSocketServiceCoroutineStub> =
        getWebSocketServices().associate { service ->
            service.serviceId to createGrpcClient(service.address, service.servicePort)
        }

    private suspend fun sendMessage(
        client: WebSocketServiceGrpcKt.WebSocketServiceCoroutineStub,
        message: String,
        receivers: List<Websocket.receiver>,
    ) {
        client.sendMessage(
            Websocket.sendRequest
                .newBuilder()
                .setMessage(message)
                .addAllReceiver(receivers)
                .build(),
        )
    }

    private suspend fun ensureSessionInitialized() {
        if (!::session.isInitialized) {
            session = updateClientSession()
        }
    }

    private suspend fun processRoute(
        server: String,
        devices: List<Websocket.receiver>,
        message: String,
    ) {
        session[server]?.let { client ->
            sendMessage(client, message, devices)
        } ?: handleUnavailableServer(server, devices, message)
    }

    private suspend fun handleUnavailableServer(
        server: String,
        devices: List<Websocket.receiver>,
        message: String,
    ) {
        session.apply { session = updateClientSession() }[server]?.let { client ->
            CoroutineScope(Dispatchers.IO).launch {
                sendMessage(client, message, devices)
            }
        } ?: run {
            devices.forEach{receiver ->
                KafkaPushMessageProducer.sendPush(UnsentChatMessage(message, receiver.userId))
            }
        }
    }

    private suspend fun processAllRoutes(
        routeInfo: Map<String, List<Websocket.receiver>>,
        message: String,
    ) {
        routeInfo.map { (server, devices) ->
            CoroutineScope(Dispatchers.IO).launch {
                processRoute(server, devices, message)
            }
        }
    }

    suspend fun routing(
        routeInfo: Map<String, List<Websocket.receiver>>,
        message: String,
    ) {
        ensureSessionInitialized()
        processAllRoutes(routeInfo, message)
    }
}
