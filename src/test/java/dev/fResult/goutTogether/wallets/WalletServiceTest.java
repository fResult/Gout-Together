package dev.fResult.goutTogether.wallets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import dev.fResult.goutTogether.wallets.services.WalletServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
  private static final int WALLET_ID = 1;

  @InjectMocks private WalletServiceImpl walletService;

  @Mock UserWalletRepository userWalletRepository;
  @Mock TourCompanyWalletRepository tourCompanyWalletRepository;

  @Test
  void whenCreateConsumerWalletThenSuccess() {
    // Arrange
    var USER_ID = 1;
    var mockCreatedUserWallet =
        UserWallet.of(WALLET_ID, AggregateReference.to(USER_ID), Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.save(any(UserWallet.class))).thenReturn(mockCreatedUserWallet);

    // Actual
    var actualCreatedWallet = walletService.createConsumerWallet(USER_ID);

    // Assert
    assertEquals(mockCreatedUserWallet, actualCreatedWallet);
  }
}
