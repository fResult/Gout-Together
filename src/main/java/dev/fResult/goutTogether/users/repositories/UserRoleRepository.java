package dev.fResult.goutTogether.users.repositories;

import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.entities.UserRole;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;

public interface UserRoleRepository extends ListCrudRepository<UserRole, Integer> {
  Optional<UserRole> findOneByUserId(AggregateReference<User, Integer> userId);

  void deleteByUserId(int userId);
}
