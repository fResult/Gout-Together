package dev.fResult.goutTogether.configs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {
  // FIXME: Uncomment this code to enable PostgreSQLContainer, and find the way to start Docker using Colima
  // @Bean
  // public PostgreSQLContainer<?> postgreSQLContainer() {
  //   var container = new PostgreSQLContainer<>("postgres:13.3");
  //   container.start();
  //   return container;
  // }
}
