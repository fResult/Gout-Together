package dev.fResult.goutTogether.auths.repositories;

import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.users.entities.User;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;

public interface UserLoginRepository extends ListCrudRepository<UserLogin, Integer> {
  List<UserLogin> findByUserIdIn(Collection<Integer> userIds);

  Optional<UserLogin> findOneByEmail(String email);

  Optional<UserLogin> findOneByUserId(AggregateReference<User, Integer> userId);

  Optional<UserLogin> findOneByEmailAndPassword(String userEmail, String password);
}
