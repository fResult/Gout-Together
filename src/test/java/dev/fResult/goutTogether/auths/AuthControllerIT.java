package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.*;

import dev.fResult.goutTogether.configs.AbstractIntegrationTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

// NOTE: `IT` suffix stands for Integration Test
public class AuthControllerIT extends AbstractIntegrationTest {
  private final String AUTHS_API = "/api/v1/auths";

  @LocalServerPort private int port;

  private RestClient restClient;

  // @BeforeEach
  // void setupUser() {
  //   restClient = RestClient.builder().baseUrl(String.format("http://localhost:%d", port)).build();
  //
  //   final var body =
  //       UserRegistrationRequest.of("Test", "Test", "email@example.com", "mypassword", "0000000000");

  //   // TODO: Remove `.retrieve()`, then retry again
  //   restClient
  //       .post()
  //       .uri("/api/v1/users")
  //       .body(body)
  //       .accept(MediaType.APPLICATION_JSON)
  //       .retrieve()
  //       .onStatus(new DefaultResponseErrorHandler())
  //       .toEntity(UserInfoResponse.class);
  // }

  // @Test
  // void shouldLoginSuccess() {
  //   final var body = LoginRequest.of("email@example.com", "999999999");
  //   final var actualLoggedIn = login(body);

  //   assertTrue(actualLoggedIn.userId() > 0);
  //   assertFalse(actualLoggedIn.accessToken().isBlank());
  //   assertFalse(actualLoggedIn.refreshToken().isBlank());
  //   assertEquals("bearer", actualLoggedIn.tokenType());
  // }

  //  @Test
  //  void shouldRefreshNewToken() {
  //    final var body = LoginRequest.of("email@example.com", "999999999");
  //    final var actualRefreshedToken = login(body);

  //    assertTrue(actualRefreshedToken.userId() > 0);
  //    assertFalse(actualRefreshedToken.accessToken().isBlank());
  //    assertFalse(actualRefreshedToken.refreshToken().isBlank());
  //    assertEquals("bearer", actualRefreshedToken.tokenType());
  //  }

  //  @Test
  //  void shouldLogoutSuccess() {
  //    final var body = LoginRequest.of("email@example.com", "999999999");
  //    final Executable actualExecutable = () -> login(body);
  //
  //    assertDoesNotThrow(actualExecutable);
  //  }

  // private LoginResponse login(LoginRequest body) {
  //   final var entity =
  //       restClient
  //           .post()
  //           .uri(AUTHS_API + "/login")
  //           .accept(MediaType.APPLICATION_JSON)
  //           .retrieve()
  //           .toEntity(LoginResponse.class);

  //   return entity.getBody();
  // }

  //  private LoginResponse refreshToken(RefreshTokenRequest body) {
  //    final var entity =
  //        restClient
  //            .post()
  //            .uri(AUTHS_API + "/refresh")
  //            .body(body)
  //            .accept(MediaType.APPLICATION_JSON)
  //            .retrieve()
  //            .toEntity(LoginResponse.class);

  //    return entity.getBody();
  //  }

  //  private void logout(String accessToken) {
  //    final var authorizationHeader = String.format("Bearer %s", accessToken);
  //    restClient
  //        .post()
  //        .uri(AUTHS_API + "/logout")
  //        .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
  //        .accept(MediaType.APPLICATION_JSON)
  //        .retrieve()
  //        .toBodilessEntity();
  //  }
}
