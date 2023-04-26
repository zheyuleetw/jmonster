plugins {
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
}

java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    api("org.springframework.boot:spring-boot-starter")
    api("org.jetbrains.kotlin:kotlin-reflect")
    api ("org.springframework.boot:spring-boot-starter-validation:3.0.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
