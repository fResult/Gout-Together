package dev.fResult.goutTogether.users;

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
  public ResponseEntity<List<User>> getUsers() {
    return ResponseEntity.ok(userService.getUsers());
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> byId(@PathVariable int id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }

  @PostMapping
  public ResponseEntity<User> register(@Validated @RequestBody User user) {
    var createdUserUri = URI.create(String.format("/api/v1/users/%d", user.id()));
    return ResponseEntity.created(createdUserUri).body(userService.register(user));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<User> updateUser(@PathVariable int id, @Validated @RequestBody User user) {
    return ResponseEntity.ok(userService.updateUser(id, user));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable int id) {
    userService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
