package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.enumurations.TourStatus;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourServiceImpl implements TourService {
  private final Logger logger = LoggerFactory.getLogger(TourServiceImpl.class);

  private final TourRepository tourRepository;
  private final TourCompanyService tourCompanyService;
  private final TourCountService tourCountService;

  public TourServiceImpl(
      TourRepository tourRepository,
      TourCompanyService tourCompanyService,
      TourCountService tourCountService) {
    this.tourRepository = tourRepository;
    this.tourCompanyService = tourCompanyService;
    this.tourCountService = tourCountService;
  }

  @Override
  @Transactional
  public Tour createTour(TourRequest body) {
    var tourCompany = tourCompanyService.getTourCompanyById(body.tourCompanyId());
    var tourToCreate =
        Tour.of(
            null,
            AggregateReference.to(tourCompany.id()),
            body.title(),
            body.description(),
            body.location(),
            body.numberOfPeople(),
            body.activityDate(),
            TourStatus.PENDING.name());

    var createdTour = tourRepository.save(tourToCreate);
    logger.debug("[createTour] new tour: {} is created", createdTour);
    tourCountService.createTourCount(
        TourCount.of(null, AggregateReference.to(createdTour.id()), 0));
    return createdTour;
  }

  @Override
  public Tour getTourById(Integer id) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Page<Tour> getTours(Integer page, Integer size) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
