package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.util.UUID;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  UserWallet findConsumerWalletByUserId(int userId);

  UserWalletInfoResponse topUpConsumerWallet(
      int userId, UUID idempotentKey, WalletTopUpRequest body);

  boolean deleteConsumerWalletByUserId(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);
}
