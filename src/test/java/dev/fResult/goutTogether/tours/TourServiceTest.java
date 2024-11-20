package dev.fResult.goutTogether.tours;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourRepository;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.tours.services.TourServiceImpl;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
            Instant.now().plus(Duration.ofDays(45)),
            null);
    var mockCompany = TourCompany.of(TOUR_COMPANY_ID, "My Tour", TourStatus.APPROVED.name());
    var mockCreatedTour =
        Tour.of(
            body.tourCompanyId(),
            AggregateReference.to(body.tourCompanyId()),
            body.title(),
            body.description(),
            body.location(),
            body.numberOfPeople(),
            body.activityDate(),
            TourStatus.PENDING.name());
    var mockTourCount = TourCount.of(1, AggregateReference.to(mockCreatedTour.id()), 0);

    when(tourCompanyService.getTourCompanyById(TOUR_COMPANY_ID)).thenReturn(mockCompany);
    when(tourRepository.save(any(Tour.class))).thenReturn(mockCreatedTour);
    when(tourCountService.createTourCount(any(TourCount.class))).thenReturn(mockTourCount);

    // Actual
    var actualCreatedTour = tourService.createTour(body);
    var actualCreatedTourCount = tourCountService.createTourCount(mockTourCount);

    // Assert
    assertEquals(mockCreatedTour, actualCreatedTour);
    assertNotNull(actualCreatedTourCount);
  }

  @Test
  void whenCreateTourButTourCompanyNotFoundThenSuccess() {
    // Arrange
    var TOUR_COMPANY_ID = 99999;
    var body =
        TourRequest.of(
            TOUR_COMPANY_ID,
            "Kunlun 7 days",
            "Go 12 places around Kunlun",
            "Kunlun, China",
            20,
            Instant.now().plus(Duration.ofDays(45)),
            null);
    var expectedErrorMessage =
        String.format("%s id [%d] not found", TourCompany.class.getSimpleName(), TOUR_COMPANY_ID);
    when(tourCompanyService.getTourCompanyById(anyInt()))
        .thenThrow(new EntityNotFound(expectedErrorMessage));

    // Actual
    Executable actualExecutable = () -> tourService.createTour(body);

    // Assert
    var actualException = assertThrowsExactly(EntityNotFound.class, actualExecutable);
    assertEquals(expectedErrorMessage, actualException.getMessage());
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
            Instant.now().plus(Duration.ofDays(45)),
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
    var TOUR_ID = 99999;
    var expectedErrorMessage =
        String.format("%s id [%d] not found", Tour.class.getSimpleName(), TOUR_ID);
    when(tourRepository.findById(TOUR_ID)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> tourService.getTourById(TOUR_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFound.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenGetToursThenSuccess() {
    // Arrange
    var tours =
        List.of(
            Tour.of(
                1,
                AggregateReference.to(1),
                "Kunlun 7 days",
                "Go 12 places around Kunlun",
                "Kunlun, China",
                20,
                Instant.now().plus(Duration.ofDays(45)),
                TourStatus.APPROVED.name()));
    var pageTours = new PageImpl<>(tours);
    when(tourRepository.findAll(any(Pageable.class))).thenReturn(pageTours);

    // Actual
    var actualTours = tourService.getTours(PageRequest.of(0, 3));

    // Assert
    assertEquals(pageTours, actualTours);
  }
}
