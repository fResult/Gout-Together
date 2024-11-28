package dev.fResult.goutTogether.admins;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admins")
public class AdminManagementController {
  @GetMapping
  public String helloAdmin() {
    return "Hello, Admin";
  }
}
