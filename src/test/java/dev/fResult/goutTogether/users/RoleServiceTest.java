package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.entities.UserRole;
import dev.fResult.goutTogether.users.repositories.RoleRepository;
import dev.fResult.goutTogether.users.repositories.UserRoleRepository;
import dev.fResult.goutTogether.users.services.RoleService;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {
  @InjectMocks private RoleService roleService;

  @Mock private RoleRepository roleRepository;
  @Mock private UserRoleRepository userRoleRepository;

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

  @Test
  void whenBindNewUserRoleThenSuccess() {
    // Arrange
    var USER_ID = 1;
    var ROLE = UserRoleName.CONSUMER;
    var mockUserRole =
        UserRole.of(1, AggregateReference.to(USER_ID), AggregateReference.to(ROLE.getId()));

    when(userRoleRepository.save(any(UserRole.class))).thenReturn(mockUserRole);

    // Actual
    var actualUserRole = roleService.bindNewUser(USER_ID, ROLE);

    // Assert
    assertEquals(mockUserRole, actualUserRole);
  }

  @Test
  void whenDeleteUserRoleByUserIdThenSuccess() {
    // Arrange
    var USER_ID = 1;
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var mockUserRole = UserRole.of(1, userRef, AggregateReference.to(1));

    when(userRoleRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserRole));
    doNothing().when(userRoleRepository).deleteByUserId(userRef);

    // Actual
    var actualDeleteResult = roleService.deleteUserRoleByUserId(USER_ID);

    // Assert
    assertTrue(actualDeleteResult);
  }
}
