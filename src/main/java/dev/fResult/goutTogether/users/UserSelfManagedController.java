package dev.fResult.goutTogether.users;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/me")
public class UserSelfManagedController {
  private final Logger logger = LoggerFactory.getLogger(UserSelfManagedController.class);

  private final UserService userService;

  public UserSelfManagedController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<UserInfoResponse> getMyUser(Authentication authentication) {
    final var userId = getMyId(authentication);

    return ResponseEntity.ok(userService.getUserById(Math.toIntExact(userId)));
  }

  @PatchMapping
  public ResponseEntity<UserInfoResponse> updateMyUser(
      @Validated @RequestBody UserUpdateRequest body, Authentication authentication) {

    final var userId = getMyId(authentication);

    logger.debug("[updateUserById] Updating {} by id [{}]", User.class.getSimpleName(), userId);

    return ResponseEntity.ok(userService.updateUserById(userId, body));
  }

  @PatchMapping("/password")
  public ResponseEntity<UpdatePasswordResult> changePassword(
      @Validated @RequestBody UserChangePasswordRequest body, Authentication authentication) {

    final var jwt = (Jwt) authentication.getPrincipal();
    final var email = jwt.getClaimAsString("sub");

    logger.debug(
        "[changePassword] Changing {} password by email [{}]", User.class.getSimpleName(), email);

    return ResponseEntity.ok(userService.changePasswordByEmail(email, body));
  }

  @DeleteMapping
  public ResponseEntity<String> deleteMyUser(Authentication authentication) {
    final var userId = getMyId(authentication);

    logger.debug("[deleteMyUser] Deleting {} by id [{}]", User.class.getSimpleName(), userId);
    userService.deleteUserById(userId);

    return ResponseEntity.ok(
        String.format("Delete %s by id [%d] successfully", User.class.getSimpleName(), userId));
  }

  private int getMyId(Authentication authentication) {
    final var jwt = (Jwt) authentication.getPrincipal();

    return Integer.parseInt(jwt.getClaimAsString(RESOURCE_ID_CLAIM));
  }
}
