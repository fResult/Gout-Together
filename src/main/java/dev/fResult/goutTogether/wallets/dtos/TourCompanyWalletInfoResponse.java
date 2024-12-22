package dev.fResult.goutTogether.wallets.dtos;

import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import java.math.BigDecimal;

public record TourCompanyWalletInfoResponse(Integer id, Integer tourCompanyId, BigDecimal balance) {
  public static TourCompanyWalletInfoResponse of(
      Integer id, Integer tourCompanyId, BigDecimal balance) {

    return new TourCompanyWalletInfoResponse(id, tourCompanyId, balance);
  }

  public static TourCompanyWalletInfoResponse fromDao(TourCompanyWallet companyWallet) {
    return TourCompanyWalletInfoResponse.of(
        companyWallet.id(), companyWallet.tourCompanyId().getId(), companyWallet.balance());
  }
}
