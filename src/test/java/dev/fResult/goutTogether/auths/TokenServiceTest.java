package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
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
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
  private final long accessTokenExpiredInSeconds = 120;
  private final long refreshTokenExpiredInSeconds = 180;

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
                accessTokenExpiredInSeconds,
                refreshTokenExpiredInSeconds,
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
}
