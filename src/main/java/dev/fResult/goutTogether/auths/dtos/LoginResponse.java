package dev.fResult.goutTogether.auths.dtos;

public record LoginResponse(int userId, String tokenType, String accessToken, String refreshToken) {

  public static LoginResponse of(
      int userId, String tokenType, String accessToken, String refreshToken) {

    return new LoginResponse(userId, tokenType, accessToken, refreshToken);
  }
}
