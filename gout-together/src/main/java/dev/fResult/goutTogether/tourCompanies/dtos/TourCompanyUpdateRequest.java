package dev.fResult.goutTogether.tourCompanies.dtos;

import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import jakarta.validation.constraints.Size;
import java.util.Optional;
import java.util.function.Function;

public record TourCompanyUpdateRequest(
    @Size(min = 1, max = 255) String name, TourCompanyStatus status) {
  public static TourCompanyUpdateRequest of(String name, TourCompanyStatus status) {
    return new TourCompanyUpdateRequest(name, status);
  }

  public static Function<TourCompany, TourCompany> dtoToTourCompanyUpdate(
      TourCompanyUpdateRequest body) {
    return tourCompany ->
        TourCompany.of(
            tourCompany.id(),
            Optional.ofNullable(body.name()).orElse(tourCompany.name()),
            Optional.ofNullable(body.status())
                .map(TourCompanyStatus::name)
                .orElse(tourCompany.status()));
  }
}
