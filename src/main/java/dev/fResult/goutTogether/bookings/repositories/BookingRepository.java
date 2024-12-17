package dev.fResult.goutTogether.bookings.repositories;

import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.users.entities.User;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.ListCrudRepository;

public interface BookingRepository extends ListCrudRepository<Booking, Integer> {
  Optional<Booking> findOneByUserIdAndTourId(
      AggregateReference<User, String> userId, AggregateReference<Tour, Integer> tourId);
}
