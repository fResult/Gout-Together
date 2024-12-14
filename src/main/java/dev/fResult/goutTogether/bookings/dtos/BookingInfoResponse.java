package dev.fResult.goutTogether.bookings.dtos;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;

public record BookingInfoResponse(
    Integer id, Integer userId, Integer tourId, BookingStatus status, Integer qrReference) {

  public static BookingInfoResponse of(
      Integer id, Integer userId, Integer tourId, BookingStatus status, Integer qrReference) {

    return new BookingInfoResponse(id, userId, tourId, status, qrReference);
  }

  public BookingInfoResponse fromDao(Booking booking) {
    return BookingInfoResponse.of(booking.id(), booking.userId().getId(), booking.tourId().getId(), booking.status(), null);
  }
}
