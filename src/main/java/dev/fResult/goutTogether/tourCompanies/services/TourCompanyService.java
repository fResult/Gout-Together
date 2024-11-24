package dev.fResult.goutTogether.tourCompanies.services;

import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;

import java.util.List;
import java.util.stream.Stream;

public interface TourCompanyService {
  List<TourCompanyResponse> getTourCompanies();

  TourCompanyResponse getTourCompanyById(int id);

  TourCompanyResponse registerTourCompany(TourCompanyRegistrationRequest body);

  TourCompanyResponse approveTourCompany(int id);

  TourCompanyResponse updateTourCompanyById(int id, TourCompanyRegistrationRequest body);

  boolean deleteTourCompanyById(int id);
}
