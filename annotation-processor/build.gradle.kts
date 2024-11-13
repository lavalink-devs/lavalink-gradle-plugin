plugins {
    java
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "lavalink-annotation-processor"
            from(components["java"])
        }
    }
}
