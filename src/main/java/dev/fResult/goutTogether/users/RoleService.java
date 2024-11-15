package dev.fResult.goutTogether.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Iterable<Role> all() {
        var availableRoles = roleRepository.findAll();
                 logger.info("availableRoles: {}", availableRoles);
        return availableRoles;

    }
}
