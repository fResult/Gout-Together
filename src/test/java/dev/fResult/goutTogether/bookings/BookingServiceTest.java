package dev.fResult.goutTogether.bookings;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.bookings.dtos.BookingCancellationRequest;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingServiceImpl;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.exceptions.BookingExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.payments.services.PaymentService;
import dev.fResult.goutTogether.qrcodes.QrCodeReference;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.services.TourCountService;
import dev.fResult.goutTogether.users.entities.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
  final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
  final int BOOKING_ID = 1;
  final int USER_ID = 1;
  final int TOUR_ID = 1;
  final Instant BOOKED_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);

  @InjectMocks private BookingServiceImpl bookingService;

  @Mock private BookingRepository bookingRepository;
  @Mock private QrCodeService qrCodeService;
  @Mock private TourCountService tourCountService;
  @Mock private PaymentService paymentService;

  private Booking buildPendingBooking(int bookingId, int userId, int tourId) {
    return Booking.of(
        bookingId,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.PENDING.name(),
        BOOKED_TIME,
        BOOKED_TIME,
        IDEMPOTENT_KEY);
  }

  private Booking buildCompletedBooking(int bookingId, int userId, int tourId) {
    return Booking.of(
        bookingId,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.COMPLETED.name(),
        BOOKED_TIME,
        BOOKED_TIME.plus(13, ChronoUnit.MINUTES),
        IDEMPOTENT_KEY);
  }

  private Authentication buildAuthentication(int userId) {
    final var jwt =
        Jwt.withTokenValue("token").header("alg", "none").claim(RESOURCE_ID_CLAIM, userId).build();

    return new JwtAuthenticationToken(jwt);
  }

  private QrCodeReference buildActivatedQrCodeRef(int id, int bookingId) {
    return QrCodeReference.of(
        id, bookingId, String.format("%s/%d", API_PAYMENT_PATH, bookingId), QrCodeStatus.ACTIVATED);
  }

  private QrCodeReference buildExpiredQrCodeRef(int id, int bookingId) {
    return QrCodeReference.of(
        id, bookingId, String.format("%s/%d", API_PAYMENT_PATH, bookingId), QrCodeStatus.EXPIRED);
  }

  @Test
  void whenFindBookingById_thenSuccess() {
    // Arrange
    final var booking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

    // Actual
    final var actualBooking = bookingService.findBookingById(BOOKING_ID);

    // Assert
    assertTrue(actualBooking.isPresent());
    assertEquals(booking, actualBooking.get());
  }

  @Test
  void whenFindBookingById_ButNotFound_thenReturnOptionalEmpty() {
    // Arrange
    final var NOT_FOUND_BOOKING_ID = 99999;
    final var expectedBooking = Optional.<Booking>empty();

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    final var actualBooking = bookingService.findBookingById(NOT_FOUND_BOOKING_ID);

    // Assert
    assertEquals(expectedBooking, actualBooking);
  }

  @Nested
  class BookTourTest {
    @Test
    void thenSuccess() {
      // Arrange
      final var authentication = buildAuthentication(USER_ID);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      final var createdBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var mockQrCodeRef = buildActivatedQrCodeRef(1, BOOKING_ID);
      final var expectedBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.PENDING, 1);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.empty());
      when(bookingRepository.save(any(Booking.class))).thenReturn(createdBooking);
      when(qrCodeService.createQrCodeRefForBooking(anyInt())).thenReturn(mockQrCodeRef);

      // Actual
      final var actualBooking = bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedBookingInfo, actualBooking);
    }

    @Test
    void butBookingExists_thenReturnExistingBooking() {
      // Arrange
      final var authentication = buildAuthentication(USER_ID);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      final var mockQrCodeRef = buildActivatedQrCodeRef(1, BOOKING_ID);
      final var exitingBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var expectedExistingBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.PENDING, 1);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.of(exitingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt())).thenReturn(mockQrCodeRef);

      // Actual
      final var actualBooking = bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedExistingBookingInfo, actualBooking);
    }

    @Test
    void butBookingAlreadyCompleted_ThenThrowException() {
      // Arrange
      final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
      final var authentication = buildAuthentication(USER_ID);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      final var exitingBooking = buildCompletedBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var expectedErrorMessage =
          String.format("UserId [%d] already booked tourId [%d]", USER_ID, TOUR_ID);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.of(exitingBooking));

      // Actual
      final Executable actualExecution =
          () -> bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      final var exception = assertThrowsExactly(BookingExistsException.class, actualExecution);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butBookingExistedAndQrCodeRefNotFound_ThenThrowException() {
      // Arrange
      final var NOT_FOUND_BOOKING_ID = 99999;
      final var authentication = buildAuthentication(USER_ID);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID);
      final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      final var exitingBooking = buildPendingBooking(NOT_FOUND_BOOKING_ID, USER_ID, TOUR_ID);
      final var expectedErrorMessage =
          String.format(
              "%s with bookingId [%d] not found",
              QrCodeReference.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.of(exitingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt()))
          .thenThrow(new EntityNotFoundException(expectedErrorMessage));

      // Actual
      final Executable actualExecution =
          () -> bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecution);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class CancelTourTest {
    @Test
    void thenSuccessWithDecrementTourCount() {
      // Arrange
      final var body = BookingCancellationRequest.of(TOUR_ID);
      final var authentication = buildAuthentication(USER_ID);
      final var existingBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var mockExpiredQrCodeRef = buildExpiredQrCodeRef(1, BOOKING_ID);
      final var expectedRefundedBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.CANCELLED, null);

      when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(existingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt())).thenReturn(mockExpiredQrCodeRef);

      // Actual
      final var actualRefundedBookingInfo =
          bookingService.cancelTour(authentication, BOOKING_ID, body, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedRefundedBookingInfo, actualRefundedBookingInfo);
      verify(tourCountService, times(1)).decrementTourCount(TOUR_ID);
      verify(paymentService, times(1)).refundBooking(any(Booking.class), anyString());
    }

    @Test
    void butQrCodeRefAlreadyIsNotExpiredYet_ThenSuccessWithoutDecrementTourCount() {
      // Arrange
      final var body = BookingCancellationRequest.of(TOUR_ID);
      final var authentication = buildAuthentication(USER_ID);
      final var existingBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var mockActivatedQrCodeRef = buildActivatedQrCodeRef(1, BOOKING_ID);
      final var expectedRefundedBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.CANCELLED, null);

      when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(existingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt())).thenReturn(mockActivatedQrCodeRef);

      // Actual
      final var actualRefundedBookingInfo =
          bookingService.cancelTour(authentication, BOOKING_ID, body, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedRefundedBookingInfo, actualRefundedBookingInfo);
      verify(tourCountService, never()).decrementTourCount(anyInt());
      verify(paymentService, never()).refundBooking(any(Booking.class), anyString());
    }

    @Test
    void butBookingNotFound_ThenThrowException() {
      // Arrange
      final var NOT_FOUND_BOOKING_ID = 99999;
      final var body = BookingCancellationRequest.of(TOUR_ID);
      final var authentication = buildAuthentication(USER_ID);
      final var expectedErrorMessage =
          String.format(
              "%s id [%d] not found", Booking.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

      when(bookingRepository.findById(anyInt())).thenReturn(Optional.empty());

      // Actual
      final Executable actualExecution =
          () ->
              bookingService.cancelTour(authentication, NOT_FOUND_BOOKING_ID, body, IDEMPOTENT_KEY);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecution);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void butQrCodeRefNotFound_ThenThrowException() {
      // Arrange
      final var body = BookingCancellationRequest.of(TOUR_ID);
      final var authentication = buildAuthentication(USER_ID);
      final var existingBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      final var expectedErrorMessage =
          String.format(
              "%s with bookingId [%d] not found",
              QrCodeReference.class.getSimpleName(), BOOKING_ID);

      when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(existingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt()))
          .thenThrow(new EntityNotFoundException(expectedErrorMessage));

      // Actual
      final Executable actualExecution =
          () -> bookingService.cancelTour(authentication, BOOKING_ID, body, IDEMPOTENT_KEY);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecution);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }
}
