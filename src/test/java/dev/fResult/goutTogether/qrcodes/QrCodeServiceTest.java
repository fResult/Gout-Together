package dev.fResult.goutTogether.qrcodes;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.helpers.QrCodeHelper;
import java.awt.image.BufferedImage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QrCodeServiceTest {
  final int QR_CODE_REF_ID = 1;
  final int BOOKING_ID = 1;
  final int NOT_FOUND_BOOKING_ID = 99999;

  @InjectMocks private QrCodeService qrCodeService;

  @Mock private QrCodeReferenceRepository qrCodeReferenceRepository;

  private QrCodeReference buildQrCodeReference(int id, int bookingId, QrCodeStatus status) {
    final var qrCodeContent = String.format("%s/%d", API_PAYMENT_PATH, bookingId);

    return QrCodeReference.of(id, bookingId, qrCodeContent, status);
  }

  @Test
  void whenGenerateQrCodeImage_thenSuccess() throws WriterException {
    // Arrange
    try (final var mockedQrCodeHelper = mockStatic(QrCodeHelper.class)) {
      final var mockQrCodeReference =
          buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.ACTIVATED);
      final var expectedQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

      when(qrCodeReferenceRepository.findById(anyInt()))
          .thenReturn(Optional.of(mockQrCodeReference));
      mockedQrCodeHelper
          .when(() -> QrCodeHelper.generateQrCodeImage(anyString()))
          .thenReturn(expectedQrCodeImage);

      // Actual
      final var actualGeneratedQrCodeImage = qrCodeService.generateQrCodeImageById(QR_CODE_REF_ID);

      // Assert
      assertEquals(expectedQrCodeImage, actualGeneratedQrCodeImage);
    }
  }

  @Test
  void whenGetQrCodeRefByBookingId_thenSuccess() {
    // Arrange
    final var mockQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.EXPIRED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt()))
        .thenReturn(Optional.of(mockQrCodeReference));

    // Actual
    final var actualQrCodeReference = qrCodeService.getQrCodeRefByBookingId(BOOKING_ID);

    // Assert
    assertEquals(mockQrCodeReference, actualQrCodeReference);
  }

  @Test
  void whenGetQrCodeRefByBookingId_ButNotFound_thenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format(
            "%s with bookingId [%d] not found",
            QrCodeReference.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> qrCodeService.getQrCodeRefByBookingId(NOT_FOUND_BOOKING_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenCreateQrCodeForBooking_ThenSuccess() {
    // Arrange
    final var mockCreatedQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.ACTIVATED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt())).thenReturn(Optional.empty());
    when(qrCodeReferenceRepository.save(any(QrCodeReference.class)))
        .thenReturn(mockCreatedQrCodeReference);

    // Actual
    final var actualQrCodeReference = qrCodeService.createQrCodeRefForBooking(BOOKING_ID);

    // Assert
    assertEquals(mockCreatedQrCodeReference, actualQrCodeReference);
  }

  @Test
  void whenCreateQrCodeForBooking_ButAlreadyExists_ThenReturnExisting() {
    // Arrange
    final var mockExistingQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.EXPIRED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt()))
        .thenReturn(Optional.of(mockExistingQrCodeReference));

    // Actual
    final var actualQrCodeReference = qrCodeService.createQrCodeRefForBooking(BOOKING_ID);

    // Assert
    assertEquals(mockExistingQrCodeReference, actualQrCodeReference);
    verify(qrCodeReferenceRepository, never()).save(any(QrCodeReference.class));
  }

  @Test
  void whenUpdateQrCodeRefByBookingId_ThenSuccess() {
    // Arrange
    final var mockExistingQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.ACTIVATED);
    final var mockUpdatedQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.EXPIRED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt()))
        .thenReturn(Optional.of(mockExistingQrCodeReference));
    when(qrCodeReferenceRepository.save(any(QrCodeReference.class)))
        .thenReturn(mockUpdatedQrCodeReference);

    // Actual
    final var actualUpdatedQrCodeReference =
        qrCodeService.updateQrCodeRefStatusByBookingId(BOOKING_ID, QrCodeStatus.EXPIRED);

    // Assert
    assertEquals(mockUpdatedQrCodeReference, actualUpdatedQrCodeReference);
  }

  @Test
  void whenUpdateQrCodeRefByBookingId_ButNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format(
            "%s with bookingId [%d] not found",
            QrCodeReference.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () ->
            qrCodeService.updateQrCodeRefStatusByBookingId(
                NOT_FOUND_BOOKING_ID, QrCodeStatus.EXPIRED);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
    verify(qrCodeReferenceRepository, never()).save(any(QrCodeReference.class));
  }

  @Test
  void whenDeleteQrCodeRefByBookingId_ThenSuccess() {
    // Arrange
    final var mockExistingQrCodeReference =
        buildQrCodeReference(QR_CODE_REF_ID, BOOKING_ID, QrCodeStatus.ACTIVATED);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt()))
        .thenReturn(Optional.of(mockExistingQrCodeReference));
    doNothing().when(qrCodeReferenceRepository).delete(any(QrCodeReference.class));

    // Actual
    final var actualIsDeleted = qrCodeService.deleteQrCodeRefByBookingId(BOOKING_ID);

    // Assert
    assertTrue(actualIsDeleted);
  }

  @Test
  void whenDeleteQrCodeRefByBookingId_ButNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format(
            "%s with bookingId [%d] not found",
            QrCodeReference.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

    when(qrCodeReferenceRepository.findOneByBookingId(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> qrCodeService.deleteQrCodeRefByBookingId(NOT_FOUND_BOOKING_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
    verify(qrCodeReferenceRepository, never()).delete(any(QrCodeReference.class));
  }
}
