package dev.fResult.goutTogether.qrcodes;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.helpers.QrCodeHelper;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {
  final int QR_CODE_REF_ID = 1;
  final int BOOKING_ID = 1;

  @InjectMocks private QrCodeService qrCodeService;

  @Mock private QrCodeReferenceRepository qrCodeReferenceRepository;

  private QrCodeReference buildQrCodeReference(int id, int bookingId, QrCodeStatus status) {
    var qrCodeContent = String.format("%s/%d", API_PAYMENT_PATH, bookingId);

    return QrCodeReference.of(id, bookingId, qrCodeContent, status);
  }

  @Test
  void whenGenerateQrCodeImage_thenSuccess() throws WriterException {
    // Arrange
    try (var mockedQrCodeHelper = mockStatic(QrCodeHelper.class)) {
      var mockQrCodeReference =
          buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.ACTIVATED);
      var expectedQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

      when(qrCodeReferenceRepository.findById(anyInt()))
          .thenReturn(Optional.of(mockQrCodeReference));
      mockedQrCodeHelper
          .when(() -> QrCodeHelper.generateQrCodeImage(anyString()))
          .thenReturn(expectedQrCodeImage);

      // Actual
      var actualGeneratedQrCodeImage = qrCodeService.generateQrCodeImageById(QR_CODE_REF_ID);

      // Assert
      assertEquals(expectedQrCodeImage, actualGeneratedQrCodeImage);
    }
  }

  @Test
  void whenGetQrCodeRefByBookingId_thenSuccess() {
    // Arrange
    var mockQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.EXPIRED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt()))
        .thenReturn(Optional.of(mockQrCodeReference));

    // Actual
    var actualQrCodeReference = qrCodeService.getQrCodeRefByBookingId(BOOKING_ID);

    // Assert
    assertEquals(mockQrCodeReference, actualQrCodeReference);
  }
}
