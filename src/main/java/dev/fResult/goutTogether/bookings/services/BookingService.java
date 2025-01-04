package dev.fResult.goutTogether.bookings.services;

import dev.fResult.goutTogether.bookings.dtos.BookingCancellationRequest;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import java.util.Optional;
import org.springframework.security.core.Authentication;

public interface BookingService {
  Optional<Booking> findBookingById(int id);

  BookingInfoResponse bookTour(Authentication authentication, int tourId, String idempotentKey);

  BookingInfoResponse cancelTour(
      Authentication authentication, int id, BookingCancellationRequest body, String idempotentKey);
}
