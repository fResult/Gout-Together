package dev.fResult.goutTogether.users.repositories;

import dev.fResult.goutTogether.users.entities.Role;
import org.springframework.data.repository.ListCrudRepository;

public interface RoleRepository extends ListCrudRepository<Role, Integer> {
}
