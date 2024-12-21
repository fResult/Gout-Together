package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyUpdateRequest;
import java.util.List;

public interface TourCompanyService {
  List<TourCompanyResponse> getTourCompanies();

  TourCompanyResponse getTourCompanyById(int id);

  TourCompanyResponse registerTourCompany(TourCompanyRegistrationRequest body);

  TourCompanyResponse approveTourCompany(int id);

  TourCompanyResponse updateTourCompanyById(int id, TourCompanyUpdateRequest body);

  boolean deleteTourCompanyById(int id);
}
