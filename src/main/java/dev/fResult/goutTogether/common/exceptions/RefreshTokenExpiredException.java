package dev.fResult.goutTogether.common.exceptions;

public class RefreshTokenExpiredException extends RuntimeException {
  public RefreshTokenExpiredException() {
    super("Refresh token is expired");
  }

  public RefreshTokenExpiredException(String message) {
    super(message);
  }
}
