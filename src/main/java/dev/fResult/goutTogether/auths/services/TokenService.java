package dev.fResult.goutTogether.auths.services;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.UserLogin;
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

  private final CustomUserDetailsService userDetailsService;
  private final long accessTokenExpiredInSeconds;
  private final long refreshTokenExpiredInSeconds;
  private final JwtEncoder jwtEncoder;

  public TokenService(
      CustomUserDetailsService userDetailsService,
      @Value("${token.access-token-expired-in-seconds}") long accessTokenExpiredInSeconds,
      @Value("${token.refresh-token-expired-in-seconds}") long refreshTokenExpiredInSeconds,
      JwtEncoder jwtEncoder) {
    this.userDetailsService = userDetailsService;
    this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
    this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
    this.jwtEncoder = jwtEncoder;
  }

  public String issueAccessToken(AuthenticatedUser authenticatedUser, Instant issuedAt) {
    return generateToken(authenticatedUser, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueAccessToken(UserLogin userLogin, Instant issuedAt) {
    var authenticatedUser =
        (AuthenticatedUser) userDetailsService.loadUserByUsername(userLogin.email());

    return generateToken(authenticatedUser, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueRefreshToken() {
    return UUIDV7.randomUUID().toString();
  }
  
  private String generateToken(
      AuthenticatedUser authenticatedUser, Instant issuedAt, long expiredInSeconds) {
    var scope =
        authenticatedUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    var expiresAt = issuedAt.plusSeconds(expiredInSeconds);

    var claims =
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

  public String encodeClaimToJwt(JwtClaimsSet claims) {
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public boolean isRefreshTokenExpired(RefreshToken refreshToken) {
    var expiredAt = refreshToken.issuedDate().plusSeconds(refreshTokenExpiredInSeconds);

    return Instant.now().isAfter(expiredAt);
  }

  public String rotateRefreshTokenIfNeed(RefreshToken refreshToken) {
    var expiredAt = refreshToken.issuedDate().plusSeconds(refreshTokenExpiredInSeconds);
    var threadHoldToRotate = expiredAt.minusSeconds(TIME_FOR_ROTATE_IN_SECONDS);

    if (Instant.now().isAfter(threadHoldToRotate)) return issueRefreshToken();
    return refreshToken.token();
  }
}
