package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompany;
import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;

public interface TourCompanyService {
    TourCompany registerTourCompany(TourCompanyRequest body);

    TourCompany approveTourCompany(int id) throws Exception;
}
