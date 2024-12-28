package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class TourCountService {
  private final Logger logger = LoggerFactory.getLogger(TourCountService.class);
  private final ErrorHelper errorHelper = new ErrorHelper(TourCountService.class);

  private final TourCountRepository tourCountRepository;
  private final TourService tourService;

  public TourCountService(TourCountRepository tourCountRepository, @Lazy TourService tourService) {
    this.tourCountRepository = tourCountRepository;
    this.tourService = tourService;
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

    var tour = tourService.getTourById(tourId);
    var tourCount =
        tourCountRepository
            .findOneByTourId(AggregateReference.to(tourId))
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "incrementTourCount", TourCount.class, "tourId", String.valueOf(tourId)));

    var tourCountAmount = 1;
    // TODO: Make pessimistic lock to avoid race condition
    var isInsufficient = tour.numberOfPeople() < tourCount.amount() + tourCountAmount;
    if (isInsufficient) {
      throw errorHelper.insufficientTourCount("incrementTourCount", tour.numberOfPeople()).get();
    }

    var incrementedTourCountToUpdate = tourCount.increaseAmount(tourCountAmount);
    tourCountRepository.save(incrementedTourCountToUpdate);

    logger.info(
        "[incrementTourCount] {} is incremented with tourId [{}] from {} to {}",
        TourCount.class.getSimpleName(),
        tourId,
        tourCount.amount(),
        incrementedTourCountToUpdate.amount());
  }

  public void decrementTourCount(int tourId) {
    logger.debug(
        "[decrementTourCount] Decrementing {} with tourId [{}]",
        TourCount.class.getSimpleName(),
        tourId);

    var tourCount =
        tourCountRepository
            .findOneByTourId(AggregateReference.to(tourId))
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "decrementTourCount", TourCount.class, "tourId", String.valueOf(tourId)));

    var tourCountAmount = 1;
    var decrementTourCountToUpdate = tourCount.decreaseAmount(tourCountAmount);
    tourCountRepository.save(decrementTourCountToUpdate);

    logger.info(
        "[decrementTourCount] {} is decremented tourId [{}] from {} to {}",
        TourCount.class.getSimpleName(),
        tourId,
        tourCount.amount(),
        decrementTourCountToUpdate.amount());
  }
}
