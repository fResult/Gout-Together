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
    var createdUser = userService.register(body);
    var createdUserUri = URI.create(String.format("/api/v1/users/%d", createdUser.id()));
    return ResponseEntity.created(createdUserUri).body(createdUser);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserInfoResponse> updateUser(
      @PathVariable int id, @Validated @RequestBody UserUpdateRequest body) {
    return ResponseEntity.ok(userService.updateUser(id, body));
  }

  // TODO: Re-think the forgot password flow
  @PatchMapping("/forgot-password")
  public ResponseEntity<UpdatePasswordResult> changePassword(
      @RequestBody UserForgotPasswordRequest body) {
    return ResponseEntity.ok(userService.changePassword(body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable int id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
