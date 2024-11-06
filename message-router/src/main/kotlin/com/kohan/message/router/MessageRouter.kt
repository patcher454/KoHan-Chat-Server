package com.kohan.message.router

import com.kohan.message.router.client.kafka.KafkaConsumer
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.ServerBuilder
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.logging.AccessLogWriter
import com.linecorp.armeria.server.logging.LoggingService
import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory

class MessageRouter

private val logger = LoggerFactory.getLogger(MessageRouter::class.java)

fun main() {
    val server = newServer(dotenv()["MESSAGE_ROUTER_PORT"].toInt())
    server.closeOnJvmShutdown()
    server.start().join()
    logger.info(
        "Server has been started. Serving DocService at http://127.0.0.1:{}/docs",
        server.activeLocalPort(),
    )
//
//    KafkaConsumer.start(
//        dotenv()["KAFKA_BOOTSTRAP_SERVER"],
//        dotenv()["KAFKA_TOPIC"],
//    )
//    logger.info("Kafka consumer has been started.")
}

private fun newServer(port: Int): Server {
    val serverBuilder = Server.builder()
    serverBuilder.http(port)
    configureServices(serverBuilder)
    return serverBuilder.build()
}

private fun configureServices(serverBuilder: ServerBuilder) {
    serverBuilder.annotatedService(TestAPI())
    serverBuilder.serviceUnder("/docs", DocService())
    serverBuilder.decorator(LoggingService.newDecorator())
    serverBuilder.accessLogWriter(AccessLogWriter.combined(), false)
}
