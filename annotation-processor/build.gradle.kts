plugins {
    java
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "annotation-processor"
            from(components["java"])
        }
    }
}
