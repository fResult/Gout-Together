package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;
import org.springframework.stereotype.Service;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
    private TourCompanyRepository tourCompanyRepository;

    @Override
    public TourCompany registerTour(TourCompanyRequest body) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
