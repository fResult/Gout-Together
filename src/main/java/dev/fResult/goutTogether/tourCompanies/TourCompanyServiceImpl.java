package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tourCompanies.models.TourCompany;
import dev.fResult.goutTogether.tourCompanies.models.TourCompanyRequest;
import dev.fResult.goutTogether.tours.TourCompanyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class TourCompanyServiceImpl implements TourCompanyService {
    private final Logger logger = LoggerFactory.getLogger(TourCompanyServiceImpl.class);
    private final TourCompanyRepository tourCompanyRepository;

    public TourCompanyServiceImpl(TourCompanyRepository tourCompanyRepository) {
        this.tourCompanyRepository = tourCompanyRepository;
    }

    @Override
    public TourCompany registerTourCompany(TourCompanyRequest body) {
        logger.debug("[registerTour] newly tour company is registering");
        var companyToRegister = TourCompany.of(null, body.name(), TourCompanyStatus.WAITING.name());
        var registeredCompany = tourCompanyRepository.save(companyToRegister);
        logger.info("[registerTour] new tour company: {}", registeredCompany);

        return registeredCompany;
    }

    @Override
    public TourCompany approveTourCompany(int id) {
        logger.debug("[approveTour] tour company with id: {} is approving", id);
        return tourCompanyRepository.findById(id).map(existingCompany -> {
            var companyToApprove = TourCompany.of(existingCompany.id(), existingCompany.name(), TourCompanyStatus.APPROVED.name());
            var approvedCompany = tourCompanyRepository.save(companyToApprove);
            logger.info("[approveTour] approved tour company: {}", approvedCompany);
            return approvedCompany;
        }).orElseThrow(() -> {
            logger.warn("[approveTour] tour company with id: {} not found", id);
            return new EntityNotFound(String.format("Tour company id: %s not found", id));
        });
    }
}
