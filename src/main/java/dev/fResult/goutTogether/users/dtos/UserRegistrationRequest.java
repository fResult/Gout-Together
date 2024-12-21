package dev.fResult.goutTogether.users.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
    @NotBlank @Size(min = 1, max = 255) String firstName,
    @NotBlank @Size(min = 1, max = 255) String lastName,
    @NotBlank @Size(max = 255) @Email String email,
    @NotBlank @Size(min = 8, max = 255) String password,
    @Size(min = 9, max = 10) String phoneNumber) {

  public static UserRegistrationRequest of(
      String firstName, String lastName, String email, String password, String phoneNumber) {

    return new UserRegistrationRequest(firstName, lastName, email, password, phoneNumber);
  }
}
