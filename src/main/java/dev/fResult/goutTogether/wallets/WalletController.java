package dev.fResult.goutTogether.wallets;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

import dev.fResult.goutTogether.wallets.dtos.TourCompanyWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
  private final Logger logger = LoggerFactory.getLogger(WalletController.class);

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  // User -> See own wallet
  @GetMapping("/me")
  public ResponseEntity<UserWalletInfoResponse> getMyUserWallet(Authentication authentication) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  // User -> Top-up (Assume doing via application, bank deduct on the background)
  @PostMapping
  public ResponseEntity<UserWalletInfoResponse> topUpUserWallet(
      @Validated @RequestBody WalletTopUpRequest body,
      @RequestHeader("idempotent-key") UUID idempotentKey,
      Authentication authentication) {

    var jwt = (Jwt) authentication.getPrincipal();
    var userId = jwt.getClaimAsString(RESOURCE_ID_CLAIM);
    logger.debug(
        "[topUpUserWallet] Topping up {} by user id [{}]",
        UserWallet.class.getSimpleName(),
        userId);

    var toppedUpWallet =
        walletService.topUpConsumerWallet(Integer.parseInt(userId), idempotentKey, body);

    return ResponseEntity.ok(toppedUpWallet);
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
