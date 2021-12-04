import java.math.BigDecimal
import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("groovy")
    id("jacoco")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.6.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.6.0"
    kotlin("kapt") version "1.6.0"
    kotlin("plugin.jpa") version "1.6.0"
    kotlin("plugin.spring") version "1.6.0"
}

group = "de.ebf"
version = "0.1.0"

repositories {
    mavenCentral()
}

idea {
    module {
        outputDir = file("build/classes/main")
        testOutputDir = file("build/classes/test")
    }
}

dependencies {
    /* Spring and Spring Boot dependencies */
    compileOnly("org.springframework.security:spring-security-core")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    /* Java Validation API dependency */
    compileOnly("javax.validation:validation-api")

    /* Kotlin dependencies */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    /* Process Spring Boot properties classes */
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")

    /* Spring and common test dependencies */
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.hibernate.validator:hibernate-validator")
    testImplementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    /* Spock dependencies */
    testImplementation("org.codehaus.groovy:groovy-all:3.0.9")
    testImplementation("org.spockframework:spock-core:2.0-groovy-3.0")
    testImplementation("org.spockframework:spock-spring:2.0-groovy-3.0")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:2.6.1")
    }
}

java {
    withSourcesJar()
}

jacoco {
    toolVersion = "0.8.7"
    reportsDir = buildDir.resolve("jacoco")
}

tasks.test {
    useJUnitPlatform()

    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        csv.isEnabled = false
        xml.isEnabled = true
        html.isEnabled = true
    }

    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal.valueOf(0.9) // at least 90% coverage is required
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.withType<DokkaTask> {
    outputDirectory.set(buildDir.resolve("javadoc"))
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Documentation for Spring JSON API Errors library"
    archiveClassifier.set("javadoc")
    from(tasks.getting(DokkaTask::class))
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(dokkaJar)
        }
    }

    repositories {
        maven {
            if(project.version.toString().contains("-SNAPSHOT")) {
                name = "ebf-snapshots-deployment"
                url = uri("https://repository.dev.ebf.de/nexus/content/repositories/snapshots/")
            } else {
                name = "ebf-releases-deployment"
                url = uri("https://repository.dev.ebf.de/nexus/content/repositories/releases/")
            }
            if (project.hasProperty("nexus_user") && project.hasProperty("nexus_pass")) {
              credentials {
                username = project.property("nexus_user") as String?
                password = project.property("nexus_pass") as String?
              }
            }
        }
    }
}
