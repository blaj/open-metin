plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("io.freefair.lombok")
}

dependencies {
    implementation(project(":contracts"))
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("io.opentelemetry.instrumentation:opentelemetry-netty-4.1:2.23.0-alpha")
    implementation("io.netty:netty-all:4.2.7.Final")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor(project(":annotation-processor"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.testcontainers:testcontainers:2.0.2")
    testImplementation("com.redis:testcontainers-redis:2.2.4")
    testImplementation("com.h2database:h2")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
