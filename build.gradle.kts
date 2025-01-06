import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.DotenvEntry
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
  application
  java
  id("org.springframework.boot") version libs.versions.springboot.get()
  id("io.spring.dependency-management") version "1.1.7"
}

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("io.github.cdimascio:java-dotenv:5.2.2")
  }
}

group = "dev.fResult"
version = "0.0.1"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(23)
  }
}

repositories {
  mavenCentral()
}

val agent: Configuration by configurations.creating

dependencies {
  implementation("io.opentelemetry:opentelemetry-api")
  agent("io.opentelemetry.javaagent:opentelemetry-javaagent:2.9.0")

  developmentOnly(libs.spring.boot.devtools)

  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.data.jdbc)
  implementation(libs.spring.boot.starter.oauth2.resource.server)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.web)
  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  implementation("org.bouncycastle:bcprov-jdk18on:1.79") // As an Argon2PasswordEncoder's dependency in Spring Security
  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("io.github.cdimascio:java-dotenv:5.2.2")
  implementation("commons-validator:commons-validator:1.9.0")
  implementation("org.jobrunr:jobrunr-spring-boot-3-starter:7.3.2")
  implementation("com.google.zxing:core:3.5.3")
  implementation("com.google.zxing:javase:3.5.3")

  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.register<Copy>("copyAgent") {
  from(agent) {
    rename("opentelemetry-javaagent-.*\\.jar", "opentelemetry-javaagent.jar")
  }
  into(layout.buildDirectory.dir("agent"))
}

application {
  mainClass.set("$group.goutTogether.GoutTogetherApplication")
}

tasks.withType<BootJar> {
  dependsOn("copyAgent")
  archiveFileName.set("app.jar")
}

tasks.withType<BootRun> {
  doFirst {
    jvmArgs(listOf("-javaagent:build/agent/opentelemetry-javaagent.jar"))
    val dotenv = Dotenv.configure().load()
    dotenv.entries().forEach { entry -> environment(entry.key, entry.value) }
  }
}
