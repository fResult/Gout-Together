package dev.fResult.goutTogether.tourCompanies.dtos;

import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TourCompanyRegistrationRequest(
    @NotBlank @Size(max = 255) String name,
    @NotBlank @Size(min = 8, max = 255) String username,
    @NotBlank @Size(min = 8, max = 255) String password,
    TourCompanyStatus status) {

  public static TourCompanyRegistrationRequest of(
      String name, String username, String password, TourCompanyStatus status) {
    return new TourCompanyRegistrationRequest(name, username, password, status);
  }
}
