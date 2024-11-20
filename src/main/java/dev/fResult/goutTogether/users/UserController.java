package dev.fResult.goutTogether.users;

import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.services.RoleService;
import dev.fResult.goutTogether.users.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }
}
