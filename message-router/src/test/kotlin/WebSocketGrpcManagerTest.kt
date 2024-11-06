package com.kohan.message.router.manager

import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.ecwid.consul.v1.Response
import com.ecwid.consul.v1.catalog.model.CatalogService
import com.kohan.proto.websocket.v1.WebSocketServiceGrpcKt
import com.kohan.proto.websocket.v1.Websocket
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class WebSocketGrpcManagerTest {

    private lateinit var consulClientMock: ConsulClient
    private lateinit var grpcClientMock: WebSocketServiceGrpcKt.WebSocketServiceCoroutineStub

    @BeforeEach
    fun setup() {
        consulClientMock = mockk()
        grpcClientMock = mockk()

        // WebSocketGrpcManager의 private 멤버와 메소드를 모킹하기 위한 설정
        mockkObject(WebSocketGrpcManager)
        every { WebSocketGrpcManager["consulApi"] } returns consulClientMock
        every { WebSocketGrpcManager["createGrpcClient"](any(), any()) } returns grpcClientMock
    }

    @Test
    fun `routing should send message to correct servers`() = runBlocking {
        // Given
        val routeInfo = mapOf(
            "server1" to listOf(Websocket.receiver.newBuilder().setUserId("user1").build()),
            "server2" to listOf(Websocket.receiver.newBuilder().setUserId("user2").build())
        )
        val message = "Test message"

        val catalogServices = listOf(
            CatalogService().apply { 
                serviceId = "server1"
                address = "localhost"
                servicePort = 8080
            },
            CatalogService().apply { 
                serviceId = "server2"
                address = "localhost"
                servicePort = 8081
            }
        )

        coEvery { consulClientMock.getCatalogService(any(), any()) } returns Response(catalogServices)
        coEvery { grpcClientMock.sendMessage(any()) } just Runs

        // When
        WebSocketGrpcManager.routing(routeInfo, message)

        // Then
        coVerify(exactly = 2) { grpcClientMock.sendMessage(any()) }
    }

    @Test
    fun `routing should handle unavailable server`() = runBlocking {
        // 테스트 구현
    }

    @Test
    fun `routing should update session when server is not found`() = runBlocking {
        // 테스트 구현
    }

    // 추가 테스트 케이스...
}

