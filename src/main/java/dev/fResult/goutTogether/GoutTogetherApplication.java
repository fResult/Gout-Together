package dev.fResult.goutTogether;

import dev.fResult.goutTogether.common.configs.MyApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ MyApplicationProperties.class })
public class GoutTogetherApplication {
  public static void main(String... args) {
    SpringApplication.run(GoutTogetherApplication.class, args);
  }
}
