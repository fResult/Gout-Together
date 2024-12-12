package dev.fResult.goutTogether.wallets;

import dev.fResult.goutTogether.wallets.dtos.TourCompanyWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
  private final Logger logger = LoggerFactory.getLogger(WalletController.class);

  // User -> See own wallet
  @GetMapping("/me")
  public ResponseEntity<UserWalletInfoResponse> getMyUserWallet(Authentication authentication) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  // User -> Top-up (Assume doing via application, bank deduct on the background)
  @PostMapping
  public ResponseEntity<UserWalletInfoResponse> topUpUserWallet(
      @Validated @RequestBody WalletTopUpRequest body,
      @RequestHeader("idempotent-key") String idempotentKey,
      Authentication authentication) {

    throw new UnsupportedOperationException("Not Implement Yet");
  }

  // Company → See own wallet
  @GetMapping
  public ResponseEntity<TourCompanyWalletInfoResponse> getMyCompanyWallet() {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  // Company -> pay to bank account
  @PostMapping
  public ResponseEntity<TourCompanyWalletInfoResponse> withdrawMoney() {
    logger.info("Assume pay to bank");

    throw new UnsupportedOperationException("Not Implement Yet");
  }
}
