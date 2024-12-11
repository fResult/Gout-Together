package dev.fResult.goutTogether.configs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
  // FIXME: Find the way to start Docker using Colima
  @Bean
  @ServiceConnection
  public PostgreSQLContainer<?> postgreSQLContainer() {
    System.setProperty(
        "TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/Users/fresult/.colima/default/docker.sock");
    System.setProperty("DOCKER_HOST", "unix:///Users/korn/.colima/default/docker.sock");

    return new PostgreSQLContainer<>("postgres:13.3");
  }
}
