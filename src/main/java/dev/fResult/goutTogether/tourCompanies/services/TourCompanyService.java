package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;

public interface TourCompanyService {
  TourCompany registerTourCompany(TourCompanyRegistrationRequest body);

  TourCompany approveTourCompany(int id);

  TourCompany getTourCompanyById(int id);
}
