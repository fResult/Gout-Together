package dev.fResult.goutTogether.auths.services;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  private static final String ISSUER = "gout-together";

  private final long accessTokenExpiredInSeconds;
  private final long refreshTokenExpiredInSeconds;
  private final JwtEncoder jwtEncoder;

  public TokenService(
      @Value("${token.access-token-expired-in-seconds}") long accessTokenExpiredInSeconds,
      @Value("${token.refresh-token-expired-in-seconds}") long refreshTokenExpiredInSeconds,
      JwtEncoder jwtEncoder) {
    this.accessTokenExpiredInSeconds = accessTokenExpiredInSeconds;
    this.refreshTokenExpiredInSeconds = refreshTokenExpiredInSeconds;
    this.jwtEncoder = jwtEncoder;
  }

  public String issueAccessToken(Authentication authentication, Instant issuedAt) {
    return generateToken(authentication, issuedAt, accessTokenExpiredInSeconds);
  }

  public String issueRefreshToken() {
    return UUIDV7.randomUUID().toString();
  }

  public String generateToken(
      Authentication authentication, Instant issuedAt, long expiredInSeconds) {

    var scope =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    var expiresAt = issuedAt.plusSeconds(expiredInSeconds);
    var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

    var claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .issuedAt(issuedAt)
            .subject(authentication.getName())
            .claim(ROLES_CLAIM, scope)
            .claim(RESOURCE_ID_CLAIM, authenticatedUser.userId())
            .expiresAt(expiresAt)
            .build();

    return encodeClaimToJwt(claims);
  }

  public String encodeClaimToJwt(JwtClaimsSet claims) {
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
