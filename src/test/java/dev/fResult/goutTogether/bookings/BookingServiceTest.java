package dev.fResult.goutTogether.bookings;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;
import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingServiceImpl;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
  @InjectMocks private BookingServiceImpl bookingService;

  @Mock private BookingRepository bookingRepository;
  @Mock private QrCodeService qrCodeService;
  @Mock private TourCountService tourCountService;
  @Mock private PaymentService paymentService;

  private Booking buildPendingBooking(int bookingId, int userId, int tourId) {
    final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    final var BOOKED_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    return Booking.of(
        bookingId,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.PENDING.name(),
        BOOKED_TIME,
        BOOKED_TIME,
        IDEMPOTENT_KEY);
  }

  @Test
  void whenFindBookingById_thenSuccess() {
    // Arrange
    final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    final var BOOKING_ID = 1;
    final var USER_ID = 1;
    final var TOUR_ID = 1;
    final var BOOKED_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    var booking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(booking));

    // Actual
    var actualBooking = bookingService.findBookingById(BOOKING_ID);

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
    var actualBooking = bookingService.findBookingById(NOT_FOUND_BOOKING_ID);

    // Assert
    assertEquals(expectedBooking, actualBooking);
  }

  private Authentication buildAuthentication(int userId) {
    var jwt =
        Jwt.withTokenValue("token").header("alg", "none").claim(RESOURCE_ID_CLAIM, userId).build();

    return new JwtAuthenticationToken(jwt);
  }

  @Nested
  class BookTourTest {
    @Test
    void thenSuccess() {
      // Arrange
      final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
      final var BOOKING_ID = 1;
      final var USER_ID = 1;
      final var TOUR_ID = 1;
      var authentication = buildAuthentication(USER_ID);
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      var createdBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      var mockQrCodeRef =
          QrCodeReference.of(1, BOOKING_ID, API_PAYMENT_PATH, QrCodeStatus.ACTIVATED);
      var expectedBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.PENDING, 1);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.empty());
      when(bookingRepository.save(any(Booking.class))).thenReturn(createdBooking);
      when(qrCodeService.createQrCodeRefForBooking(anyInt())).thenReturn(mockQrCodeRef);

      // Actual
      var actualBooking = bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedBookingInfo, actualBooking);
    }

    @Test
    void butBookingExists_thenReturnExistingBooking() {
      // Arrange
      final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
      final var BOOKING_ID = 1;
      final var USER_ID = 1;
      final var TOUR_ID = 1;
      final var BOOKED_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);
      var authentication = buildAuthentication(USER_ID);
      var userRef = AggregateReference.<User, Integer>to(USER_ID);
      var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
      var mockQrCodeRef =
          QrCodeReference.of(1, BOOKING_ID, API_PAYMENT_PATH, QrCodeStatus.ACTIVATED);
      var exitingBooking = buildPendingBooking(BOOKING_ID, USER_ID, TOUR_ID);
      var expectedExistingBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.PENDING, 1);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.of(exitingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt())).thenReturn(mockQrCodeRef);

      // Actual
      var actualBooking = bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedExistingBookingInfo, actualBooking);
    }
      var exitingBooking =
          Booking.of(
              BOOKING_ID,
              userRef,
              tourRef,
              BookingStatus.PENDING.name(),
              BOOKED_TIME,
              BOOKED_TIME,
              IDEMPOTENT_KEY);
      var expectedExistingBookingInfo =
          BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.PENDING, 1);

      when(bookingRepository.findOneByUserIdAndTourId(userRef, tourRef))
          .thenReturn(Optional.of(exitingBooking));
      when(qrCodeService.getQrCodeRefByBookingId(anyInt())).thenReturn(mockQrCodeRef);

      // Actual
      var actualBooking = bookingService.bookTour(authentication, TOUR_ID, IDEMPOTENT_KEY);

      // Assert
      assertEquals(expectedExistingBookingInfo, actualBooking);
    }
  }
}
