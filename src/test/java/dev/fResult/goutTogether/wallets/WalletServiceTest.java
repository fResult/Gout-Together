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
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionRepository;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.dtos.UserWalletInfoResponse;
import dev.fResult.goutTogether.wallets.dtos.WalletTopUpRequest;
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
  private static final int USER_WALLET_ID = 1;
  private static final String IDEMPOTENCY_KEY = UUIDV7.randomUUID().toString();

  @InjectMocks private WalletServiceImpl walletService;

  @Mock UserWalletRepository userWalletRepository;
  @Mock TourCompanyWalletRepository tourCompanyWalletRepository;
  @Mock TourService tourService;
  @Mock TransactionRepository transactionRepository;

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
      var body = WalletTopUpRequest.of(AMOUNT);
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var mockUserWallet = buildUserWallet(CURRENT_BALANCE);
      var mockUpdatedUserWallet =
          UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), BALANCE_TO_UPDATE);
      var expectedUpdatedUserWallet =
          UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, BALANCE_TO_UPDATE);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(transactionRepository.findOneByIdempotentKey(anyString())).thenReturn(Optional.empty());
      when(userWalletRepository.save(any(UserWallet.class))).thenReturn(mockUpdatedUserWallet);

      // Actual
      var actualUpdatedUserWallet =
          walletService.topUpConsumerWallet(USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      assertEquals(expectedUpdatedUserWallet, actualUpdatedUserWallet);
    }

    @Test
    void butTransactionAlreadyExistsThenReturnUserWalletWithoutUpdate() {
      // Arrange
      var body = WalletTopUpRequest.of(AMOUNT);
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var mockUserWallet = buildUserWallet(CURRENT_BALANCE);
      var mockTransaction = buildTopUpTransaction(USER_ID, 1, 1, AMOUNT);
      var expectedUpdatedUserWallet =
          UserWalletInfoResponse.of(USER_WALLET_ID, USER_ID, CURRENT_BALANCE);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(transactionRepository.findOneByIdempotentKey(anyString()))
          .thenReturn(Optional.of(mockTransaction));

      // Actual
      var actualUpdatedUserWallet =
          walletService.topUpConsumerWallet(USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      assertEquals(expectedUpdatedUserWallet, actualUpdatedUserWallet);
    }

    @Test
    void butWalletNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s with userId [%s] not found", UserWallet.class.getSimpleName(), NOT_FOUND_USER_ID);
      var body = WalletTopUpRequest.of(AMOUNT);
      var notFoundUserRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
      when(userWalletRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

      // Actual
      Executable actualExecutable =
          () -> walletService.topUpConsumerWallet(NOT_FOUND_USER_ID, IDEMPOTENCY_KEY, body);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
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
      var userRef = AggregateReference.<User, Integer>to(1);
      var tourRef = AggregateReference.<Tour, Integer>to(1);
      var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(1);
      var mockBooking = buildMockBooking(userRef, tourRef);
      var mockTour =
          Tour.of(
              TOUR_ID,
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

    @Test
    void butUserIdIsNullThenThrowException() {
      // Arrange
      var TOUR_ID = 1;
      AggregateReference<User, Integer> userIdRef = null;
      var expectedErrorMessage =
          String.format(
              "%s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), null, TOUR_ID);

      var mockBooking =
          Booking.of(
              1,
              null,
              AggregateReference.<Tour, Integer>to(TOUR_ID),
              BookingStatus.PENDING.name(),
              Instant.now(),
              Instant.now(),
              IDEMPOTENCY_KEY);

      // Actual
      Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butTourCompanyIdIsNullThenThrowException() {
      // Arrange
      var USER_ID = 1;
      AggregateReference<Tour, Integer> userIdRef = null;
      var expectedErrorMessage =
          String.format(
              "%s with userId [%s] tourId [%s] not found",
              Booking.class.getSimpleName(), USER_ID, null);
      var mockBooking =
          Booking.of(
              1,
              AggregateReference.<User, Integer>to(USER_ID),
              null,
              BookingStatus.PENDING.name(),
              Instant.now(),
              Instant.now(),
              IDEMPOTENCY_KEY);

      // Actual
      Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butTourNotFoundThenThrowException() {
      // Arrange
      var USER_ID = 1;
      var TOUR_ID = 1;
      var expectedErrorMessage =
          String.format("%s id [%d] not found", Tour.class.getSimpleName(), TOUR_ID);
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      var mockBooking = buildMockBooking(userRef, tourRef);
      var mockUserWallet = buildMockUserWallet(userRef);

      when(userWalletRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserWallet));
      when(tourService.getTourById(TOUR_ID))
          .thenThrow(new EntityNotFoundException(expectedErrorMessage));

      // Actual
      Executable actualUserWallet =
          () -> walletService.getConsumerAndTourCompanyWallets(mockBooking);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualUserWallet);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

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

    private UserWallet buildMockUserWallet(int userId) {
      return UserWallet.of(
          USER_WALLET_ID, AggregateReference.to(userId), Instant.now(), CURRENT_USER_BALANCE);
    }

    private TourCompanyWallet buildMockCompanyWallet(int tourCompanyId) {
      return TourCompanyWallet.of(
          1,
          AggregateReference.to(tourCompanyId),
          Instant.now().minus(30, ChronoUnit.HOURS),
          CURRENT_COMPANY_BALANCE);
    }

    @Test
    void thenSuccess() {
      // Arrange
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var companyRef = AggregateReference.<TourCompany, Integer>to(COMPANY_ID);
      var userWalletInput = buildMockUserWallet(USER_ID);
      var companyWalletInput = buildMockCompanyWallet(COMPANY_ID);
      var expectedUserWallet =
          UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), USER_BALANCE_AFTER_TRANSFER);
      var expectedCompanyWallet =
          TourCompanyWallet.of(1, companyRef, Instant.now(), COMPANY_BALANCE_AFTER_TRANSFER);

      when(userWalletRepository.save(any(UserWallet.class))).thenReturn(expectedUserWallet);
      when(tourCompanyWalletRepository.save(any(TourCompanyWallet.class)))
          .thenReturn(expectedCompanyWallet);

      // Actual
      var actualWallets =
          walletService.transferMoney(
              userWalletInput, companyWalletInput, AMOUNT_TO_TRANSFER, TransactionType.BOOKING);

      // Assert
      assertEquals(expectedUserWallet, actualWallets.getFirst());
      assertEquals(expectedCompanyWallet, actualWallets.getSecond());
    }

    @Test
    void butInsufficientBalanceThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s balance is insufficient for this operation", UserWallet.class.getSimpleName());
      var userWalletInput = buildMockUserWallet(USER_ID);
      var tourCompanyWalletInput = buildMockCompanyWallet(COMPANY_ID);

      // Actual
      Executable actualExecutable =
          () ->
              walletService.transferMoney(
                  userWalletInput,
                  tourCompanyWalletInput,
                  AMOUNT_OVER_USER_BALANCE,
                  TransactionType.BOOKING);

      // Assert
      var exception = assertThrowsExactly(InsufficientBalanceException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }
  }
}
