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
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourController.class)
class TourControllerTest {
  private static final String TOUR_API = "/api/v1/tours";
  private static final int TOUR_ID = 1;
  private static final int NOT_FOUND_TOUR_ID = 99999;

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private TourService tourService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetTours_ThenSuccess() throws Exception {
    // Arrange
    final var params =
        new LinkedMultiValueMap<>(Map.of("page", List.of("0"), "size", List.of("10")));
    final var tours =
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
    final var pageTours = new PageImpl<>(tours);

    when(tourService.getTours(any(Pageable.class))).thenReturn(pageTours);

    // Actual
    final var actualResults = mockMvc.perform(get(TOUR_API).params(params));

    // Assert
    actualResults
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(TOUR_ID));
  }

  @Test
  void whenGetTours_ButForgotRequiredQueryParams_ThenReturn400() throws Exception {
    // Arrange
    final var params = new LinkedMultiValueMap<String, String>();

    // Actual
    final var actualResults = mockMvc.perform(get(TOUR_API).params(params));

    // Assert
    actualResults.andExpect(status().isBadRequest());
  }

  @Test
  void whenGetTourById_ThenSuccess() throws Exception {
    // Arrange
    final var mockTour =
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
    final var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }

  @Test
  void whenGetTourById_ButTourNotFound_Then404() throws Exception {
    // Arrange
    when(tourService.getTourById(anyInt())).thenThrow(new EntityNotFoundException());

    // Actual
    final var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", NOT_FOUND_TOUR_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenGetTourById_ButServerError_ThenReturn500() throws Exception {
    // Arrange
    when(tourService.getTourById(anyInt()))
        .thenThrow(new InternalServerErrorException("Mock Error"));

    // Actual
    final var resultActions = mockMvc.perform(get(TOUR_API + "/{id}", NOT_FOUND_TOUR_ID));

    // Assert
    resultActions.andExpect(status().isInternalServerError());
  }

  @Test
  void whenCreateTour_ThenSuccess() throws Exception {
    // Arrange
    final var TOUR_COMPANY_ID = 1;
    final var body =
        TourRequest.of(
            TOUR_COMPANY_ID,
            "Kunlun 7 days",
            "Go 12 places around Kunlun",
            "Kunlun, China",
            20,
            Instant.now().plus(Duration.ofDays(45)),
            null);
    final var mockTour =
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
    final var actualResults =
        mockMvc.perform(
            post(TOUR_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    actualResults.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }

  @Test
  void whenCreateTour_ButMissingSomeFields_ThenReturn400() throws Exception {
    // Arrange
    final var TOUR_COMPANY_ID = 1;
    final var body =
        TourRequest.of(
            TOUR_COMPANY_ID, null, "Go 12 places around Kunlun", "Kunlun, China", 20, null, null);

    // Actual
    final var actualResults =
        mockMvc.perform(
            post(TOUR_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    actualResults.andExpect(status().isBadRequest());
  }
}
