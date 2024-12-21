package dev.fResult.goutTogether.wallets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import dev.fResult.goutTogether.wallets.services.WalletServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
  private static final int USER_WALLET_ID = 1;
  private static final String IDEMPOTENCY_KEY = UUIDV7.randomUUID().toString();

  @InjectMocks private WalletServiceImpl walletService;

  @Mock UserWalletRepository userWalletRepository;
  @Mock TourCompanyWalletRepository tourCompanyWalletRepository;
  @Mock TourService tourService;

  @Test
  void whenCreateConsumerWalletThenSuccess() {
    // Arrange
    var USER_ID = 1;
    var mockCreatedUserWallet =
        UserWallet.of(
            USER_WALLET_ID, AggregateReference.to(USER_ID), Instant.now(), BigDecimal.ZERO);
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
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var mockUserWallet = UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    var expectedFoundUserWallet =
        UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, BigDecimal.ZERO);

    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));

    // Actual
    var actualFoundWallet = walletService.getConsumerWalletByUserId(USER_ID);

    // Assert
    assertEquals(expectedFoundUserWallet, actualFoundWallet);
  }

  @Test
  void whenFindConsumerWalletButNotFoundThenThrowEntityNotFoundException() {
    // Arrange
    var NOT_FOUND_USER_ID = 99999;
    var expectedErrorMessage =
        String.format(
            "%s with userId [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
    var notFoundUserRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
    when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> walletService.getConsumerWalletByUserId(NOT_FOUND_USER_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenDeleteConsumerWalletThenSuccess() {
    // Arrange
    var USER_ID = 1;
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var walletToDelete = UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(walletToDelete));
    doNothing().when(userWalletRepository).delete(any(UserWallet.class));

    // Actual
    var actualIsSuccess = walletService.deleteConsumerWalletByUserId(USER_WALLET_ID);

    // Assert
    verify(userWalletRepository, times(1)).delete(walletToDelete);
    assertTrue(actualIsSuccess);
  }

  @Test
  void whenCreateCompanyWalletThenSuccess() {
    // Arrange
    var COMPANY_ID = 1;
    var mockCreatedCompanyWallet =
        TourCompanyWallet.of(
            USER_WALLET_ID, AggregateReference.to(COMPANY_ID), Instant.now(), BigDecimal.ZERO);
    when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
        .thenReturn(mockCreatedCompanyWallet);

    // Actual
    var actualCreatedWallet = walletService.createTourCompanyWallet(COMPANY_ID);

    // Assert
    assertEquals(mockCreatedCompanyWallet, actualCreatedWallet);
  }

  @Test
  void whenGetConsumerAndCompanyWalletsThenSuccess() {
    // Arrange
    var COMPANY_WALLET_ID = 12;
    var userRef = AggregateReference.<User, Integer>to(1);
    var tourRef = AggregateReference.<Tour, Integer>to(1);
    var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(1);
    var mockBooking =
        Booking.of(
            1,
            userRef,
            tourRef,
            BookingStatus.PENDING.name(),
            Instant.now(),
            Instant.now(),
            IDEMPOTENCY_KEY);
    var mockTour =
        Tour.of(
            1,
            tourCompanyRef,
            "",
            "Camping in Bangkok",
            "Bangkok, Thailand",
            10,
            Instant.now().plus(45, ChronoUnit.DAYS),
            TourStatus.APPROVED.name());
    var mockUserWallet = UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    var mockTourCompanyWallet =
        TourCompanyWallet.of(COMPANY_WALLET_ID, tourCompanyRef, Instant.now(), BigDecimal.ZERO);

    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
    when(tourService.getTourById(anyInt())).thenReturn(mockTour);
    when(tourCompanyWalletRepository.findOneByTourCompanyId(tourCompanyRef))
        .thenReturn(Optional.of(mockTourCompanyWallet));

    // Actual
    var actualUserWallet = walletService.getConsumerAndTourCompanyWallets(mockBooking);

    // Assert
    assertEquals(mockUserWallet, actualUserWallet.getFirst());
    assertEquals(mockTourCompanyWallet, actualUserWallet.getSecond());
  }
}
