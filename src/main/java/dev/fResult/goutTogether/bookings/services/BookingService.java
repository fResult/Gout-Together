package dev.fResult.goutTogether.bookings.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingRequest;
import dev.fResult.goutTogether.bookings.entities.Booking;
import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface BookingService {
  Optional<Booking> findBookingById(int id);

  BookingInfoResponse bookTour(Authentication authentication, int tourId, String idempotentKey);

  BookingInfoResponse cancelTour(String idempotentKey, BookingRequest body);
}
