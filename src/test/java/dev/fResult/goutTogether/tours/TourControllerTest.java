package dev.fResult.goutTogether.tours;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourController.class)
class TourControllerTest {
  private static final String TOUR_API = "/api/v1/tours";

  @Autowired private WebApplicationContext webApplicationContext;

  @MockBean private TourService tourService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetTourByIdThenSuccess() throws Exception {
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
    when(tourService.getTourById(anyInt())).thenReturn(mockTour);

    // Actual
    var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }
}
