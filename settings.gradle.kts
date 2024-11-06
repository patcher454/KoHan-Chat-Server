plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "kohan-chat-server"
include("shared-armeria")
include("shared-spring")
include("authentication")
include("message-rest")
include("file")
include("message-websocket")
include("push")
include("proto")
include("shared-collection")
include("message-router")
