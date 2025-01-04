package dev.fResult.goutTogether.wallets.repositories;

import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.ListCrudRepository;

public interface UserWalletRepository extends ListCrudRepository<UserWallet, Integer> {
  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<UserWallet> findOneByUserId(AggregateReference<User, Integer> userId);
}
