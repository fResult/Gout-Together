package dev.fResult.goutTogether.users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.services.UserService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(UserController.class)
public class UserControllerTest {
  private static final String USER_API = "/api/v1/users";
  private static final int USER_ID = 1;
  private static final int NOT_FOUND_USER_ID = 99999;
  private static final String INVALID_EMAIL = "invalid-email";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenRegisterCompanyThenSuccess() throws Exception {
    // Arrange
    var body =
        UserRegistrationRequest.of(
            "John", "Wick", "john.wick@example.com", "password", "0999999999");
    var mockTourCompany =
        UserInfoResponse.of(
            USER_ID, body.firstName(), body.lastName(), body.email(), body.phoneNumber());
    when(userService.register(any(UserRegistrationRequest.class))).thenReturn(mockTourCompany);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(USER_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(USER_ID));
  }

  @Test
  void whenRegisterCompanyButEmailIsInvalidThenReturn400() throws Exception {
    // Arrange
    var body = UserRegistrationRequest.of("John", "Wick", INVALID_EMAIL, "password", "0999999999");
    when(userService.register(any(UserRegistrationRequest.class)))
        .thenThrow(ConstraintViolationException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(USER_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void whenRegisterCompanyButEmailIsAlreadyExistsThenReturn409() throws Exception {
    // Arrange
    var body =
        UserRegistrationRequest.of(
            "John", "Wick", "john.wick@example.com", "password", "0999999999");
    when(userService.register(any(UserRegistrationRequest.class)))
        .thenThrow(CredentialExistsException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(USER_API)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isConflict());
  }
}
