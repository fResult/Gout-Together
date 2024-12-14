package dev.fResult.goutTogether.bookings.entities;

import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.users.entities.User;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("bookings")
public record Booking(
    @Id Integer id,
    AggregateReference<User, Integer> userId,
    AggregateReference<Tour, Integer> tourId,
    BookingStatus status,
    Instant bookingDate,
    Instant lastUpdated,
    String idempotentKey) {

  public static Booking of(
      Integer id,
      AggregateReference<User, Integer> userId,
      AggregateReference<Tour, Integer> tourId,
      BookingStatus status,
      Instant bookingDate,
      Instant lastUpdated,
      String idempotentKey) {

    return new Booking(id, userId, tourId, status, bookingDate, lastUpdated, idempotentKey);
  }
}
