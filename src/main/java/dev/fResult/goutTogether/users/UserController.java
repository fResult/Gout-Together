package dev.fResult.goutTogether.users;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.services.UserService;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
  public ResponseEntity<Page<UserInfoResponse>> getUsers(
      @RequestParam(required = false, defaultValue = "") String keyword,
      @RequestParam int page,
      @RequestParam int size,
      @RequestParam(defaultValue = "id") String field,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    var sort = Sort.by(direction, field);
    var pageable = PageRequest.of(page, size, sort);

    return ResponseEntity.ok(userService.getUsersByFirstName(keyword, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserInfoResponse> getUserById(@PathVariable int id) {
    return ResponseEntity.ok(userService.getUserById(id));
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
  @PatchMapping("/{id}/password")
  public ResponseEntity<UpdatePasswordResult> changePasswordByUserId(
      @PathVariable int id, @Validated @RequestBody UserChangePasswordRequest body) {

    logger.debug(
        "[changePasswordByUserId] Changing {} password by id [{}]", User.class.getSimpleName(), id);

    return ResponseEntity.ok(userService.changePasswordByUserId(id, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteUserById(@PathVariable int id) {
    logger.debug("[deleteUserById] Deleting {} by id [{}]", User.class.getSimpleName(), id);
    userService.deleteUserById(id);

    return ResponseEntity.ok(
        String.format("Delete %s by id [%d] successfully", User.class.getSimpleName(), id));
  }
}
