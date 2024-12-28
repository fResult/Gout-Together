package dev.fResult.goutTogether.auths.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserForgotPasswordRequest(
    @NotBlank String email, String oldPassword, String newPassword) {
  public static UserForgotPasswordRequest of(String email, String oldPassword, String newPassword) {
    return new UserForgotPasswordRequest(email, oldPassword, newPassword);
  }
}
