package dev.fResult.goutTogether.auths.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.UUID;
import org.springframework.lang.NonNull;

public record RefreshTokenRequest(
    @NotBlank String usage,
    @NonNull @Min(0) Integer resourceId,
    @NotBlank @UUID(message = "invalid format") String refreshToken) {

  public static RefreshTokenRequest of(String usage, Integer resourceId, String refreshToken) {
    return new RefreshTokenRequest(usage, resourceId, refreshToken);
  }
}
