package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.tourCompanies.models.TourCompany;
import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;
import dev.fResult.goutTogether.tours.TourCompanyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);

    private final TourCompanyRepository tourCompanyRepository;

    public TourCompanyServiceImpl(TourCompanyRepository tourCompanyRepository) {
        this.tourCompanyRepository = tourCompanyRepository;
    }

    @Override
    public TourCompany registerTour(TourCompanyRequest body) {
        logger.debug("[registerTour] newly tour company is registering");
        var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
        var registeredCompany = tourCompanyRepository.save(companyToRegister);
        logger.info("[registerTour] new tour company: {}", registeredCompany);

        return registeredCompany;
    }
}
