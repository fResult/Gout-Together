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
    final var mockRoles =
        Arrays.stream(UserRoleName.values())
            .map(roleName -> Role.of(roleName.getId(), roleName.name()))
            .toList();
    when(roleRepository.findAll()).thenReturn(mockRoles);

    final var actualRoles = roleService.getRoles();

    assertEquals(mockRoles.size(), actualRoles.size());
    assertEquals(mockRoles, actualRoles);
  }

  @Test
  void whenBindNewUserRole_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var ROLE = UserRoleName.CONSUMER;
    final var mockUserRole =
        UserRole.of(1, AggregateReference.to(USER_ID), AggregateReference.to(ROLE.getId()));

    when(userRoleRepository.save(any(UserRole.class))).thenReturn(mockUserRole);

    // Actual
    final var actualUserRole = roleService.bindNewUser(USER_ID, ROLE);

    // Assert
    assertEquals(mockUserRole, actualUserRole);
  }

  @Test
  void whenDeleteUserRoleByUserId_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var mockUserRole = UserRole.of(1, userRef, AggregateReference.to(1));

    when(userRoleRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserRole));
    doNothing().when(userRoleRepository).delete(mockUserRole);

    // Actual
    final var actualDeleteResult = roleService.deleteUserRoleByUserId(USER_ID);

    // Assert
    assertTrue(actualDeleteResult);
  }

  @Test
  void whenDeleteUserRoleByUserId_ButNotFound_ThenThrowException() {
    // Arrange
    final var USER_ID = 1;
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    when(userRoleRepository.findOneByUserId(userRef)).thenReturn(Optional.empty());

    // Actual
    final var actualDeleteResult = roleService.deleteUserRoleByUserId(USER_ID);

    // Assert
    assertFalse(actualDeleteResult);
  }
}
