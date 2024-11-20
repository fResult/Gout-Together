package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.UserRole;
import java.util.Arrays;

import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.repositories.RoleRepository;
import dev.fResult.goutTogether.users.services.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
  @InjectMocks private RoleService roleService;
  @Mock private RoleRepository roleRepository;

  @Test
  void shouldReturnRoles() {
    var mockRoles =
        Arrays.stream(UserRole.values())
            .map(userRole -> Role.of(userRole.getId(), userRole.name()))
            .toList();
    when(roleRepository.findAll()).thenReturn(mockRoles);

    var actualRoles = roleService.getRoles();

    assertEquals(mockRoles.size(), actualRoles.size());
    assertEquals(mockRoles, actualRoles);
  }
}
