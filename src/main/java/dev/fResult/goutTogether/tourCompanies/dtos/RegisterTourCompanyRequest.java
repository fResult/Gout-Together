package dev.fResult.goutTogether.tourCompanies.dtos;

import jakarta.validation.constraints.NotBlank;

public record RegisterTourCompanyRequest(
    Integer id,
    @NotBlank String name,
    @NotBlank String username,
    @NotBlank String password,
    String status) {
  public static RegisterTourCompanyRequest of(
      Integer id, String name, String username, String password, String status) {
    return new RegisterTourCompanyRequest(id, name, username, password, status);
  }
}
