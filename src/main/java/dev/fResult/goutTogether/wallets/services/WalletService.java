package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  UserWalletInfoResponse getConsumerWalletByUserId(int userId);

  UserWalletInfoResponse topUpConsumerWallet(
      int userId, String idempotentKey, WalletTopUpRequest body);

  boolean deleteConsumerWalletByUserId(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);
}
