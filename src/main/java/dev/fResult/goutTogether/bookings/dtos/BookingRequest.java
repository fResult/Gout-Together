package dev.fResult.goutTogether.bookings.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull @Min(1) Integer userId, @NotNull @Min(1) Integer tourId) {
  public static BookingRequest of(Integer userId, Integer tourId) {
    return new BookingRequest(userId, tourId);
  }
}
