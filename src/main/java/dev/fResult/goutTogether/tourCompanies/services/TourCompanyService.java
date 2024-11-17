package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;

public interface TourCompanyService {
  TourCompany registerTourCompany(RegisterTourCompanyRequest body);

  TourCompany approveTourCompany(int id);

  TourCompany getTourCompanyById(int id);
}
