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
  @InjectMocks private QrCodeService qrCodeService;

  @Mock private QrCodeReferenceRepository qrCodeReferenceRepository;

  @Test
  void whenGenerateQrCodeImage_thenSuccess() throws WriterException {
    // Arrange
    try (var mockedQrCodeHelper = mockStatic(QrCodeHelper.class)) {
      final var QR_CODE_REF_ID = 1;
      final var BOOKING_ID = 1;
      var mockQrCodeReference =
          QrCodeReference.of(QR_CODE_REF_ID, BOOKING_ID, API_PAYMENT_PATH, QrCodeStatus.ACTIVATED);
      var expectedQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

      when(qrCodeReferenceRepository.findById(anyInt()))
          .thenReturn(Optional.of(mockQrCodeReference));
      mockedQrCodeHelper.when(() -> QrCodeHelper.generateQrCodeImage(anyString())).thenReturn(expectedQrCodeImage);

      // Actual
      var actualGeneratedQrCodeImage = qrCodeService.generateQrCodeImageById(QR_CODE_REF_ID);

      // Assert
      assertEquals(expectedQrCodeImage, actualGeneratedQrCodeImage);
    }
  }
}
