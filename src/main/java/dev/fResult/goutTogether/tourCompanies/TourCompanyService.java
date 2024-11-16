package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;

public interface TourCompanyService {
  TourCompany registerTourCompany(RegisterTourCompanyRequest body);

  TourCompany approveTourCompany(int id) throws Exception;
}
