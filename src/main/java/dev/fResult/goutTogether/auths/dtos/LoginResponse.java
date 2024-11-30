package dev.fResult.goutTogether.auths.dtos;

public record LoginResponse(int userId, String accessToken, String refreshToken) {
  public static LoginResponse of(int userId, String accessToken, String refreshToken) {
    return new LoginResponse(userId, accessToken, refreshToken);
  }
}
