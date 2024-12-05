package dev.fResult.goutTogether.users;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.services.UserService;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public ResponseEntity<List<UserInfoResponse>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserInfoResponse> getUserById(@PathVariable int id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @GetMapping("/me")
  public ResponseEntity<UserInfoResponse> getMyUser(Authentication authentication) {
    var jwt = (Jwt) authentication.getPrincipal();
    var claims = jwt.getClaims();
    var userId = (long) claims.get(RESOURCE_ID_CLAIM);

    return ResponseEntity.ok(userService.getUserById(Math.toIntExact(userId)));
  }

  @PostMapping
  public ResponseEntity<UserInfoResponse> register(
      @Validated @RequestBody UserRegistrationRequest body) {
    logger.debug("[register] Registering a new {}", User.class.getSimpleName());
    var createdUser = userService.registerUser(body);
    var createdUserUri = URI.create(String.format("/api/v1/users/%d", createdUser.id()));

    return ResponseEntity.created(createdUserUri).body(createdUser);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserInfoResponse> updateUserById(
      @PathVariable int id, @Validated @RequestBody UserUpdateRequest body) {
    logger.debug("[updateUserById] Updating {} by id [{}]", User.class.getSimpleName(), id);

    return ResponseEntity.ok(userService.updateUserById(id, body));
  }

  // TODO: Re-think the forgot password flow
  @PatchMapping("/change-password")
  public ResponseEntity<UpdatePasswordResult> changePassword(
      Authentication authentication, @Validated @RequestBody UserChangePasswordRequest body) {

    var jwt = (Jwt) authentication.getPrincipal();
    var claims = jwt.getClaims();
    var email = (String) claims.get("sub");

    return ResponseEntity.ok(userService.changePassword(email, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable int id) {
    logger.debug("[deleteUserById] Deleting {} by id [{}]", User.class.getSimpleName(), id);
    userService.deleteUserById(id);

    return ResponseEntity.ok(
        String.format("Delete %s by id [%d] successfully", User.class.getSimpleName(), id));
  }
}
