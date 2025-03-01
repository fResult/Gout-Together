package dev.fResult.goutTogether.wallets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.TourStatus;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.InsufficientBalanceException;
import dev.fResult.goutTogether.common.exceptions.UnsupportedTransactionTypeException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionRepository;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.TourCompanyWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
import dev.fResult.goutTogether.wallets.dtos.WalletWithdrawRequest;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.repositories.TourCompanyWalletRepository;
import dev.fResult.goutTogether.wallets.repositories.UserWalletRepository;
import dev.fResult.goutTogether.wallets.services.WalletServiceImpl;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
  private final int USER_WALLET_ID = 1;
  private final int TOUR_COMPANY_WALLET_ID = 2;

  private static final String IDEMPOTENCY_KEY = UUIDV7.randomUUID().toString();

  @InjectMocks private WalletServiceImpl walletService;

  @Mock UserWalletRepository userWalletRepository;
  @Mock TourCompanyWalletRepository tourCompanyWalletRepository;
  @Mock TourService tourService;
  @Mock TransactionRepository transactionRepository;

  private UserWallet buildMockUserWallet(int userId, BigDecimal balance) {
    return UserWallet.of(USER_WALLET_ID, AggregateReference.to(userId), Instant.now(), balance);
  }

  private TourCompanyWallet buildMockCompanyWallet(int tourCompanyId, BigDecimal balance) {
    return TourCompanyWallet.of(
        TOUR_COMPANY_WALLET_ID,
        AggregateReference.to(tourCompanyId),
        Instant.now().minus(30, ChronoUnit.HOURS),
        balance);
  }

  @Test
  void whenCreateConsumerWallet_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var mockCreatedUserWallet =
        UserWallet.of(
            USER_WALLET_ID, AggregateReference.to(USER_ID), Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.save(any(UserWallet.class))).thenReturn(mockCreatedUserWallet);

    // Actual
    final var actualCreatedWallet = walletService.createConsumerWallet(USER_ID);

    // Assert
    assertEquals(mockCreatedUserWallet, actualCreatedWallet);
  }

  @Test
  void whenGetConsumerWallet_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var mockUserWallet =
        UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    final var expectedFoundUserWallet =
        UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, BigDecimal.ZERO);

    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));

    // Actual
    final var actualFoundWallet = walletService.getConsumerWalletInfoByUserId(USER_ID);

    // Assert
    assertEquals(expectedFoundUserWallet, actualFoundWallet);
  }

  @Test
  void whenGetConsumerWallet_ButNotFound_ThenThrowEntityNotFoundException() {
    // Arrange
    final var NOT_FOUND_USER_ID = 99999;
    final var expectedErrorMessage =
        String.format(
            "%s with userId [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
    final var notFoundUserRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
    when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> walletService.getConsumerWalletInfoByUserId(NOT_FOUND_USER_ID);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenCreateCompanyWallet_ThenSuccess() {
    // Arrange
    final var COMPANY_ID = 1;
    final var mockCreatedCompanyWallet =
        TourCompanyWallet.of(
            USER_WALLET_ID, AggregateReference.to(COMPANY_ID), Instant.now(), BigDecimal.ZERO);
    when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
        .thenReturn(mockCreatedCompanyWallet);

    // Actual
    final var actualCreatedWallet = walletService.createTourCompanyWallet(COMPANY_ID);

    // Assert
    assertEquals(mockCreatedCompanyWallet, actualCreatedWallet);
  }

  @Nested
  class TopUpConsumerWalletTest {
    private final int USER_ID = 1;
    private final int NOT_FOUND_USER_ID = 99999;
    private final BigDecimal AMOUNT = BigDecimal.valueOf(100);
    private final BigDecimal CURRENT_BALANCE = BigDecimal.ZERO;
    private final BigDecimal BALANCE_TO_UPDATE = CURRENT_BALANCE.add(AMOUNT);
    private final String IDEMPOTENCY_KEY = UUIDV7.randomUUID().toString();

    private UserWallet buildUserWallet(BigDecimal balance) {
      return UserWallet.of(
          USER_WALLET_ID,
          AggregateReference.to(USER_ID),
          Instant.now().minus(1, ChronoUnit.DAYS),
          balance);
    }

    private Transaction buildTopUpTransaction(
        int userId, int tourCompanyId, int bookingId, BigDecimal amount) {

      return Transaction.of(
          1,
          AggregateReference.<User, Integer>to(userId),
          AggregateReference.<TourCompany, Integer>to(tourCompanyId),
          AggregateReference.<Booking, Integer>to(bookingId),
          Instant.now().minusSeconds(10),
          amount,
          TransactionType.TOP_UP,
          IDEMPOTENCY_KEY);
    }

    @Test
    void thenSuccess() {
      // Arrange
      final var body = WalletTopUpRequest.of(AMOUNT);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var mockUserWallet = buildUserWallet(CURRENT_BALANCE);
      final var mockUpdatedUserWallet =
          UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BALANCE_TO_UPDATE);
      final var expectedUpdatedUserWallet =
          UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, BALANCE_TO_UPDATE);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(transactionRepository.findOneByIdempotentKey(anyString())).thenReturn(Optional.empty());
      when(userWalletRepository.save(any(UserWallet.class))).thenReturn(mockUpdatedUserWallet);

      // Actual
      final var actualUpdatedUserWallet =
          walletService.topUpConsumerWallet(USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      assertEquals(expectedUpdatedUserWallet, actualUpdatedUserWallet);
    }

    @Test
    void butTransactionAlreadyExists_ThenReturnUserWalletWithoutReTopUp() {
      // Arrange
      final var body = WalletTopUpRequest.of(AMOUNT);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var mockUserWallet = buildUserWallet(CURRENT_BALANCE);
      final var mockTransaction = buildTopUpTransaction(USER_ID, 1, 1, AMOUNT);
      final var expectedUpdatedUserWallet =
          UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, CURRENT_BALANCE);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(transactionRepository.findOneByIdempotentKey(anyString()))
          .thenReturn(Optional.of(mockTransaction));

      // Actual
      final var actualUpdatedUserWallet =
          walletService.topUpConsumerWallet(USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      assertEquals(expectedUpdatedUserWallet, actualUpdatedUserWallet);
    }

    @Test
    void butWalletNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with userId [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
      final var body = WalletTopUpRequest.of(AMOUNT);
      final var notFoundUserRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
      when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable =
          () -> walletService.topUpConsumerWallet(NOT_FOUND_USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Test
  void whenDeleteConsumerWallet_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var walletToDelete =
        UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(walletToDelete));
    doNothing().when(userWalletRepository).delete(any(UserWallet.class));

    // Actual
    final var actualIsSuccess = walletService.deleteConsumerWalletByUserId(USER_WALLET_ID);

    // Assert
    verify(userWalletRepository, times(1)).delete(walletToDelete);
    assertTrue(actualIsSuccess);
  }

  @Test
  void whenGetCompanyWallet_ThenSuccess() {
    // Arrange
    final var TOUR_COMPANY_ID = 1;
    final var companyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
    final var mockCompanyWallet = buildMockCompanyWallet(TOUR_COMPANY_ID, BigDecimal.ZERO);
    final var expectedFoundCompanyWalletInfo =
        TourCompanyWalletInfoResponse.of(TOUR_COMPANY_WALLET_ID, TOUR_COMPANY_ID, BigDecimal.ZERO);

    when(tourCompanyWalletRepository.findOneByTourCompanyId(companyRef))
        .thenReturn(Optional.of(mockCompanyWallet));

    // Actual
    final var actualFoundWallet =
        walletService.getTourCompanyWalletInfoByTourCompanyId(TOUR_COMPANY_ID);

    // Assert
    assertEquals(expectedFoundCompanyWalletInfo, actualFoundWallet);
  }

  @Test
  void whenGetCompanyWallet_ButNotFound_ThenThrowEntityNotFoundException() {
    // Arrange
    final var NOT_FOUND_USER_ID = 99999;
    final var expectedErrorMessage =
        String.format(
            "%s with userId [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
    final var notFoundUserRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
    when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> walletService.getConsumerWalletInfoByUserId(NOT_FOUND_USER_ID);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Nested
  class GetConsumerAndCompanyWalletsTest {
    private final int USER_ID = 1;
    private final int TOUR_ID = 1;
    private final int COMPANY_WALLET_ID = 12;

    private Booking buildMockBooking(
        AggregateReference<User, Integer> userRef, AggregateReference<Tour, Integer> tourRef) {

      return Booking.of(
          1,
          userRef,
          tourRef,
          BookingStatus.PENDING.name(),
          Instant.now(),
          Instant.now(),
          IDEMPOTENCY_KEY);
    }

    private UserWallet buildMockUserWallet(AggregateReference<User, Integer> userRef) {
      return UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
    }

    @Test
    void thenSuccess() {
      // Arrange
      final var userRef = AggregateReference.<User, Integer>to(1);
      final var tourRef = AggregateReference.<Tour, Integer>to(1);
      final var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(1);
      final var mockBooking = buildMockBooking(userRef, tourRef);
      final var mockTour =
          Tour.of(
              TOUR_ID,
              tourCompanyRef,
              "",
              "Camping in Bangkok",
              "Bangkok, Thailand",
              10,
              Instant.now().plus(45, ChronoUnit.DAYS),
              TourStatus.APPROVED.name());
      final var mockUserWallet =
          UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BigDecimal.ZERO);
      final var mockTourCompanyWallet =
          TourCompanyWallet.of(COMPANY_WALLET_ID, tourCompanyRef, Instant.now(), BigDecimal.ZERO);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(tourService.getTourById(anyInt())).thenReturn(mockTour);
      when(tourCompanyWalletRepository.findOneByTourCompanyId(tourCompanyRef))
          .thenReturn(Optional.of(mockTourCompanyWallet));

      // Actual
      final var actualUserWallet = walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      assertEquals(mockUserWallet, actualUserWallet.getFirst());
      assertEquals(mockTourCompanyWallet, actualUserWallet.getSecond());
    }

    @Test
    void butUserIdIsNull_ThenThrowException() {
      // Arrange
      final AggregateReference<User, Integer> userIdRef = null;
      final var expectedErrorMessage =
          String.format(
              "%s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), null, TOUR_ID);

      final var mockBooking =
          Booking.of(
              1,
              null,
              AggregateReference.<Tour, Integer>to(TOUR_ID),
              BookingStatus.PENDING.name(),
              Instant.now(),
              Instant.now(),
              IDEMPOTENCY_KEY);

      // Actual
      final Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butTourCompanyIdIsNull_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), USER_ID, null);
      final var mockBooking =
          Booking.of(
              1,
              AggregateReference.<User, Integer>to(USER_ID),
              null,
              BookingStatus.PENDING.name(),
              Instant.now(),
              Instant.now(),
              IDEMPOTENCY_KEY);

      // Actual
      final Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butTourNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format("%s id [%d] not found", Tour.class.getSimpleName(), TOUR_ID);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      final var mockBooking = buildMockBooking(userRef, tourRef);
      final var mockUserWallet = buildMockUserWallet(userRef);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(tourService.getTourById(TOUR_ID))
          .thenThrow(new EntityNotFoundException(expectedErrorMessage));

      // Actual
      final Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class WithdrawCompanyWalletTest {
    private final int TOUR_COMPANY_ID = 2;

    @Test
    void whenSuccess() {
      // Arrange
      final var AMOUNT_TO_WITHDRAW = BigDecimal.valueOf(80_000);
      final var CURRENT_BALANCE = BigDecimal.valueOf(1_000_000);
      final var BALANCE_AFTER_WITHDRAW = BigDecimal.valueOf(920_000);
      final var body = WalletWithdrawRequest.of(AMOUNT_TO_WITHDRAW);
      final var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);

      final var mockCompanyWallet = buildMockCompanyWallet(TOUR_COMPANY_ID, CURRENT_BALANCE);
      final var mockCompanyWalletToWithdraw =
          buildMockCompanyWallet(TOUR_COMPANY_ID, BALANCE_AFTER_WITHDRAW);
      final var expectedCompanyWalletInfo =
          TourCompanyWalletInfoResponse.of(
              TOUR_COMPANY_WALLET_ID, TOUR_COMPANY_ID, BALANCE_AFTER_WITHDRAW);

      when(tourCompanyWalletRepository.findOneByTourCompanyId(tourCompanyRef))
          .thenReturn(Optional.of(mockCompanyWallet));
      when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
          .thenReturn(mockCompanyWalletToWithdraw);

      // Actual
      final var actualWithdrewWallet =
          walletService.withdrawTourCompanyWallet(TOUR_COMPANY_ID, IDEMPOTENCY_KEY, body);

      // Assert
      assertEquals(expectedCompanyWalletInfo, actualWithdrewWallet);
    }

    @Test
    void butWalletNotFound_ThenThrowException() {
      // Arrange
      final var NOT_FOUND_TOUR_COMPANY_ID = 99999;
      final var body = WalletWithdrawRequest.of(BigDecimal.TEN);
      final var tourCompanyRef =
          AggregateReference.<TourCompany, Integer>to(NOT_FOUND_TOUR_COMPANY_ID);
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%s] not found",
              TourCompanyWallet.class.getSimpleName(), "tourCompanyId", NOT_FOUND_TOUR_COMPANY_ID);

      when(tourCompanyWalletRepository.findOneByTourCompanyId(tourCompanyRef))
          .thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable =
          () ->
              walletService.withdrawTourCompanyWallet(
                  NOT_FOUND_TOUR_COMPANY_ID, IDEMPOTENCY_KEY, body);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class TransferMoneyTest {
    @Nested
    class TransferMoneyForBookingTest {
      private final int USER_ID = 1;
      private final int COMPANY_ID = 2;
      private final BigDecimal AMOUNT_TO_TRANSFER = BigDecimal.valueOf(100);
      private final BigDecimal AMOUNT_OVER_USER_BALANCE = BigDecimal.valueOf(201);
      private final BigDecimal CURRENT_USER_BALANCE = BigDecimal.valueOf(200);
      private final BigDecimal CURRENT_COMPANY_BALANCE = BigDecimal.valueOf(300);
      private final BigDecimal USER_BALANCE_AFTER_TRANSFER =
          CURRENT_USER_BALANCE.subtract(AMOUNT_TO_TRANSFER);
      private final BigDecimal COMPANY_BALANCE_AFTER_TRANSFER =
          CURRENT_COMPANY_BALANCE.add(AMOUNT_TO_TRANSFER);

      @Test
      void thenSuccess() {
        // Arrange
        final var userRef = AggregateReference.<User, Integer>to(USER_ID);
        final var companyRef = AggregateReference.<TourCompany, Integer>to(COMPANY_ID);
        final var userWalletInput = buildMockUserWallet(USER_ID, CURRENT_USER_BALANCE);
        final var companyWalletInput = buildMockCompanyWallet(COMPANY_ID, CURRENT_COMPANY_BALANCE);
        final var expectedUserWallet =
            UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), USER_BALANCE_AFTER_TRANSFER);
        final var expectedCompanyWallet =
            TourCompanyWallet.of(1, companyRef, Instant.now(), COMPANY_BALANCE_AFTER_TRANSFER);

        when(userWalletRepository.save(any(UserWallet.class))).thenReturn(expectedUserWallet);
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
            .thenReturn(expectedCompanyWallet);

        // Actual
        final var actualWallets =
            walletService.transferMoney(
                userWalletInput, companyWalletInput, AMOUNT_TO_TRANSFER, TransactionType.BOOKING);

        // Assert
        assertEquals(expectedUserWallet, actualWallets.getFirst());
        assertEquals(expectedCompanyWallet, actualWallets.getSecond());
      }

      @Test
      void butInsufficientBalance_ThenThrowException() {
        // Arrange
        final var expectedErrorMessage =
            String.format(
                "%s balance is insufficient for this operation", UserWallet.class.getSimpleName());
        final var userWalletInput = buildMockUserWallet(USER_ID, CURRENT_USER_BALANCE);
        final var tourCompanyWalletInput =
            buildMockCompanyWallet(COMPANY_ID, CURRENT_COMPANY_BALANCE);

        // Actual
        final Executable actualExecutable =
            () ->
                walletService.transferMoney(
                    userWalletInput,
                    tourCompanyWalletInput,
                    AMOUNT_OVER_USER_BALANCE,
                    TransactionType.BOOKING);

        // Assert
        final var exception = assertThrowsExactly(InsufficientBalanceException.class, actualExecutable);
        assertEquals(expectedErrorMessage, exception.getMessage());
      }
    }

    @Nested
    class TransferMoneyForRefundTest {
      private final int COMPANY_ID = 2;
      private final int USER_ID = 1;
      private final BigDecimal AMOUNT_TO_TRANSFER = BigDecimal.valueOf(100);
      private final BigDecimal CURRENT_COMPANY_BALANCE = BigDecimal.valueOf(300);
      private final BigDecimal CURRENT_USER_BALANCE = BigDecimal.valueOf(200);
      private final BigDecimal COMPANY_BALANCE_AFTER_TRANSFER =
          CURRENT_COMPANY_BALANCE.subtract(AMOUNT_TO_TRANSFER);
      private final BigDecimal USER_BALANCE_AFTER_TRANSFER =
          CURRENT_USER_BALANCE.add(AMOUNT_TO_TRANSFER);

      @Test
      void thenSuccess() {
        // Arrange
        final var userWalletInput = buildMockUserWallet(USER_ID, CURRENT_USER_BALANCE);
        final var companyWalletInput = buildMockCompanyWallet(COMPANY_ID, CURRENT_COMPANY_BALANCE);
        final var userRef = AggregateReference.<User, Integer>to(USER_ID);
        final var companyRef = AggregateReference.<TourCompany, Integer>to(COMPANY_ID);
        final var expectedUserWallet =
            UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), USER_BALANCE_AFTER_TRANSFER);
        final var expectedCompanyWallet =
            TourCompanyWallet.of(1, companyRef, Instant.now(), COMPANY_BALANCE_AFTER_TRANSFER);

        when(userWalletRepository.save(any(UserWallet.class))).thenReturn(expectedUserWallet);
        when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
            .thenReturn(expectedCompanyWallet);

        // Actual
        final var actualTransferredWallets =
            walletService.transferMoney(
                userWalletInput, companyWalletInput, AMOUNT_TO_TRANSFER, TransactionType.REFUND);

        // Assert
        assertEquals(expectedUserWallet, actualTransferredWallets.getFirst());
        assertEquals(expectedCompanyWallet, actualTransferredWallets.getSecond());
      }
    }

    @Test
    void forTopUp_ButUnsupported_ThenThrowError() {
      // Arrange
      final var AMOUNT_TO_TRANSFER = BigDecimal.valueOf(200);
      final var expectedErrorMessage =
          String.format(
              "Transaction type [%s] is not supported for this transferring method",
              TransactionType.TOP_UP);
      final var userWalletInput = buildMockUserWallet(1, BigDecimal.ZERO);
      final var companyWalletInput = buildMockCompanyWallet(2, BigDecimal.ZERO);

      // Actual
      final Executable actualExecutable =
          () ->
              walletService.transferMoney(
                  userWalletInput, companyWalletInput, AMOUNT_TO_TRANSFER, TransactionType.TOP_UP);

      // Assert
      final var exception =
          assertThrowsExactly(UnsupportedTransactionTypeException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void forWithdraw_ButUnsupported_ThenThrowError() {
      // Arrange
      final var AMOUNT_TO_TRANSFER = BigDecimal.valueOf(200);
      final var expectedErrorMessage =
          String.format(
              "Transaction type [%s] is not supported for this transferring method",
              TransactionType.WITHDRAW);
      final var userWalletInput = buildMockUserWallet(1, BigDecimal.ZERO);
      final var companyWalletInput = buildMockCompanyWallet(2, BigDecimal.ZERO);

      // Actual
      final Executable actualExecutable =
          () ->
              walletService.transferMoney(
                  userWalletInput,
                  companyWalletInput,
                  AMOUNT_TO_TRANSFER,
                  TransactionType.WITHDRAW);

      // Assert
      final var exception =
          assertThrowsExactly(UnsupportedTransactionTypeException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }
}
