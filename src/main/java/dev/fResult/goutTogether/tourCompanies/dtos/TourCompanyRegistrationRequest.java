package dev.fResult.goutTogether.tourCompanies.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TourCompanyRegistrationRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(min = 8, max = 255) String username,
    @NotBlank @Size(min = 8, max = 255) String password,
    @Size(max = 20) String status) {

  public static TourCompanyRegistrationRequest of(
      String name, String username, String password, String status) {
    return new TourCompanyRegistrationRequest(name, username, password, status);
  }
}
