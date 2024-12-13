package dev.fResult.goutTogether.common.configs;

import dev.fResult.goutTogether.auths.services.TokenService;
import java.time.Instant;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecurringJobConfig {
  private static final Logger logger = LoggerFactory.getLogger(RecurringJobConfig.class);
  private static final String CRON_EXPRESSION = "*/2 * * * *";

  private final TokenService tokenService;

  public RecurringJobConfig(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Recurring(id = "refresh-token-cleanup", cron = CRON_EXPRESSION)
  @Job(name = "Recurring job for cleanup expired refresh token")
  public void cleanupExpiredRefreshToken() {
    logger.info("Start cleaning up at {}", Instant.now());

    tokenService.cleanupExpiredRefreshToken();
  }
}
