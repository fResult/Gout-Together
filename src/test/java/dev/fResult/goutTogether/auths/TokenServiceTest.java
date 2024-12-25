package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.services.CustomUserDetailsService;
import dev.fResult.goutTogether.auths.services.TokenService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import java.time.Instant;
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
    void byAuthenticatedUserThenSuccess() {
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
  }
}
