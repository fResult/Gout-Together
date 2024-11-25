package dev.fResult.goutTogether.auths;

import dev.fResult.goutTogether.auths.entities.UserLogin;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface UserLoginRepository extends ListCrudRepository<UserLogin, Integer> {
    List<UserLogin> findByUserIdIn(Collection<Integer> userIds);

    Optional<UserLogin> findOneByEmail(String email);

    Optional<UserLogin> findOneByUserId(int userId);
}
