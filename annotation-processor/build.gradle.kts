plugins {
    java
    `maven-publish`
}

dependencies {
//    implementation(libs.pf4j)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "annotation-processor"
            from(components["java"])
        }
    }
}
