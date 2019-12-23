import org.gradle.jvm.tasks.Jar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springVersion = "2.1.4.RELEASE"

plugins {
    id("idea")
    id("java-library")
    id("maven-publish")
    id("groovy")
    id("jacoco")
    id("org.jetbrains.dokka") version "0.9.17"
    id("org.datlowe.maven-publish-auth") version "2.0.2"

    kotlin("jvm") version "1.3.50"
    kotlin("plugin.jpa") version "1.3.50"
    kotlin("plugin.spring") version "1.3.50"
}

group = "de.ebf"
version = "0.0.1"

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
    compileOnly("org.springframework.boot:spring-boot-starter-web:$springVersion")
    compileOnly("org.springframework.boot:spring-boot-starter-security:$springVersion")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor:$springVersion")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.springframework.boot:spring-boot-starter-test:$springVersion")

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("org.spockframework:spock-spring:1.3-groovy-2.5")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.8")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    testImplementation("org.springframework.boot:spring-boot-starter-web:$springVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-security:$springVersion")
    testImplementation("org.springframework.boot:spring-boot-configuration-processor:$springVersion")
}

val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Documentation for Spring JSON API Errors library"
    classifier = "javadoc"
    from(tasks.getByName("dokka"))
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
                url = uri("http://repository.dev.ebf.de/nexus/content/repositories/snapshots/")
            } else {
                name = "ebf-releases-deployment"
                url = uri("http://repository.dev.ebf.de/nexus/content/repositories/releases/")
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

tasks.withType<DokkaTask> {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/javadoc"
}