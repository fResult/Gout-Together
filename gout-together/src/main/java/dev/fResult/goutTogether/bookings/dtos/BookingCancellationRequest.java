package dev.fResult.goutTogether.bookings.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingCancellationRequest(@NotNull @Min(1) Integer tourId) {
  public static BookingCancellationRequest of(Integer tourId) {
    return new BookingCancellationRequest(tourId);
  }
}
