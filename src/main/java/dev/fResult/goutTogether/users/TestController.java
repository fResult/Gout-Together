package dev.fResult.goutTogether.users;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private final RoleService roleService;

    public TestController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/roles")
    public Iterable<Role> allRoles() {
        return roleService.getRoles();
    }
}
