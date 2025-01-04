package dev.fResult.goutTogether.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QrCodeHelperTest {
  @Test
  void whenGenerateQrCodeImageIsCalled_thenNoExceptionIsThrown() {
    // Arrange
    final var barcodeText = "Hello, World!";

    // Actual
    try {
      QrCodeHelper.generateQrCodeImage(barcodeText);
    } catch (Exception ex) {
      // Assert
      throw new AssertionError("An exception was thrown when it shouldn't have been.", ex);
    }
  }

  @Test
  void whenGenerateQrCode_ThenSuccess() throws WriterException {
    try (final var mockedMatrixToImageWriter = mockStatic(MatrixToImageWriter.class)) {
      // Arrange
      final var barcodeTextInput = "Hello, World!";
      final var expectedQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

      mockedMatrixToImageWriter
          .when(() -> MatrixToImageWriter.toBufferedImage(any(BitMatrix.class)))
          .thenReturn(expectedQrCodeImage);

      // Actual
      final var actualQrCodeImage = QrCodeHelper.generateQrCodeImage(barcodeTextInput);

      // Assert
      assertEquals(expectedQrCodeImage, actualQrCodeImage);
    }
  }
}
