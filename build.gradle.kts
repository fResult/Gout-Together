//import io.github.cdimascio.dotenv.Dotenv
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  application
  java
  id("org.springframework.boot") version libs.versions.springboot.get()
  id("io.spring.dependency-management") version "1.1.7"
}

//buildscript {
//  dependencies {
//    implementation("io.github.cdimascio:dotenv-java:3.2.0")
//  }
//}

group = "dev.fResult"
version = "0.0.1"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}

repositories {
  mavenCentral()
}

val agent: Configuration by configurations.creating

dependencies {
  compileOnly("org.projectlombok:lombok")
  annotationProcessor("org.projectlombok:lombok")

  implementation("io.opentelemetry:opentelemetry-api")
  agent("io.opentelemetry.javaagent:opentelemetry-javaagent:2.23.0")

  developmentOnly(libs.spring.boot.devtools)

  annotationProcessor(libs.spring.boot.configuration.processor)
  implementation(libs.spring.boot.starter.actuator)
  implementation(libs.spring.boot.starter.data.jdbc)
  implementation(libs.spring.boot.starter.flyway)
  implementation(libs.spring.boot.starter.oauth2.resource.server)
  implementation(libs.spring.boot.starter.security)
  implementation(libs.spring.boot.starter.validation)
  implementation(libs.spring.boot.starter.webmvc)
  implementation("org.bouncycastle:bcprov-jdk18on:1.83") // As an Argon2PasswordEncoder's dependency in Spring Security
  implementation("org.flywaydb:flyway-core")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("io.github.cdimascio:dotenv-java:3.2.0")
  implementation("commons-validator:commons-validator:1.10.1")
  implementation("org.jobrunr:jobrunr-spring-boot-4-starter:8.3.0")
  implementation("com.google.zxing:core:3.5.4")
  implementation("com.google.zxing:javase:3.5.4")

  runtimeOnly("io.micrometer:micrometer-registry-prometheus")
  runtimeOnly("org.postgresql:postgresql")

  testImplementation(libs.spring.boot.starter.webmvc.test)
  testImplementation(libs.spring.boot.starter.flyway.test)
  implementation(libs.spring.boot.starter.validation.test)
  testImplementation(libs.spring.boot.starter.data.jdbc.test)
  testImplementation(libs.spring.boot.testcontainers)
  testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter")
  testImplementation("org.testcontainers:testcontainers-postgresql")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations {
  all {
    exclude(group = "org.springframework.boot", module = "spring-boot-thymeleaf")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-thymeleaf")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  environment("TESTCONTAINERS_RYUK_DISABLED", "true") // Disable Ryuk container for tests
}

tasks.register<Copy>("copyAgent") {
  description = "Copies the OpenTelemetry Java agent to the build directory for runtime instrumentation."
  group = BasePlugin.BUILD_GROUP

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

//tasks.withType<BootRun> {
//  doFirst {
//    jvmArgs(listOf("-javaagent:build/agent/opentelemetry-javaagent.jar"))
//    val dotenv = Dotenv.configure().load()
//    dotenv.entries().forEach { entry -> environment(entry.key, entry.value) }
//  }
//}

tasks.withType<BootBuildImage> {
  environment.put("JAVA_TOOL_OPTIONS", "-javaagent:/layers/agent/opentelemetry-javaagent.jar")
  environment.put("DOCKER_HOST", "unix:///Users/fResult/.colima/default/docker.sock")
  environment.put("TESTCONTAINERS_RYUK_DISABLED", "false") // Disable Ryuk container for tests
}
