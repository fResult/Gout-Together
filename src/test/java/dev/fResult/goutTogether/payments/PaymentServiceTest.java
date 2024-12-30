package dev.fResult.goutTogether.payments;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.payments.services.PaymentServiceImpl;
import dev.fResult.goutTogether.qrcodes.QrCodeReference;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.transactions.Transaction;
import dev.fResult.goutTogether.transactions.TransactionService;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import kotlin.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
  private final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
  private final int USER_ID = 1;
  private final int TOUR_COMPANY_ID = 1;
  private final int BOOKING_ID = 1;
  private final int USER_WALLET_ID = 1;
  private final int COMPANY_WALLET_ID = 1;

  private final BigDecimal USER_WALLET_BALANCE = BigDecimal.valueOf(100);
  private final BigDecimal COMPANY_WALLET_BALANCE = BigDecimal.valueOf(100);

  @Value("${goutapp.booking.tour-price}")
  private double tourPrice;

  private final BigDecimal TOUR_PRICE = BigDecimal.valueOf(tourPrice);
  private final int QR_CODE_REF_ID = 1;

  private PaymentServiceImpl paymentService;

  @Mock private BookingRepository bookingRepository;
  @Mock private QrCodeService qrCodeService;
  @Mock private BookingService bookingService;
  @Mock private WalletService walletService;
  @Mock private TourCountService tourCountService;
  @Mock private TransactionService transactionService;

  @BeforeEach
  void setUp() {
    paymentService =
        new PaymentServiceImpl(
            tourPrice,
            bookingRepository,
            qrCodeService,
            bookingService,
            walletService,
            tourCountService,
            transactionService);
  }

  private UserWallet buildUserWallet(int id, int userId, BigDecimal balance) {
    return UserWallet.of(
        id, AggregateReference.to(userId), Instant.now().minus(17, ChronoUnit.DAYS), balance);
  }

  private TourCompanyWallet buildTourCompanyWallet(int id, int tourCompanyId, BigDecimal balance) {
    return new TourCompanyWallet(
        id,
        AggregateReference.to(tourCompanyId),
        Instant.now().minus(12, ChronoUnit.MINUTES),
        balance);
  }

  private Booking buildPendingBooking(int id, int userId, int tourId) {
    return Booking.of(
        id,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.PENDING.name(),
        Instant.now().minus(10, ChronoUnit.MINUTES),
        Instant.now().minus(5, ChronoUnit.MINUTES),
        IDEMPOTENT_KEY);
  }

  private Booking buildCompletedBooking(int id, int userId, int tourId) {
    return Booking.of(
        id,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.COMPLETED.name(),
        Instant.now().minus(10, ChronoUnit.MINUTES),
        Instant.now().minus(5, ChronoUnit.MINUTES),
        IDEMPOTENT_KEY);
  }

  private String buildQrCodeContent(int bookingId) {
    return String.format("%s/%d", API_PAYMENT_PATH, bookingId);
  }

  @Test
  void whenGenerateQrCode_ThenSuccess() throws WriterException {
    // Arrange
    var mockQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

    when(qrCodeService.generateQrCodeImageById(anyInt())).thenReturn(mockQrCodeImage);

    // Actual
    var qrCodeImage = paymentService.generatePaymentQr(QR_CODE_REF_ID);

    // Assert
    assertEquals(mockQrCodeImage, qrCodeImage);
  }

  @Test
  void whenPayByBookingId_ThenSuccess() {
    // Arrange
    var TOUR_ID = 1;
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_ID);
    var bookingRef = AggregateReference.<Booking, Integer>to(BOOKING_ID);
    var mockBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
    var USER_WALLET_BALANCE_AFTER_TRANSFER = USER_WALLET_BALANCE.subtract(TOUR_PRICE);
    var COMPANY_WALLET_BALANCE_AFTER_TRANSFER = COMPANY_WALLET_BALANCE.add(TOUR_PRICE);
    var qrCodeContent = buildQrCodeContent(BOOKING_ID);
    var mockUserWallet = buildUserWallet(USER_WALLET_ID, USER_ID, USER_WALLET_BALANCE);
    var mockTourCompanyWallet =
        buildTourCompanyWallet(COMPANY_WALLET_ID, TOUR_COMPANY_ID, COMPANY_WALLET_BALANCE);
    var mockUserWalletAfterTransfer =
        UserWallet.of(USER_ID, userRef, Instant.now(), USER_WALLET_BALANCE_AFTER_TRANSFER);
    var mockTourCompanyWalletAfterTransfer =
        TourCompanyWallet.of(
            COMPANY_WALLET_ID,
            tourCompanyRef,
            Instant.now(),
            COMPANY_WALLET_BALANCE_AFTER_TRANSFER);
    var mockQrCodeRef =
        QrCodeReference.of(QR_CODE_REF_ID, BOOKING_ID, qrCodeContent, QrCodeStatus.EXPIRED);
    var mockTransaction =
        Transaction.of(
            1,
            userRef,
            tourCompanyRef,
            bookingRef,
            Instant.now(),
            TOUR_PRICE,
            TransactionType.BOOKING,
            IDEMPOTENT_KEY);
    var mockBookingToBeCompleted = buildCompletedBooking(BOOKING_ID, USER_ID, TOUR_ID);
    var expectedBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.COMPLETED, null);

    when(bookingService.findBookingById(anyInt())).thenReturn(Optional.of(mockBooking));
    when(walletService.getConsumerAndTourCompanyWallets(any(Booking.class)))
        .thenReturn(new Pair<>(mockUserWallet, mockTourCompanyWallet));
    when(walletService.transferMoney(
            any(UserWallet.class),
            any(TourCompanyWallet.class),
            any(BigDecimal.class),
            eq(TransactionType.BOOKING)))
        .thenReturn(new Pair<>(mockUserWalletAfterTransfer, mockTourCompanyWalletAfterTransfer));
    when(transactionService.createTransaction(any())).thenReturn(null);
    doNothing().when(tourCountService).incrementTourCount(anyInt());
    when(qrCodeService.updateQrCodeRefStatusByBookingId(anyInt(), any(QrCodeStatus.class)))
        .thenReturn(mockQrCodeRef);
    when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockTransaction);
    when(bookingRepository.save(any(Booking.class))).thenReturn(mockBookingToBeCompleted);

    // Actual
    var actualPaidBookingInfo = paymentService.payByBookingId(1, IDEMPOTENT_KEY);

    // Assert
    assertEquals(expectedBookingInfo, actualPaidBookingInfo);
  }

  @Test
  void whenPayByBookingId_ButBookingNotFound_ThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s id [%d] not found", Booking.class.getSimpleName(), BOOKING_ID);
    when(bookingService.findBookingById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> paymentService.payByBookingId(BOOKING_ID, IDEMPOTENT_KEY);

    // Assert
    var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenRefundBooking_ThenSuccess() {
    // Arrange
    var bookingRef = AggregateReference.<Booking, Integer>to(BOOKING_ID);
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
    var completedBookingInput = buildCompletedBooking(BOOKING_ID, USER_ID, TOUR_COMPANY_ID);
    var mockUserWallet = buildUserWallet(USER_WALLET_ID, USER_ID, USER_WALLET_BALANCE);
    var mockTourCompanyWallet =
        buildTourCompanyWallet(COMPANY_WALLET_ID, TOUR_COMPANY_ID, COMPANY_WALLET_BALANCE);
    var USER_WALLET_BALANCE_AFTER_TRANSFER = USER_WALLET_BALANCE.add(TOUR_PRICE);
    var COMPANY_WALLET_BALANCE_AFTER_TRANSFER = COMPANY_WALLET_BALANCE.subtract(TOUR_PRICE);
    var mockUserWalletAfterTransfer =
        UserWallet.of(USER_WALLET_ID, userRef, Instant.now(), USER_WALLET_BALANCE_AFTER_TRANSFER);
    var mockTourCompanyWalletAfterTransfer =
        TourCompanyWallet.of(
            COMPANY_WALLET_ID,
            tourCompanyRef,
            Instant.now(),
            COMPANY_WALLET_BALANCE_AFTER_TRANSFER);
    var mockTransaction =
        Transaction.of(
            1,
            userRef,
            tourCompanyRef,
            bookingRef,
            Instant.now(),
            TOUR_PRICE,
            TransactionType.REFUND,
            null);

    when(walletService.getConsumerAndTourCompanyWallets(any(Booking.class)))
        .thenReturn(new Pair<>(mockUserWallet, mockTourCompanyWallet));
    when(walletService.transferMoney(
            any(UserWallet.class),
            any(TourCompanyWallet.class),
            any(BigDecimal.class),
            eq(TransactionType.REFUND)))
        .thenReturn(new Pair<>(mockUserWalletAfterTransfer, mockTourCompanyWalletAfterTransfer));
    when(transactionService.createTransaction(any(Transaction.class))).thenReturn(mockTransaction);

    // Actual
    var actualResult = paymentService.refundBooking(completedBookingInput, IDEMPOTENT_KEY);

    // Assert
    assertTrue(actualResult);
  }
}
