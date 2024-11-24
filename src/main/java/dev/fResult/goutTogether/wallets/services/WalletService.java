package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.util.Optional;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  Optional<UserWallet> findUserWalletByUserId(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);
}
