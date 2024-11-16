import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  application
  java
  id("org.springframework.boot") version "3.3.5"
  id("io.spring.dependency-management") version "1.1.6"
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

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
  implementation("org.bouncycastle:bcprov-jdk18on:1.79") // As an Argon2PasswordEncoder's dependency in Spring Security
  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-postgresql")

  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.security:spring-security-test")

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
