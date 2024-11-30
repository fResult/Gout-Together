package dev.fResult.goutTogether.auths.controllers;

import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
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

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication) {
    logger.debug("[logout] Logging out by username [{}]", authentication.getName());
    authService.logout((Jwt) authentication.getPrincipal());

    return ResponseEntity.noContent().build();
  }
}
