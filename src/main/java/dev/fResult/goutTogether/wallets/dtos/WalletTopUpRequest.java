package dev.fResult.goutTogether.wallets.dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WalletTopUpRequest(
    @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount) {

  public WalletTopUpRequest of(BigDecimal amount) {
    return new WalletTopUpRequest(amount);
  }
}
