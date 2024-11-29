package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.entities.UserRole;
import dev.fResult.goutTogether.users.repositories.RoleRepository;
import dev.fResult.goutTogether.users.repositories.UserRoleRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
  private final Logger logger = LoggerFactory.getLogger(RoleService.class);

  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;

  public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
    this.roleRepository = roleRepository;
    this.userRoleRepository = userRoleRepository;
  }

  public List<Role> getRoles() {
    var availableRoles = roleRepository.findAll();
    logger.info("[getRoles] Available Roles: {}", availableRoles);

    return availableRoles;
  }

  public UserRole bindNewUser(int userId, UserRoleName roleName) {
    logger.debug("[bindNewUser] new {} is binding", UserRole.class.getSimpleName());
    var userRoleToBind =
        UserRole.of(null, AggregateReference.to(userId), AggregateReference.to(roleName.getId()));

    var boundUserRole = userRoleRepository.save(userRoleToBind);
    logger.info(
        "[bindNewUser] New {} is registered: {}", UserRole.class.getSimpleName(), boundUserRole);

    return boundUserRole;
  }
}
