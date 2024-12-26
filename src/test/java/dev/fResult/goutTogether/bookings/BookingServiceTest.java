package dev.fResult.goutTogether.bookings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingServiceImpl;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.payments.services.PaymentService;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import dev.fResult.goutTogether.tours.services.TourCountService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
  @InjectMocks private BookingServiceImpl bookingService;

  @Mock private BookingRepository bookingRepository;
  @Mock private QrCodeService qrCodeService;
  @Mock private TourCountService tourCountService;
  @Mock private PaymentService paymentService;

  @Test
  void whenFindBookingById_thenSuccess() {
    // Arrange
    final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    final var BOOKING_ID = 1;
    final var USER_ID = 1;
    final var TOUR_ID = 1;
    final var BOOKED_TIME = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    var booking =
        Booking.of(
            BOOKING_ID,
            AggregateReference.to(USER_ID),
            AggregateReference.to(TOUR_ID),
            BookingStatus.PENDING.name(),
            BOOKED_TIME,
            BOOKED_TIME,
            IDEMPOTENT_KEY);

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
}
