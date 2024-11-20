package dev.fResult.goutTogether.tourCompanies.dtos;

import jakarta.validation.constraints.NotBlank;

public record TourCompanyRegistrationRequest(
    Integer id,
    @NotBlank String name,
    @NotBlank String username,
    @NotBlank String password,
    String status) {
  public static TourCompanyRegistrationRequest of(
      Integer id, String name, String username, String password, String status) {
    return new TourCompanyRegistrationRequest(id, name, username, password, status);
  }
}
