package dev.fResult.goutTogether.wallets.services;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.math.BigDecimal;
import kotlin.Pair;

public interface WalletService {
  UserWallet createConsumerWallet(int userId);

  UserWalletInfoResponse getConsumerWalletByUserId(int userId);

  UserWalletInfoResponse topUpConsumerWallet(
      int userId, String idempotentKey, WalletTopUpRequest body);

  boolean deleteConsumerWalletByUserId(int userId);

  TourCompanyWallet createTourCompanyWallet(int tourCompanyId);

  Pair<UserWallet, TourCompanyWallet> getConsumerAndTourCompanyWallets(Booking booking);

  Pair<UserWallet, TourCompanyWallet> transferMoney(
      UserWallet userWallet,
      TourCompanyWallet tourCompanyWallet,
      BigDecimal amount,
      TransactionType transactionType);
}
