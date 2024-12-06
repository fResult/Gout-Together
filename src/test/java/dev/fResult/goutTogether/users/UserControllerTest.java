package dev.fResult.goutTogether.users;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.services.UserService;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(UserController.class)
public class UserControllerTest {
  private static final String USER_API = "/api/v1/users";
  private static final int USER_ID = 1;
  private static final int NOT_FOUND_USER_ID = 99999;
  private static final String INVALID_EMAIL = "invalid-email";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void whenGetUsersThenSuccess() throws Exception {
    // Arrange
    var TARGET_FIRST_NAME = "John";
    var params =
        new LinkedMultiValueMap<>(
            Map.of(
                "page",
                List.of("0"),
                "size",
                List.of("10"),
                "first-name",
                List.of(TARGET_FIRST_NAME)));

    var mockUserResp =
        UserInfoResponse.of(
            USER_ID, TARGET_FIRST_NAME, "Wick", "john.wick@exampl.com", "0999999999");
    var userPage = new PageImpl<UserInfoResponse>(List.of(mockUserResp));

    when(userService.getUsersByFirstName(anyString(), any(Pageable.class))).thenReturn(userPage);

    // Actual
    var resultActions = mockMvc.perform(get(USER_API).params(params));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].id").value(USER_ID));
  }

  @Test
  void whenGetUserByIdThenSuccess() throws Exception {
    // Arrange
    var mockUserResp =
        UserInfoResponse.of(USER_ID, "John", "Wick", "john.wick@example.com", "0999999999");
    when(userService.getUserById(anyInt())).thenReturn(mockUserResp);

    // Actual
    var resultActions = mockMvc.perform(get(USER_API + "/{id}", USER_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(USER_ID));
  }

  @Test
  void whenGetUserByIdButUserNotFoundThenReturn404() throws Exception {
    // Arrange
    when(userService.getUserById(anyInt())).thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions = mockMvc.perform(get(USER_API + "/{id}", NOT_FOUND_USER_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenRegisterUserThenSuccess() throws Exception {
    // Arrange
    var body =
        UserRegistrationRequest.of(
            "John", "Wick", "john.wick@example.com", "password", "0999999999");
    var mockTourCompany =
        UserInfoResponse.of(
            USER_ID, body.firstName(), body.lastName(), body.email(), body.phoneNumber());
    when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(mockTourCompany);

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
  void whenRegisterUserButEmailIsInvalidThenReturn400() throws Exception {
    // Arrange
    var body = UserRegistrationRequest.of("John", "Wick", INVALID_EMAIL, "password", "0999999999");
    when(userService.registerUser(any(UserRegistrationRequest.class)))
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
  void whenUpdateUserThenSuccess() throws Exception {
    // Arrange
    var LAST_NAME_TO_UPDATE = "Constantine";
    var PHONE_NUMBER_TO_UPDATE = "0777777777";
    var body = UserUpdateRequest.of(null, LAST_NAME_TO_UPDATE, PHONE_NUMBER_TO_UPDATE);
    var mockTourCompany =
        UserInfoResponse.of(
            USER_ID, "John", LAST_NAME_TO_UPDATE, "john.wick@example.com", PHONE_NUMBER_TO_UPDATE);
    when(userService.updateUserById(anyInt(), any(UserUpdateRequest.class)))
        .thenReturn(mockTourCompany);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(USER_API + "/{id}", USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.lastName").value(LAST_NAME_TO_UPDATE))
        .andExpect(jsonPath("$.phoneNumber").value(PHONE_NUMBER_TO_UPDATE));
  }

  @Test
  void whenUpdateUserByIdButUserNotFoundThenReturn404() throws Exception {
    // Arrange
    var body = UserUpdateRequest.of(null, "Constantine", "0777777777");
    when(userService.updateUserById(anyInt(), any(UserUpdateRequest.class)))
        .thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(USER_API + "/{id}", NOT_FOUND_USER_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenRegisterUserButEmailIsAlreadyExistsThenReturn409() throws Exception {
    // Arrange
    var body =
        UserRegistrationRequest.of(
            "John", "Wick", "john.wick@example.com", "password", "0999999999");
    when(userService.registerUser(any(UserRegistrationRequest.class)))
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

  @Test
  void whenDeleteUserByIdThenSuccess() throws Exception {
    // Arrange
    var responseMessage =
        String.format("Delete %s by id [%d] successfully", User.class.getSimpleName(), USER_ID);
    when(userService.deleteUserById(anyInt())).thenReturn(true);

    // Actual
    var resultActions = mockMvc.perform(delete(USER_API + "/{id}", USER_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$").value(responseMessage));
  }

  @Test
  void whenDeleteUserByIdButUserNotFoundThenReturn404() throws Exception {
    // Arrange
    when(userService.deleteUserById(anyInt())).thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions = mockMvc.perform(delete(USER_API + "/{id}", NOT_FOUND_USER_ID));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }
}
