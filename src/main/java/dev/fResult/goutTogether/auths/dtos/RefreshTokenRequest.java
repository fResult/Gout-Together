package dev.fResult.goutTogether.auths.dtos;

import dev.fResult.goutTogether.common.constraints.UUID;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record RefreshTokenRequest(
    @NotNull UserRoleName usage,
    @NonNull @Min(1) Integer resourceId,
    @NotBlank @UUID(message = "invalid format") String refreshToken) {

  public static RefreshTokenRequest of(
      UserRoleName usage, Integer resourceId, String refreshToken) {

    return new RefreshTokenRequest(usage, resourceId, refreshToken);
  }
}
