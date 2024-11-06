plugins {
}

dependencies {
    implementation(project(":shared-armeria"))
    implementation(libs.redis.lettuce.core)
    implementation(libs.consul.api)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.kohan.message.router.MessageRouterKt"
    }
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
