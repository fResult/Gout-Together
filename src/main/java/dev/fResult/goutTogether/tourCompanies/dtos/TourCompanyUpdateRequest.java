package dev.fResult.goutTogether.tourCompanies.dtos;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;
import java.util.function.Function;

public record TourCompanyUpdateRequest(
    Integer id, @Size(min = 1, max = 255) String name, @Size(min = 1, max = 20) String status) {
  public static TourCompanyUpdateRequest of(Integer id, String name, String status) {
    return new TourCompanyUpdateRequest(id, name, status);
  }

  public static Function<TourCompany, TourCompany> dtoToTourCompanyUpdate(
      TourCompanyUpdateRequest body) {
    return tourCompany ->
        TourCompany.of(
            tourCompany.id(),
            Optional.ofNullable(body.name()).orElse(tourCompany.name()),
            Optional.ofNullable(body.status()).orElse(tourCompany.status()));
  }
}
