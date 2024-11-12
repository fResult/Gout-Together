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

val agent by configurations.creating

dependencies {
  implementation("io.opentelemetry:opentelemetry-api")
  agent("io.opentelemetry.javaagent:opentelemetry-javaagent:2.9.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-web")

//  runtimeOnly("io.micrometer:micrometer-registry-prometheus")

  testImplementation("org.springframework.boot:spring-boot-starter-test")

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
  mainClass.set("dev/fResult/goutTogether/GoutTogetherApplication.java")
}

tasks.withType<BootJar> {
  dependsOn("copyAgent")
  archiveFileName.set("app.jar")
}
