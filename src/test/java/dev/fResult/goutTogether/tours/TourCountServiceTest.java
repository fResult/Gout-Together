package dev.fResult.goutTogether.tours;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.InsufficientTourCountException;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.tours.services.TourService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class TourCountServiceTest {
  private final int TOUR_ID = 1;
  private final int NOT_FOUND_TOUR_ID = 99999;

  @InjectMocks private TourCountService tourCountService;

  @Mock private TourCountRepository tourCountRepository;
  @Mock private TourService tourService;

  @Test
  void whenCreateTourCount_ThenSuccess() {
    // Arrange
    final var mockCreatedTourCount = TourCount.of(null, AggregateReference.to(TOUR_ID), 0);
    when(tourCountRepository.save(mockCreatedTourCount)).thenReturn(mockCreatedTourCount);

    // Actual
    final var actualCreatedTourCount = tourCountService.createTourCount(mockCreatedTourCount);

    // Assert
    assertEquals(mockCreatedTourCount, actualCreatedTourCount);
  }

  @Test
  void whenIncrementTourCount_ThenSuccess() {
    // Arrange
    final var TOUR_COUNT_ID = 3;
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var mockTour =
        Tour.of(
            TOUR_ID,
            AggregateReference.to(1),
            "Hanoi City 3 days",
            "Camping in Hanoi",
            "Hanoi, Vietnam",
            35,
            Instant.now().plus(45, ChronoUnit.DAYS),
            TourStatus.APPROVED.name());
    final var mockTourCount = TourCount.of(TOUR_COUNT_ID, tourRef, 5);
    final var actualIncrementedTourCount = mockTourCount.increaseAmount(1);

    when(tourService.getTourById(anyInt())).thenReturn(mockTour);
    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.of(mockTourCount));
    when(tourCountRepository.save(any(TourCount.class))).thenReturn(actualIncrementedTourCount);
    // Actual
    tourCountService.incrementTourCount(TOUR_ID);

    // Assert
    assertEquals(6, actualIncrementedTourCount.amount());
  }

  @Test
  void whenIncrementTourCount_ButTourNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format("%s with id [%d] not found", Tour.class.getSimpleName(), NOT_FOUND_TOUR_ID);
    when(tourService.getTourById(NOT_FOUND_TOUR_ID))
        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

    // Actual
    final Executable actualExecutable =
        () -> tourCountService.incrementTourCount(NOT_FOUND_TOUR_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenIncrementTourCount_ButTourCountNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format(
            "%s with tourId [%d] not found", TourCount.class.getSimpleName(), NOT_FOUND_TOUR_ID);

    when(tourCountRepository.findOneByTourId(AggregateReference.to(NOT_FOUND_TOUR_ID)))
        .thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> tourCountService.incrementTourCount(NOT_FOUND_TOUR_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenIncrementTourCount_ButTourAmountExceededLimit_ThenThrowException() {
    // Arrange
    final var TOUR_AMOUNT_LIMIT = 5;
    final var expectedErrorMessage =
        String.format(
            "%s amount is insufficient for this operation", TourCount.class.getSimpleName());
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var mockTour =
        Tour.of(
            TOUR_ID,
            AggregateReference.to(1),
            "Hanoi City 3 days",
            "Camping in Hanoi",
            "Hanoi, Vietnam",
            TOUR_AMOUNT_LIMIT,
            Instant.now().plus(45, ChronoUnit.DAYS),
            TourStatus.APPROVED.name());
    final var mockTourCount = TourCount.of(1, tourRef, TOUR_AMOUNT_LIMIT);

    when(tourService.getTourById(TOUR_ID)).thenReturn(mockTour);
    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.of(mockTourCount));

    // Actual
    final Executable actualExecutable = () -> tourCountService.incrementTourCount(TOUR_ID);

    // Assert
    final var exception = assertThrows(InsufficientTourCountException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenDecrementTourCount_ThenSuccess() {
    // Arrange
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var tourCount = TourCount.of(1, tourRef, 5);
    final var actualDecrementTourCount = tourCount.decreaseAmount(1);

    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.of(tourCount));
    when(tourCountRepository.save(any(TourCount.class))).thenReturn(actualDecrementTourCount);

    // Actual
    tourCountService.decrementTourCount(TOUR_ID);

    // Assert
    assertEquals(4, actualDecrementTourCount.amount());
  }

  @Test
  void whenDecrementTourCount_ButTourCountNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format(
            "%s with tourId [%d] not found", TourCount.class.getSimpleName(), NOT_FOUND_TOUR_ID);
    when(tourCountRepository.findOneByTourId(AggregateReference.to(NOT_FOUND_TOUR_ID)))
        .thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable = () -> tourCountService.decrementTourCount(NOT_FOUND_TOUR_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
