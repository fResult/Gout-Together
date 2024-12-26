package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.services.CustomUserDetailsService;
import dev.fResult.goutTogether.auths.services.TokenService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
  private final long ACCESS_TOKEN_EXPIRED_IN_120_SECONDS = 120;
  private final long REFRESH_TOKEN_EXPIRED_IN_600_SECONDS = 600;

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private JwtEncoder jwtEncoder;

  private TokenService tokenService;

  @BeforeEach
  void setUp() {
    tokenService =
        spy(
            new TokenService(
                refreshTokenRepository,
                userDetailsService,
                ACCESS_TOKEN_EXPIRED_IN_120_SECONDS,
                REFRESH_TOKEN_EXPIRED_IN_600_SECONDS,
                jwtEncoder));
  }

  @Nested
  class IssueAccessToken {
    @Test
    void byAuthenticatedUser_ThenSuccess() {
      // Arrange
      var authenticatedUser =
          AuthenticatedUser.of(1, "email@example.com", "P@$$w0rd", UserRoleName.ADMIN);
      var mockIssuedAccessToken = "token";
      doReturn(mockIssuedAccessToken).when(tokenService).encodeClaimToJwt(any(JwtClaimsSet.class));

      // Actual
      var actualIssuedAccessToken = tokenService.issueAccessToken(authenticatedUser, Instant.now());

      // Assert
      assertEquals(mockIssuedAccessToken, actualIssuedAccessToken);
    }

    @Test
    void byUserLogin_ThenSuccess() {
      // Arrange
      var USER_ID = 1;
      var userLogin =
          UserLogin.of(1, AggregateReference.to(USER_ID), "email@example.com", "P@$$w0rd");
      var authenticatedUser =
          AuthenticatedUser.of(
              USER_ID, userLogin.email(), userLogin.password(), UserRoleName.ADMIN);
      var mockIssuedAccessToken = "token";

      when(userDetailsService.loadUserByUsername(userLogin.email())).thenReturn(authenticatedUser);
      doReturn(mockIssuedAccessToken).when(tokenService).encodeClaimToJwt(any(JwtClaimsSet.class));

      // Actual
      var actualIssuedAccessToken = tokenService.issueAccessToken(userLogin, Instant.now());

      // Assert
      assertEquals(mockIssuedAccessToken, actualIssuedAccessToken);
    }

    @Test
    void byTourCompanyLogin_ThenSuccess() {
      // Arrange
      var TOUR_COMPANY_ID = 1;
      var tourCompanyLogin =
          TourCompanyLogin.of(1, AggregateReference.to(TOUR_COMPANY_ID), "username", "P@$$w0rd");
      var authenticatedUser =
          AuthenticatedUser.of(
              TOUR_COMPANY_ID,
              tourCompanyLogin.username(),
              tourCompanyLogin.password(),
              UserRoleName.ADMIN);
      var expectedIssuedAccessToken = "token";

      when(userDetailsService.loadUserByUsername(tourCompanyLogin.username()))
          .thenReturn(authenticatedUser);
      doReturn(expectedIssuedAccessToken)
          .when(tokenService)
          .encodeClaimToJwt(any(JwtClaimsSet.class));

      // Actual
      var actualIssuedAccessToken = tokenService.issueAccessToken(tourCompanyLogin, Instant.now());

      // Assert
      assertEquals(expectedIssuedAccessToken, actualIssuedAccessToken);
    }
  }

  @Test
  void issueRefreshToken_ThenSuccess() {
    try (var mockedUUIDV7 = mockStatic(UUIDV7.class)) {
      // Arrange
      var mockRefreshToken = UUID.fromString("cc468bab-17c5-4ded-b94a-7a7d85993bcb");
      var expectedIssuedRefreshToken = mockRefreshToken.toString();

      mockedUUIDV7.when(UUIDV7::randomUUID).thenReturn(mockRefreshToken);

      // Actual
      var actualIssuedRefreshToken = tokenService.issueRefreshToken();

      // Assert
      assertEquals(expectedIssuedRefreshToken, actualIssuedRefreshToken);
    }
  }

  @Test
  void whenEncodeClaimToJwt_ThenSuccess() {
    // Arrange
    var claims = JwtClaimsSet.builder().claim("key", "value").build();
    var expectedEncodedJwt = "encoded_jwt";
    var mockJwt = mock(Jwt.class);

    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);
    when(mockJwt.getTokenValue()).thenReturn(expectedEncodedJwt);

    // Actual
    var actualEncodedJwt = tokenService.encodeClaimToJwt(claims);

    // Assert
    assertEquals(expectedEncodedJwt, actualEncodedJwt);
  }

  @Test
  void whenRefreshTokenExpired_ThenReturnTrue() {
    // Arrange
    var USER_ID = 1;
    var ISSUED_AT_600_SECS_AGO = Instant.now().minusSeconds(REFRESH_TOKEN_EXPIRED_IN_600_SECONDS);
    var refreshTokenInput =
        RefreshToken.of(1, "token", ISSUED_AT_600_SECS_AGO, UserRoleName.CONSUMER, USER_ID, false);

    // Actual
    var actualIsRefreshTokenExpired = tokenService.isRefreshTokenExpired(refreshTokenInput);

    // Assert
    assertTrue(actualIsRefreshTokenExpired);
  }

  @Test
  void whenRefreshTokenNotExpired_ThenReturnFalse() {
    // Arrange
    var USER_ID = 1;
    var ISSUED_AT_599_SECS_AGO =
        Instant.now().minusSeconds(REFRESH_TOKEN_EXPIRED_IN_600_SECONDS - 1);
    var refreshTokenInput =
        RefreshToken.of(1, "token", ISSUED_AT_599_SECS_AGO, UserRoleName.CONSUMER, USER_ID, false);

    // Actual
    var actualIsRefreshTokenExpired = tokenService.isRefreshTokenExpired(refreshTokenInput);

    // Assert
    assertFalse(actualIsRefreshTokenExpired);
  }

  @Nested
  class RotateRefreshTokenTest {
    @Test
    void andReachedTimeToRotate_ThenReturnNewToken() {
      // Arrange
      var USER_ID = 1;
      var ISSUED_AT_480_SECS_AGO = Instant.now().minusSeconds(480);
      var refreshTokenInput =
          RefreshToken.of(
              1, "old_token", ISSUED_AT_480_SECS_AGO, UserRoleName.CONSUMER, USER_ID, false);
      var expectedNewToken = "new_token";

      when(tokenService.issueRefreshToken()).thenReturn(expectedNewToken);

      // Actual
      var actualNewToken = tokenService.rotateRefreshTokenIfNeed(refreshTokenInput);

      // Assert
      assertEquals(expectedNewToken, actualNewToken);
    }

    @Test
    void andNotReachTimeToRotateYet_ThenReturnOldToken() {
      // Arrange
      var USER_ID = 1;
      var ISSUED_AT_479_SECS_AGO = Instant.now().minusSeconds(479);
      var OLD_TOKEN = "old_token";
      var refreshTokenInput =
          RefreshToken.of(
              1, OLD_TOKEN, ISSUED_AT_479_SECS_AGO, UserRoleName.CONSUMER, USER_ID, false);

      // Actual
      var actualNewToken = tokenService.rotateRefreshTokenIfNeed(refreshTokenInput);

      // Assert
      assertEquals(OLD_TOKEN, actualNewToken);
    }
  }

  @Test
  void cleanupExpiredRefreshToken() {
    // Actual
    tokenService.cleanupExpiredRefreshToken();

    // Assert
    verify(refreshTokenRepository, times(1))
        .updateRefreshTokenThatExpired(eq(true), any(Instant.class));
  }
}
