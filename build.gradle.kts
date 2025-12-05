plugins {
    java
    id("org.springframework.boot") version "4.0.0" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("io.freefair.lombok") version "9.0.0" apply false
}

subprojects {
    group = "com.blaj"
    version = "0.0.1-SNAPSHOT"
    description = "Open source implementation of Metin2 server"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.annotationProcessor.get())
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
