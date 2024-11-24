package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.entities.UserWallet;

import java.util.Optional;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  Optional<UserWallet> findUserWalletByUserId(int userId);
}
