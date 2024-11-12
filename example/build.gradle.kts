plugins {
    java
    id("dev.arbjerg.lavalink.gradle-plugin")
}

group = "dev.arbjerg.lavalink.example"
version = "1.0.0"

dependencies {
    // Only here to test dependency handling
    // And Kotlin is best dependency <3
    implementation(kotlin("stdlib", "2.0.21"))
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.7.3")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.9.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

lavalinkPlugin {
    apiVersion = "4.0.8"
    serverVersion = "4.0.8"
}
