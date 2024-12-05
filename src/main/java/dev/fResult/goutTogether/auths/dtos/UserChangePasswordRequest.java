package dev.fResult.goutTogether.auths.dtos;

public record UserChangePasswordRequest(String oldPassword, String newPassword) {
  public static UserChangePasswordRequest of(String oldPassword, String newPassword) {
    return new UserChangePasswordRequest(oldPassword, newPassword);
  }
}
