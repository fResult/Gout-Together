package dev.fResult.goutTogether.auths.services;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private static final String ISSUER = "gout-together";
  private static final int TIME_FOR_ROTATE_IN_SECONDS = 120;

  private final RefreshTokenRepository refreshTokenRepository;
  private final CustomUserDetailsService userDetailsService;
  private final long accessTokenExpiredInSeconds;
  private final long refreshTokenExpiredInSeconds;
  private final JwtEncoder jwtEncoder;

  public TokenService(
      RefreshTokenRepository refreshTokenRepository,
      CustomUserDetailsService userDetailsService,
      @Value("${goutapp.token.access-token-expired-in-seconds}") long accessTokenExpiredInSeconds,
      @Value("${goutapp.token.refresh-token-expired-in-seconds}") long refreshTokenExpiredInSeconds,
      JwtEncoder jwtEncoder) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.userDetailsService = userDetailsService;
    this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
    this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
    this.jwtEncoder = jwtEncoder;
  }

  public String issueAccessToken(AuthenticatedUser authenticatedUser, Instant issuedAt) {
    return generateToken(authenticatedUser, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueAccessToken(UserLogin userLogin, Instant issuedAt) {
    final var authenticatedUser =
        (AuthenticatedUser) userDetailsService.loadUserByUsername(userLogin.email());

    return generateToken(authenticatedUser, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueAccessToken(TourCompanyLogin tourCompanyLogin, Instant issuedAt) {
    final var authenticatedUser =
        (AuthenticatedUser) userDetailsService.loadUserByUsername(tourCompanyLogin.username());

    return generateToken(authenticatedUser, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueRefreshToken() {
    return UUIDV7.randomUUID().toString();
  }

  public String encodeClaimToJwt(JwtClaimsSet claims) {
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public boolean isRefreshTokenExpired(RefreshToken refreshToken) {
    final var expiredAt = refreshToken.issuedDate().plusSeconds(refreshTokenExpiredInSeconds);

    return Instant.now().isAfter(expiredAt);
  }

  public String rotateRefreshTokenIfNeed(RefreshToken refreshToken) {
    final var expiredAt = refreshToken.issuedDate().plusSeconds(refreshTokenExpiredInSeconds);
    final var threadHoldToRotate = expiredAt.minusSeconds(TIME_FOR_ROTATE_IN_SECONDS);

    if (Instant.now().isAfter(threadHoldToRotate)) return issueRefreshToken();
    return refreshToken.token();
  }

  public void cleanupExpiredRefreshToken() {
    /* Assume Life of Refresh Token is 1 day
     * - Token issued at 20241213 - 16:13
     * - Token expires at 20241214 - 16.13
     * - Cron started at 20241213 - 16:13
     * - When we want to check expired token from issued date -> use minusDay(1)
     */
    final var currentDateTime = Instant.now();
    final var thresholdDateTime = currentDateTime.minusSeconds(refreshTokenExpiredInSeconds);

    refreshTokenRepository.updateRefreshTokenThatExpired(true, thresholdDateTime);
  }

  private String generateToken(
      AuthenticatedUser authenticatedUser, Instant issuedAt, long expiredInSeconds) {
    final var scope =
        authenticatedUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    final var expiresAt = issuedAt.plusSeconds(expiredInSeconds);

    final var claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .issuedAt(issuedAt)
            .subject(authenticatedUser.email())
            .claim(ROLES_CLAIM, scope)
            .claim(RESOURCE_ID_CLAIM, authenticatedUser.userId())
            .expiresAt(expiresAt)
            .build();

    return encodeClaimToJwt(claims);
  }
}
