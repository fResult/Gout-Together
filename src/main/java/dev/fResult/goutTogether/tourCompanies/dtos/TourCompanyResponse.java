package dev.fResult.goutTogether.tourCompanies.dtos;

import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;

public record TourCompanyResponse(int id, String name, TourCompanyStatus status) {
  public static TourCompanyResponse of(int id, String name, TourCompanyStatus status) {
    return new TourCompanyResponse(id, name, status);
  }

  public static TourCompanyResponse fromDao(TourCompany tourCompany) {
    return new TourCompanyResponse(
        tourCompany.id(), tourCompany.name(), TourCompanyStatus.valueOf(tourCompany.status()));
  }
}
