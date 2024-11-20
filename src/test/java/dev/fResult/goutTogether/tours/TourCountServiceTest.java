package dev.fResult.goutTogether.tours;

import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import dev.fResult.goutTogether.tours.services.TourCountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TourCountServiceTest {
  @InjectMocks private TourCountService tourCountService;

  @Mock private TourCountRepository tourCountRepository;

  @Test
  void whenCreateTourCountThenSuccess() {
    // Arrange
    var TOUR_ID = 1;
    var mockCreatedTourCount = TourCount.of(null, AggregateReference.to(TOUR_ID), 0);
    when(tourCountRepository.save(mockCreatedTourCount)).thenReturn(mockCreatedTourCount);

    // Act
    var actualCreatedTourCount = tourCountService.createTourCount(mockCreatedTourCount);

    // Assert
    assertEquals(mockCreatedTourCount, actualCreatedTourCount);
  }
}
