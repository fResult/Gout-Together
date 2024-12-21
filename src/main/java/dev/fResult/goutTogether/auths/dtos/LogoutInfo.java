package dev.fResult.goutTogether.auths.dtos;

public record LogoutInfo(Integer resourceId, String roles) {
  public static LogoutInfo of(Integer resourceId, String roles) {
    return new LogoutInfo(resourceId, roles);
  }
}
