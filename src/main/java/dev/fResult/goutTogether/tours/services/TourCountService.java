package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class TourCountService {
  private final Logger logger = LoggerFactory.getLogger(TourCountService.class);
  private final ErrorHelper errorHelper = new ErrorHelper(TourCountService.class);

  private final TourCountRepository tourCountRepository;

  public TourCountService(TourCountRepository tourCountRepository) {
    this.tourCountRepository = tourCountRepository;
  }

  public TourCount createTourCount(TourCount tourCount) {
    logger.debug("[createTourCount] Creating new {}", TourCount.class.getSimpleName());

    var createdTourCount = tourCountRepository.save(tourCount);
    logger.info("[createTourCount] Created {}", createdTourCount);

    return createdTourCount;
  }

  public void incrementTourCount(int tourId) {
    logger.debug(
        "[incrementTourCount] Incrementing {} with tourId [{}]",
        TourCount.class.getSimpleName(),
        tourId);

    var tourCount =
        tourCountRepository
            .findOneByTourId(AggregateReference.to(tourId))
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "incrementTourCount", TourCount.class, "tourId", String.valueOf(tourId)));

    var tourCountToIncrement = tourCount.increaseAmount(1);
    tourCountRepository.save(tourCountToIncrement);

    logger.info(
        "[incrementTourCount] Incremented {} to {}",
        TourCount.class.getSimpleName(),
        tourCount.amount());
  }
}
