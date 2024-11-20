package dev.fResult.goutTogether.users.dtos;

import jakarta.validation.constraints.NotBlank;

public record UserRegistrationRequest(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String email,
    @NotBlank String password,
    String phoneNumber) {
  public static UserRegistrationRequest of(
      String firstName, String lastName, String email, String password, String phoneNumber) {
    return new UserRegistrationRequest(firstName, lastName, email, password, phoneNumber);
  }
}
