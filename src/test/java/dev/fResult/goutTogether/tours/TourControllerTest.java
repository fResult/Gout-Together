package dev.fResult.goutTogether.tours;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourController.class)
class TourControllerTest {
  private static final String TOUR_API = "/api/v1/tours";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private TourService tourService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetToursThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
    var params = new LinkedMultiValueMap<>(Map.of("page", List.of("0"), "size", List.of("10")));
    var tours =
        List.of(
            Tour.of(
                TOUR_ID,
                AggregateReference.to(1),
                "Kunlun 7 days",
                "Go 12 places around Kunlun",
                "Kunlun, China",
                20,
                Instant.now().plus(Duration.ofDays(45)),
                TourStatus.APPROVED.name()));
    var pageTours = new PageImpl<>(tours);
    when(tourService.getTours(any(Pageable.class))).thenReturn(pageTours);

    // Actual
    var actualResults = mockMvc.perform(get(TOUR_API).params(params));

    // Assert
    actualResults
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(TOUR_ID));
  }

  @Test
  void whenGetToursButForgotRequiredQueryParamsThenError() throws Exception {
    // Arrange
    var params = new LinkedMultiValueMap<String, String>();

    // Actual
    var actualResults = mockMvc.perform(get(TOUR_API).params(params));

    // Assert
    actualResults.andExpect(status().isBadRequest());
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

  @Test
  void whenGetTourByIdButTourNotFoundThenError() throws Exception {
    // Arrange
    var TOUR_ID = 99999;
    when(tourService.getTourById(anyInt())).thenThrow(new EntityNotFound());

    // Actual
    var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenGetTourByIdButServerErrorThenError() throws Exception {
    // Arrange
    var TOUR_ID = 99999;
    when(tourService.getTourById(anyInt()))
        .thenThrow(new InternalServerErrorException("Mock Error"));

    // Actual
    var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isInternalServerError());
  }

  @Test
  void whenCreateTourThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
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
    var mockTour =
        Tour.of(
            TOUR_ID,
            AggregateReference.to(body.tourCompanyId()),
            body.title(),
            body.description(),
            body.location(),
            body.numberOfPeople(),
            body.activityDate(),
            TourStatus.PENDING.name());
    when(tourService.createTour(any(TourRequest.class))).thenReturn(mockTour);

    // Actual
    var actualResults =
        mockMvc.perform(
            post(TOUR_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    actualResults.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }

  @Test
  void whenCreateTourButMissingSomeFieldsThenError() throws Exception {
    // Arrange
    var TOUR_COMPANY_ID = 1;
    var body =
        TourRequest.of(
            TOUR_COMPANY_ID,
            null,
            "Go 12 places around Kunlun",
            "Kunlun, China",
            20,
            null,
            null);

    // Actual
    var actualResults =
        mockMvc.perform(
            post(TOUR_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    actualResults.andExpect(status().isBadRequest());
  }
}
