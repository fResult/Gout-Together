package dev.fResult.goutTogether.wallets.dtos;

import java.math.BigDecimal;

public record UserWalletInfoResponse(Integer userId, BigDecimal balance) {}
