import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springVersion = "2.1.4.RELEASE"

plugins {
    id("groovy")
    id("jacoco")
    id("java-library")

    kotlin("plugin.jpa") version "1.3.0"
    kotlin("jvm") version "1.3.0"
    kotlin("plugin.spring") version "1.3.0"
}

group = "de.ebf"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-security:$springVersion")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:$springVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("org.spockframework:spock-spring:1.3-groovy-2.5")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-security:$springVersion")
    testImplementation("org.springframework.boot:spring-boot-configuration-processor:$springVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
