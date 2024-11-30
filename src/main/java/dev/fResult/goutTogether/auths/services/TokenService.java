package dev.fResult.goutTogether.auths.services;

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
  private static final String ROLE_CLAIM = "roles";

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

  public String issueAccessToken(Authentication authentication) {
    return generateToken(authentication, accessTokenExpiredInSeconds);
  }

  public String issueRefreshToken(Authentication authentication) {
    return generateToken(authentication, refreshTokenExpiredInSeconds);
  }

  public String generateToken(Authentication authentication, long expiredInSeconds) {
    var scope =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

    var issuedAt = Instant.now();
    var expireAt = issuedAt.plusSeconds(expiredInSeconds);

    var claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .issuedAt(issuedAt)
            .subject(authentication.getName())
            .claim(ROLE_CLAIM, scope)
            .expiresAt(expireAt)
            .build();

    return encodeClaimToJwt(claims);
  }

  public String encodeClaimToJwt(JwtClaimsSet claims) {
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
