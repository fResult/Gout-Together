package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.repositories.RoleRepository;
import dev.fResult.goutTogether.users.services.RoleService;
import java.util.Arrays;
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
        Arrays.stream(UserRoleName.values())
            .map(roleName -> Role.of(roleName.getId(), roleName.name()))
            .toList();
    when(roleRepository.findAll()).thenReturn(mockRoles);

    var actualRoles = roleService.getRoles();

    assertEquals(mockRoles.size(), actualRoles.size());
    assertEquals(mockRoles, actualRoles);
  }
}
