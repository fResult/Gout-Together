package dev.fResult.goutTogether.tourCompanies;

import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.dtos.RegisterTourCompanyRequest;
import dev.fResult.goutTogether.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyRepository;
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
    public TourCompany registerTourCompany(RegisterTourCompanyRequest body) {
        logger.debug("[registerTour] newly tour company is registering");
        var companyToRegister = TourCompany.of(
                null,
                body.name(),
                body.username(),
                body.password(),
                TourCompanyStatus.WAITING.name());
        var registeredCompany = tourCompanyRepository.save(companyToRegister);
        logger.info("[registerTour] new tour company: {} is registered", registeredCompany);

        // TODO: Create Login for registered company
        return registeredCompany;
    }

    @Override
    public TourCompany approveTourCompany(int id) {
        logger.debug("[approveTour] tour company id [{}] is approving", id);
        return tourCompanyRepository.findById(id).map(existingCompany -> {
            if (existingCompany.status().equals(TourCompanyStatus.APPROVED.name())) {
                logger.warn("[approveTour] tour company with id [{}] is already approved", id);
                try {
                    throw new Exception(String.format("[approveTour] Tour company id [%s] is already approved", id));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            var companyToApprove = TourCompany.of(
                    existingCompany.id(),
                    existingCompany.name(),
                    existingCompany.username(),
                    existingCompany.password(),
                    TourCompanyStatus.APPROVED.name());
            var approvedCompany = tourCompanyRepository.save(companyToApprove);
            logger.info("[approveTour] approved tour company: {}", approvedCompany);

            // TODO: Create wallet for approved company
            return approvedCompany;
        }).orElseThrow(() -> {
            logger.warn("[approveTour] tour company id [{}] not found", id);
            return new EntityNotFound(String.format("Tour company id [%s] not found", id));
        });
    }
}
