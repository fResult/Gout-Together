package dev.fResult.goutTogether.tourCompanies;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.enumurations.TourCompanyStatus;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.ValidationException;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyRegistrationRequest;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyResponse;
import dev.fResult.goutTogether.tourCompanies.dtos.TourCompanyUpdateRequest;
import dev.fResult.goutTogether.tourCompanies.services.TourCompanyService;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(TourCompanyController.class)
class TourCompanyControllerTest {
  private final String TOUR_COMPANY_API = "/api/v1/tour-companies";
  private final int TOUR_COMPANY_ID_1 = 1;
  private final int TOUR_COMPANY_ID_2 = 2;
  private final int NOT_FOUND_TOUR_COMPANY_ID = 99999;

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private TourCompanyService tourCompanyService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetCompanies_ThenSuccess() throws Exception {
    // Arrange
    final var mockTourCompany1 =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour 1", TourCompanyStatus.WAITING);
    final var mockTourCompany2 =
        TourCompanyResponse.of(TOUR_COMPANY_ID_2, "My Tour 2", TourCompanyStatus.APPROVED);

    when(tourCompanyService.getTourCompanies())
        .thenReturn(List.of(mockTourCompany1, mockTourCompany2));

    // Actual
    final var resultActions = mockMvc.perform(get(TOUR_COMPANY_API));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(TOUR_COMPANY_ID_1))
        .andExpect(jsonPath("$[1].id").value(TOUR_COMPANY_ID_2));
  }

  @Test
  void whenGetCompanyById_ThenSuccess() throws Exception {
    // Arrange
    final var mockTourCompany =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour", TourCompanyStatus.APPROVED);
    when(tourCompanyService.getTourCompanyById(TOUR_COMPANY_ID_1)).thenReturn(mockTourCompany);

    // Actual
    final var resultActions = mockMvc.perform(get(TOUR_COMPANY_API + "/{id}", TOUR_COMPANY_ID_1));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(TOUR_COMPANY_ID_1));
  }

  @Test
  void whenGetCompanyById_ButCompanyNotFound_ThenReturn404() throws Exception {
    // Arrange
    when(tourCompanyService.getTourCompanyById(anyInt())).thenThrow(EntityNotFoundException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(get(TOUR_COMPANY_API + "/{id}", NOT_FOUND_TOUR_COMPANY_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenGetMyTourCompany_ThenSuccess() throws Exception {
    // Arrange
    final var mockJwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim(RESOURCE_ID_CLAIM, String.valueOf(TOUR_COMPANY_ID_1))
            .build();
    final var mockAuthentication = new JwtAuthenticationToken(mockJwt);
    final var mockTourCompany =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour", TourCompanyStatus.APPROVED);

    when(tourCompanyService.getTourCompanyById(anyInt())).thenReturn(mockTourCompany);

    // Actual
    final var resultActions =
        mockMvc.perform(get(TOUR_COMPANY_API + "/me").principal(mockAuthentication));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(TOUR_COMPANY_ID_1));
  }

  @Test
  void whenRegisterCompany_ThenSuccess() throws Exception {
    // Arrange
    final var body = TourCompanyRegistrationRequest.of("My Tour", "MyTour123", "mypassword", null);
    final var mockTourCompany =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour", TourCompanyStatus.WAITING);
    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenReturn(mockTourCompany);

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(TOUR_COMPANY_ID_1));
  }

  @Test
  void whenRegisterCompany_ButUsernameIsInvalid_ThenReturn400() throws Exception {
    // Arrange
    final var INVALID_USERNAME = "MyTour";
    final var body =
        TourCompanyRegistrationRequest.of("My Tour", INVALID_USERNAME, "mypassword", null);

    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenThrow(ConstraintViolationException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void whenRegisterCompany_ButCompanyUsernameIsAlreadyExists_ThenReturn409() throws Exception {
    // Arrange
    final var body = TourCompanyRegistrationRequest.of("My Tour", "MyTour123", "mypassword", null);
    when(tourCompanyService.registerTourCompany(any(TourCompanyRegistrationRequest.class)))
        .thenThrow(CredentialExistsException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(TOUR_COMPANY_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isConflict());
  }

  @Test
  void whenApproveCompany_ThenSuccess() throws Exception {
    // Arrange
    final var mockTourCompany =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour", TourCompanyStatus.APPROVED);
    when(tourCompanyService.approveTourCompany(anyInt())).thenReturn(mockTourCompany);

    // Actual
    final var resultActions =
        mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", TOUR_COMPANY_ID_1));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TOUR_COMPANY_ID_1))
        .andExpect(jsonPath("$.status").value(TourCompanyStatus.APPROVED.name()));
  }

  @Test
  void whenApproveCompany_ButCompanyIsAlreadyApproved_ThenReturn422() throws Exception {
    // Arrange
    when(tourCompanyService.approveTourCompany(TOUR_COMPANY_ID_1))
        .thenThrow(ValidationException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", TOUR_COMPANY_ID_1));

    // Assert
    resultActions.andExpect(status().isUnprocessableEntity());
  }

  @Test
  void whenApproveCompany_ButCompanyNotFound_ThenReturn404() throws Exception {
    // Arrange
    when(tourCompanyService.approveTourCompany(anyInt())).thenThrow(EntityNotFoundException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(post(TOUR_COMPANY_API + "/{id}/approve", NOT_FOUND_TOUR_COMPANY_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenUpdateCompany_ThenSuccess() throws Exception {
    // Arrange
    final var STATUS_TO_UPDATE = TourCompanyStatus.BANNED;
    final var body = TourCompanyUpdateRequest.of(null, STATUS_TO_UPDATE);
    final var mockTourCompany =
        TourCompanyResponse.of(TOUR_COMPANY_ID_1, "My Tour", STATUS_TO_UPDATE);

    when(tourCompanyService.updateTourCompanyById(anyInt(), any(TourCompanyUpdateRequest.class)))
        .thenReturn(mockTourCompany);

    // Actual
    final var resultActions =
        mockMvc.perform(
            patch(TOUR_COMPANY_API + "/{id}", TOUR_COMPANY_ID_1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(TOUR_COMPANY_ID_1))
        .andExpect(jsonPath("$.status").value(STATUS_TO_UPDATE.name()));
  }

  @Test
  void whenUpdateCompanyById_ButCompanyNotFound_ThenReturn404() throws Exception {
    // Arrange
    final var body = TourCompanyUpdateRequest.of("My Tour", TourCompanyStatus.APPROVED);
    when(tourCompanyService.updateTourCompanyById(anyInt(), any(TourCompanyUpdateRequest.class)))
        .thenThrow(EntityNotFoundException.class);

    // Actual
    final var resultActions =
        mockMvc.perform(
            patch(TOUR_COMPANY_API + "/{id}", NOT_FOUND_TOUR_COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenDeleteCompanyById_ThenSuccess() throws Exception {
    // Arrange
    final var expectedResponseMessage =
        String.format("Delete %s by id [%d] successfully", "TourCompany", TOUR_COMPANY_ID_1);
    when(tourCompanyService.deleteTourCompanyById(TOUR_COMPANY_ID_1)).thenReturn(true);

    // Actual
    final var resultActions =
        mockMvc.perform(delete(TOUR_COMPANY_API + "/{id}", TOUR_COMPANY_ID_1));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(expectedResponseMessage));
  }

  @Test
  void whenDeleteCompanyById_ButCompanyNotFound_ThenReturn404() throws Exception {
    // Arrange
    when(tourCompanyService.deleteTourCompanyById(anyInt()))
        .thenThrow(new EntityNotFoundException());

    // Actual
    final var resultActions =
        mockMvc.perform(delete(TOUR_COMPANY_API + "/{id}", NOT_FOUND_TOUR_COMPANY_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }
}
