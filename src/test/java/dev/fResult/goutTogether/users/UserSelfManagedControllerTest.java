package dev.fResult.goutTogether.users;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.services.UserService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(UserSelfManagedController.class)
public class UserSelfManagedControllerTest {
  private final String MY_USER_API = "/api/v1/me";
  private final int USER_ID = 1;
  private final int NOT_FOUND_USER_ID = 99999;
  private final String EMAIL = "john.w@example.com";
  private final String NOT_FOUND_EMAIL = "in_existing@email.com";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private UserService userService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private Authentication buildAuthentication(int resourceId, UserRoleName roleName, String email) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", email)
            .claim(RESOURCE_ID_CLAIM, String.valueOf(resourceId))
            .claim(ROLES_CLAIM, List.of("ROLE_" + roleName.name()))
            .build();
    return new JwtAuthenticationToken(jwt);
  }

  @Test
  void whenGetMyUser_ThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    when(userService.getUserById(USER_ID))
        .thenReturn(UserInfoResponse.of(USER_ID, "John", "Wick", EMAIL, "0999999999"));

    // Actual
    var resultActions = mockMvc.perform(get(MY_USER_API).principal(authentication));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(USER_ID));
  }

  @Test
  void whenUpdateMyUser_ThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var LAST_NAME_TO_UPDATE = "Utah";
    var body = UserUpdateRequest.of(null, LAST_NAME_TO_UPDATE, null);
    var expectedUpdatedUserInfo =
        UserInfoResponse.of(USER_ID, "John", LAST_NAME_TO_UPDATE, EMAIL, "0999999999");
    when(userService.updateUserById(USER_ID, body)).thenReturn(expectedUpdatedUserInfo);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(MY_USER_API)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID))
        .andExpect(jsonPath("$.lastName").value(LAST_NAME_TO_UPDATE));
  }

  @Test
  void whenUpdateMyUser_ButNotFound_ThenReturn404() throws Exception {
    // Arrange
    var authentication = buildAuthentication(NOT_FOUND_USER_ID, UserRoleName.CONSUMER, EMAIL);
    var body = UserUpdateRequest.of(null, "Utah", null);
    when(userService.updateUserById(NOT_FOUND_USER_ID, body))
        .thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(MY_USER_API)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenChangeMyPassword_ThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var body = UserChangePasswordRequest.of("0ldP@ssw0rd", "NewP@ssw0rd");
    var expectedUpdatePasswordResult = UpdatePasswordResult.SUCCESS;
    when(userService.changePasswordByEmail(EMAIL, body)).thenReturn(expectedUpdatePasswordResult);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(MY_USER_API + "/password")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(expectedUpdatePasswordResult.name()));
  }

  @Test
  void whenChangeMyPassword_ButCredentialNotFound_ThenReturn404() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var body = UserChangePasswordRequest.of("0ldP@ssw0rd", "NewP@ssw0rd");

    when(userService.changePasswordByEmail(EMAIL, body)).thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions =
        mockMvc.perform(
            patch(MY_USER_API + "/password")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }

  @Test
  void whenDeleteMyUser_ThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var expectedDeleteResult =
        String.format("Delete %s by id [%d] successfully", User.class.getSimpleName(), USER_ID);

    when(userService.deleteUserById(USER_ID)).thenReturn(true);

    // Actual
    var resultActions = mockMvc.perform(delete(MY_USER_API).principal(authentication));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$").value(expectedDeleteResult));
  }

  @Test
  void whenDeleteMyUser_ButUserNotFound_ThenReturn404() throws Exception {
    // Arrange
    var authentication = buildAuthentication(NOT_FOUND_USER_ID, UserRoleName.CONSUMER, EMAIL);
    var expectedDeleteResult =
        String.format(
            "Delete %s by id [%d] successfully", User.class.getSimpleName(), NOT_FOUND_USER_ID);

    when(userService.deleteUserById(NOT_FOUND_USER_ID)).thenThrow(EntityNotFoundException.class);

    // Actual
    var resultActions = mockMvc.perform(delete(MY_USER_API).principal(authentication));

    // Assert
    resultActions.andExpect(status().isNotFound());
  }
}
