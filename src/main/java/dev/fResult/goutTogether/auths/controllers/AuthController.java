package dev.fResult.goutTogether.auths.controllers;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;

import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.dtos.LogoutInfo;
import dev.fResult.goutTogether.auths.services.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auths")
public class AuthController {
  private final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest body) {
    logger.debug("[login] Logging in by username [{}]", body.username());

    return ResponseEntity.ok(authService.login(body));
  }

  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refreshToken(
      // TODO: Check about Authentication, bec we may be able to pass token to this API
      @RequestBody @Validated RefreshTokenRequest body) {
    logger.debug("[refreshToken] Refreshing in by refresh token");

    return ResponseEntity.ok(authService.refreshToken(body));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication) {
    var jwt = (Jwt) authentication.getPrincipal();
    var claims = jwt.getClaims();
    var resourceId = (long) claims.get(RESOURCE_ID_CLAIM);
    var roles = (String) claims.get(ROLES_CLAIM);
    var logoutInfo = LogoutInfo.of(Math.toIntExact(resourceId), roles);
    logger.debug("[logout] Logging out by username [{}]", authentication.getName());
    authService.logout(logoutInfo);

    return ResponseEntity.noContent().build();
  }
}
