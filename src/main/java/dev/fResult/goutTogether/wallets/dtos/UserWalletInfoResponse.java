package dev.fResult.goutTogether.wallets.dtos;

import dev.fResult.goutTogether.wallets.entities.UserWallet;
import java.math.BigDecimal;

public record UserWalletInfoResponse(Integer id, Integer userId, BigDecimal balance) {
  public static UserWalletInfoResponse of(Integer id, Integer userId, BigDecimal balance) {
    return new UserWalletInfoResponse(id, userId, balance);
  }

  public static UserWalletInfoResponse fromDao(UserWallet userWallet) {
    return UserWalletInfoResponse.of(
        userWallet.id(), userWallet.userId().getId(), userWallet.balance());
  }
}
