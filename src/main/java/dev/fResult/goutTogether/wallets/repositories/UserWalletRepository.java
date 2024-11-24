package dev.fResult.goutTogether.wallets.repositories;

import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserWalletRepository extends CrudRepository<UserWallet, Integer> {
  Optional<UserWallet> findOneByUserId(int userId);
}
