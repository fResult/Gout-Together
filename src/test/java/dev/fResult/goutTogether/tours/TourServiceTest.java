package dev.fResult.goutTogether.tours;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.repositories.TourRepository;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.tours.services.TourServiceImpl;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {
  @InjectMocks private TourServiceImpl tourService;

  @Mock private TourRepository tourRepository;
  @Mock private TourCompanyService tourCompanyService;
  @Mock private TourCountService tourCountService;

  @Test
  void whenCreateTourThenSuccess() {
    // Arrange
    var TOUR_COMPANY_ID = 1;
    var body =
        TourRequest.of(
            TOUR_COMPANY_ID,
            "Kunlun 7 days",
            "Go 12 places around Kunlun",
            "Kunlun, China",
            20,
            Instant.now(),
            null);
    var mockCompany = TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourStatus.APPROVED.name());
    var mockCreatedTour =
        Tour.of(
            1,
            AggregateReference.to(body.tourCompanyId()),
            body.title(),
            body.description(),
            body.location(),
            body.numberOfPeople(),
            body.activityDate(),
            TourStatus.PENDING.name());

    when(tourCompanyService.getTourCompanyById(TOUR_COMPANY_ID)).thenReturn(mockCompany);
    when(tourRepository.save(any(Tour.class))).thenReturn(mockCreatedTour);

    // Actual
    var actualCreatedTour = tourService.createTour(body);
    // Assert
    assertEquals(mockCreatedTour, actualCreatedTour);
  }

  @Test
  void whenGetTourByIdThenSuccess() {
    // Arrange
    var TOUR_ID = 1;
    var mockTour =
        Tour.of(
            TOUR_ID,
            AggregateReference.to(1),
            "Kunlun 7 days",
            "Go 12 places around Kunlun",
            "Kunlun, China",
            20,
            Instant.now(),
            TourStatus.APPROVED.name());
    when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.of(mockTour));

    // Actual
    var actualTour = tourService.getTourById(TOUR_ID);

    // Assert
    assertEquals(mockTour, actualTour);
  }

  @Test
  void whenGetTourByIdButTourNotFoundThenError() {
    // Arrange
    var TOUR_ID = 999;
    var expectedErrorMessage = String.format("Tour id [%d] not found", TOUR_ID);
    when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> tourService.getTourById(TOUR_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFound.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
