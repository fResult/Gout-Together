package dev.fResult.goutTogether.users;

import java.util.List;

import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
  private final Logger logger = LoggerFactory.getLogger(RoleService.class);
  private final RoleRepository roleRepository;

  public RoleService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public List<Role> getRoles() {
    var availableRoles = roleRepository.findAll();
    logger.info("[getRoles] Available Roles: {}", availableRoles);

    return availableRoles;
  }
}
