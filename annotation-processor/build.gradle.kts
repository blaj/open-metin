plugins {
    java
}

dependencies {
    implementation(project(":contracts"))
    implementation("com.google.auto.service:auto-service:1.1.1")
    implementation("com.palantir.javapoet:javapoet:0.9.0")

    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testImplementation("org.assertj:assertj-core:3.27.6")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}