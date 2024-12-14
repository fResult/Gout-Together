package dev.fResult.goutTogether.bookings.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingRequest;

public class BookingServiceImpl implements BookingService {
  @Override
  public BookingInfoResponse bookTour(String idempotentKey, BookingRequest body) {
    throw new UnsupportedOperationException("Not Implement Yet.");
  }

  @Override
  public BookingInfoResponse cancelTour(String idempotentKey, BookingRequest body) {
    throw new UnsupportedOperationException("Not Implement Yet.");
  }
}
