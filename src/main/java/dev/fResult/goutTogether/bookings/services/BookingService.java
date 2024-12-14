package dev.fResult.goutTogether.bookings.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingRequest;

public interface BookingService {
  BookingInfoResponse bookTour(String idempotentKey, BookingRequest body);

  BookingInfoResponse cancelTour(String idempotentKey, BookingRequest body);
}
