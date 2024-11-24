package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  UserWallet findUserWalletByUserId(int userId);

  boolean deleteUserWalletById(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);
}
