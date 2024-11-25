package dev.fResult.goutTogether.tourCompanies;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourCompanyController.class)
class TourCompanyControllerTest {
  private final String TOUR_COMPANY_API = "/api/v1/tour-companies";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private TourCompanyService tourCompanyService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetToursThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID_1 = 1;
    var TOUR_ID_2 = 2;
    var mockTourCompany1 =
        TourCompanyResponse.of(TOUR_ID_1, "My Tour 1", TourCompanyStatus.WAITING);
    var mockTourCompany2 =
        TourCompanyResponse.of(TOUR_ID_2, "My Tour 2", TourCompanyStatus.APPROVED);
    when(tourCompanyService.getTourCompanies())
        .thenReturn(List.of(mockTourCompany1, mockTourCompany2));

    // Actual
    var resultActions = mockMvc.perform(get(TOUR_COMPANY_API));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(TOUR_ID_1))
        .andExpect(jsonPath("$[1].id").value(TOUR_ID_2));
  }

  @Test
  void whenGetTourByIdThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
    var mockTourCompany = TourCompanyResponse.of(TOUR_ID, "My Tour", TourCompanyStatus.APPROVED);
    when(tourCompanyService.getTourCompanyById(TOUR_ID)).thenReturn(mockTourCompany);

    // Actual
    var resultActions = mockMvc.perform(get(TOUR_COMPANY_API + "/{id}", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }

  @Test
  void whenRegisterCompanyThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
    var body = TourCompanyRegistrationRequest.of("My Tour", "MyTour123", "mypassword", null);
    var mockTourCompany = TourCompanyResponse.of(TOUR_ID, "My Tour", TourCompanyStatus.WAITING);
    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenReturn(mockTourCompany);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(TOUR_ID));
  }

  @Test
  void whenRegisterCompanyButUsernameIsInvalidThenReturn400() throws Exception {
    // Arrange
    var INVALID_USERNAME = "MyTour";
    var body = TourCompanyRegistrationRequest.of("My Tour", INVALID_USERNAME, "mypassword", null);
    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenThrow(ConstraintViolationException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void whenRegisterCompanyButCompanyUsernameIsAlreadyExistsThenReturn409() throws Exception {
    // Arrange
    var body = TourCompanyRegistrationRequest.of("My Tour", "MyTour123", "mypassword", null);
    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenThrow(CredentialExistsException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isConflict());
  }

  @Test
  void whenApproveCompanyThenSuccess() throws Exception {
    // Arrange
    var TOUR_ID = 1;
    var mockTourCompany = TourCompanyResponse.of(TOUR_ID, "My Tour", TourCompanyStatus.APPROVED);
    when(tourCompanyService.approveTourCompany(anyInt())).thenReturn(mockTourCompany);

    // Actual
    var resultActions = mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", TOUR_ID));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TOUR_ID))
        .andExpect(jsonPath("$.status").value(TourCompanyStatus.APPROVED.name()));
  }

  @Test
  void whenApproveCompanyButCompanyIsAlreadyApprovedThenReturn422() throws Exception {
    // Arrange
    var TOUR_COMPANY_ID = 1;
    when(tourCompanyService.approveTourCompany(TOUR_COMPANY_ID))
        .thenThrow(ValidationException.class);

    // Actual
    var resultActions = mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", TOUR_COMPANY_ID));

    // Assert
    resultActions.andExpect(status().isUnprocessableEntity());
  }

  @Test
  void whenApproveCompanyButCompanyNotFoundThenReturn404() throws Exception {
    // Arrange
    var TOUR_ID = 99999;
    when(tourCompanyService.approveTourCompany(anyInt())).thenThrow(new EntityNotFoundException());

    // Actual
    var resultActions = mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", TOUR_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }
}
