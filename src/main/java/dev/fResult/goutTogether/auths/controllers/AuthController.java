package dev.fResult.goutTogether.auths.controllers;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;

import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.dtos.LogoutInfo;
import dev.fResult.goutTogether.auths.dtos.RefreshTokenRequest;
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
    final var jwt = (Jwt) authentication.getPrincipal();
    final var resourceId = jwt.getClaimAsString(RESOURCE_ID_CLAIM);
    final var roles = jwt.getClaimAsString(ROLES_CLAIM);
    final var logoutInfo = LogoutInfo.of(Integer.parseInt(resourceId), roles);
    logger.debug("[logout] Logging out by username [{}]", authentication.getName());
    authService.logout(logoutInfo);

    return ResponseEntity.noContent().build();
  }
}
