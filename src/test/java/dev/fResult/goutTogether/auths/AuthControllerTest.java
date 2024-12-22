package dev.fResult.goutTogether.auths;

import static dev.fResult.goutTogether.common.Constants.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.auths.controllers.AuthController;
import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.dtos.LogoutInfo;
import dev.fResult.goutTogether.auths.dtos.RefreshTokenRequest;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

@WebMvcTest(AuthController.class)
class AuthControllerTest {
  private final String AUTH_API = "/api/v1/auths";
  private final int USER_ID = 1;

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthService authService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private Authentication buildAuthentication(int resourceId, UserRoleName roleName) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim(RESOURCE_ID_CLAIM, String.valueOf(resourceId))
            .claim(ROLES_CLAIM, List.of("ROLE_" + roleName.name()))
            .build();
    return new JwtAuthenticationToken(jwt);
  }

  @Test
  void whenLoginThenSuccess() throws Exception {
    // Arrange
    var body = new LoginRequest("username", "password");
    var expectedLoggedInUser =
        new LoginResponse(
            USER_ID, TOKEN_TYPE, "%(!(@)*$&(!&^^%$##!&", UUIDV7.randomUUID().toString());

    when(authService.login(Mockito.any(LoginRequest.class))).thenReturn(expectedLoggedInUser);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(AUTH_API + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(USER_ID))
        .andExpect(jsonPath("$.tokenType").value(TOKEN_TYPE));
  }

  @Test
  void whenRefreshTokenThenSuccess() throws Exception {
    // Arrange
    var refreshToken = UUIDV7.randomUUID().toString();
    var body = new RefreshTokenRequest(UserRoleName.CONSUMER, USER_ID, refreshToken);
    var expectedRotatedLoggedInUser =
        new LoginResponse(USER_ID, TOKEN_TYPE, "%(!(@)*$&(!&^^%$##!&", refreshToken);

    when(authService.refreshToken(Mockito.any())).thenReturn(expectedRotatedLoggedInUser);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(AUTH_API + "/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(USER_ID))
        .andExpect(jsonPath("$.tokenType").value(TOKEN_TYPE))
        .andExpect(jsonPath("$.refreshToken").value(refreshToken));
  }

  @Test
  void whenLogoutThenSuccess() throws Exception {
    // Arrange
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER);
    var mockLogoutInfoInput = LogoutInfo.of(USER_ID, UserRoleName.CONSUMER.name());

    when(authService.logout(mockLogoutInfoInput)).thenReturn(true);

    // Actual
    var resultActions = mockMvc.perform(post(AUTH_API + "/logout").principal(authentication));

    // Assert
    resultActions.andExpect(status().isNoContent());
  }
}
