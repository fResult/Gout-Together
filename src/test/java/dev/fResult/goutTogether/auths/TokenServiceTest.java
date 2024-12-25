package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.services.CustomUserDetailsService;
import dev.fResult.goutTogether.auths.services.TokenService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
  private final long accessTokenExpiredInSeconds = 120;
  private final long refreshTokenExpiredInSeconds = 180;

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private JwtEncoder jwtEncoder;

  @Spy @InjectMocks
  private TokenService tokenService =
      new TokenService(
          refreshTokenRepository,
          userDetailsService,
          accessTokenExpiredInSeconds,
          refreshTokenExpiredInSeconds,
          jwtEncoder);

  @Test
  void whenIssueAccessTokenWithAuthenticatedUserThenSuccess() {
    // Arrange
    var authenticatedUser =
        AuthenticatedUser.of(1, "email@example.com", "P@$$w0rd", UserRoleName.ADMIN);
    var mockIssuedToken = "token";
    doReturn(mockIssuedToken).when(tokenService).encodeClaimToJwt(any(JwtClaimsSet.class));

    // Actual
    var actualIssuedAccessToken = tokenService.issueAccessToken(authenticatedUser, Instant.now());

    // Assert
    assertEquals(mockIssuedToken, actualIssuedAccessToken);
  }
}
