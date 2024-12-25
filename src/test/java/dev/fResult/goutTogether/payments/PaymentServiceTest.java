package dev.fResult.goutTogether.payments;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
  @Value("${booking.tour-price}")
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

  private String buildQrCodeContent(int bookingId) {
    return String.format("%s/%d", API_PAYMENT_PATH, bookingId);
  }

  @Test
  void whenGenerateQrCodeThenSuccess() throws WriterException {
    // Arrange
    var mockQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

    when(qrCodeService.generateQrCodeImageById(anyInt())).thenReturn(mockQrCodeImage);

    // Actual
    var qrCodeImage = paymentService.generatePaymentQr(QR_CODE_REF_ID);

    // Assert
    assertEquals(mockQrCodeImage, qrCodeImage);
  }

  @Test
  void whenPayByBookingIdThenSuccess() {
    // Arrange
    var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    var BOOKING_ID = 1;
    var USER_ID = 1;
    var TOUR_ID = 1;
    var userRef = AggregateReference.<User, Integer>to(USER_ID);
    var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_ID);
    var bookingRef = AggregateReference.<Booking, Integer>to(BOOKING_ID);
    var BOOKING_TIME = Instant.now().minus(10, ChronoUnit.MINUTES);

    var mockBooking =
        Booking.of(
            BOOKING_ID,
            userRef,
            tourRef,
            BookingStatus.PENDING.name(),
            BOOKING_TIME,
            BOOKING_TIME,
            IDEMPOTENT_KEY);
    var USER_WALLET_BALANCE = BigDecimal.valueOf(100);
    var COMPANY_WALLET_BALANCE = BigDecimal.valueOf(100);
    var USER_WALLET_BALANCE_AFTER_TRANSFER = USER_WALLET_BALANCE.subtract(TOUR_PRICE);
    var COMPANY_WALLET_BALANCE_AFTER_TRANSFER = COMPANY_WALLET_BALANCE.add(TOUR_PRICE);
    var qrCodeContent = buildQrCodeContent(BOOKING_ID);
    var mockUserWallet = new UserWallet(USER_ID, userRef, BOOKING_TIME, USER_WALLET_BALANCE);
    var mockTourCompanyWallet =
        new TourCompanyWallet(1, tourCompanyRef, BOOKING_TIME, COMPANY_WALLET_BALANCE);
    var mockWalletPair = new Pair<>(mockUserWallet, mockTourCompanyWallet);
    var mockUserWalletAfterTransfer =
        new UserWallet(USER_ID, userRef, BOOKING_TIME, USER_WALLET_BALANCE_AFTER_TRANSFER);
    var mockTourCompanyWalletAfterTransfer =
        new TourCompanyWallet(
            1, tourCompanyRef, BOOKING_TIME, COMPANY_WALLET_BALANCE_AFTER_TRANSFER);
    var mockWalletPairAfterTransfer =
        new Pair<>(mockUserWalletAfterTransfer, mockTourCompanyWalletAfterTransfer);
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
    var mockBookingToBeCompleted =
        Booking.of(
            BOOKING_ID,
            userRef,
            tourRef,
            BookingStatus.COMPLETED.name(),
            BOOKING_TIME,
            Instant.now(),
            IDEMPOTENT_KEY);

    var expectedBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.COMPLETED, null);

    when(bookingService.findBookingById(anyInt())).thenReturn(Optional.of(mockBooking));
    when(walletService.getConsumerAndTourCompanyWallets(any(Booking.class)))
        .thenReturn(mockWalletPair);
    when(walletService.transferMoney(
            any(UserWallet.class), any(TourCompanyWallet.class), any(), any()))
        .thenReturn(mockWalletPairAfterTransfer);
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
}
