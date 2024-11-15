package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;

public interface TourCompanyService {
    TourCompany registerTour(TourCompanyRequest body);
}
