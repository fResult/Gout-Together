package dev.fResult.goutTogether.wallets.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletWithdrawRequest(
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount) {

  public static WalletWithdrawRequest of(BigDecimal amount) {
    return new WalletWithdrawRequest(amount);
  }
}
