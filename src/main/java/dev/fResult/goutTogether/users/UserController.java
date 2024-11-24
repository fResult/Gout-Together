package dev.fResult.goutTogether.users;

import dev.fResult.goutTogether.auths.UserForgotPasswordRequest;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<UserInfoResponse> byId(@PathVariable int id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @PostMapping
  public ResponseEntity<UserInfoResponse> register(
      @Validated @RequestBody UserRegistrationRequest body) {
    logger.debug("[register] Registering a new {}", User.class.getSimpleName());
    var createdUser = userService.register(body);
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
  @PatchMapping("/forgot-password")
  public ResponseEntity<UpdatePasswordResult> changePassword(
      @RequestBody UserForgotPasswordRequest body) {
    return ResponseEntity.ok(userService.changePassword(body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable int id) {
    logger.debug("[deleteUserById] Deleting {} by id [{}]", User.class.getSimpleName(), id);
    userService.deleteUserById(id);

    return ResponseEntity.ok(
        String.format("%s with id [%d] has been deleted", User.class.getSimpleName(), id));
  }
}
