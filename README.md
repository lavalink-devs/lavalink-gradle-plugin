# Lavalink Gradle Plugin

[![Download](https://img.shields.io/gradle-plugin-portal/v/dev.arbjerg.lavalink.gradle-plugin)](https://plugins.gradle.org/plugin/cdev.arbjerg.lavalink.gradle-plugin)


Gradle plugin which makes it easier to create Lavalink plugins

# Usage

```kotlin
plugins {
    java
    id("dev.arbjerg.lavalink.gradle-plugin") version "{latest.version}"
}

group = "dev.arbjerg.lavalink.example"
version = "1.0.0"

lavalinkPlugin {
    apiVersion = "4.0.0-beta.1"
}
```

# Building the plugin
Running `./gradlew jar` will output a plugin jar to `build/libs`

# Running the Plugin
Running `./gradlew runLavalink` will run a test server with your plugin installed

# Example Project

You can find and example project [here](example)
