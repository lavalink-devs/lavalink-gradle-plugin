plugins {
    `kotlin-dsl`
    alias(libs.plugins.gradle.publish)
    `maven-publish`
    alias(libs.plugins.buildconfig)
}

allprojects {
    group = "dev.arbjerg"
    version = "2.0.0"

    repositories {
        mavenCentral()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
}

buildConfig {
    packageName("dev.arbjerg.lavalink.gradle")
    buildConfigField("String", "VERSION", provider { "\"${project.version}\"" })
}

gradlePlugin {
    plugins {
        create("lavalink-gradle-plugin") {
            id = "dev.arbjerg.lavalink.gradle-plugin"
            implementationClass = "dev.arbjerg.lavalink.gradle.LavalinkGradlePlugin"

            displayName = "Lavalink Gradle Plugin"
            description = "Gradle plugin which makes it easier to create Lavalink plugins"
            tags = setOf("lavalink", "discord")
        }

        website = "https://github.com/lavalink-devs/lavalink-gradle-plugin"
        vcsUrl = "https://github.com/lavalink-devs/lavalink-gradle-plugin"
    }
}

publishing {
    repositories {
        maven("https://maven.lavalink.dev/releases") {
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
