package dev.fResult.goutTogether.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.payments.services.PaymentServiceImpl;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.transactions.TransactionService;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
  @Value("${booking.tour-price}")
  private double tourPrice;

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

  @Test
  void whenGenerateQrCodeThenSuccess() throws WriterException {
    // Arrange
    var QR_CODE_REF_ID = 1;
    var mockQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

    when(qrCodeService.generateQrCodeImageById(anyInt())).thenReturn(mockQrCodeImage);

    // Actual
    var qrCodeImage = paymentService.generatePaymentQr(QR_CODE_REF_ID);

    // Assert
    assertEquals(mockQrCodeImage, qrCodeImage);
  }
}
