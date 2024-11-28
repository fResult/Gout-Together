package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  UserWallet findConsumerWalletByUserId(int userId);

  boolean deleteConsumerWalletByUserId(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);
}
