package dev.fResult.goutTogether.wallets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import dev.fResult.goutTogether.wallets.services.WalletServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
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

  @Test
  void whenFindConsumerWalletThenSuccess() {
    // Arrange
    var USER_ID = 1;
    AggregateReference<User, Integer> userRef = AggregateReference.to(USER_ID);
    var mockFoundUserWallet = UserWallet.of(WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.findOneByUserId(userRef))
        .thenReturn(Optional.of(mockFoundUserWallet));

    // Actual
    var actualFoundWallet = walletService.findConsumerWalletByUserId(USER_ID);

    // Assert
    assertEquals(mockFoundUserWallet, actualFoundWallet);
  }

  @Test
  void whenFindConsumerWalletButNotFoundThenThrowEntityNotFoundException() {
    // Arrange
    var NOT_FOUND_USER_ID = 99999;
    var expectedErrorMessage =
        String.format("%s id [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
    AggregateReference<User, Integer> notFoundUserRef = AggregateReference.to(NOT_FOUND_USER_ID);
    when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> walletService.findConsumerWalletByUserId(NOT_FOUND_USER_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenDeleteConsumerWalletThenSuccess() {
    // Arrange
    var USER_ID = 1;
    AggregateReference<User, Integer> userRef = AggregateReference.to(USER_ID);
    var walletToDelete = UserWallet.of(WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(walletToDelete));
    doNothing().when(userWalletRepository).delete(any(UserWallet.class));

    // Actual
    var actualIsSuccess = walletService.deleteConsumerWalletById(WALLET_ID);

    // Assert
    verify(userWalletRepository, times(1)).delete(walletToDelete);
    assertTrue(actualIsSuccess);
  }
}
