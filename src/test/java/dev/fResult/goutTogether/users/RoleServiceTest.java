package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.UserRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {
  @InjectMocks private RoleService roleService;
  @Mock private RoleRepository roleRepository;

  @Test
  void shouldReturnRoles() {
    var mockRoles =
        List.of(
            Role.of(1, UserRole.CONSUMER.name()),
            Role.of(2, UserRole.ADMIN.name()),
            Role.of(3, UserRole.COMPANY.name()));
    when(roleRepository.findAll()).thenReturn(mockRoles);

    var actualRoles = roleService.getRoles();

    assertEquals(mockRoles.size(), actualRoles.size());
    assertEquals(mockRoles, actualRoles);
  }
}
