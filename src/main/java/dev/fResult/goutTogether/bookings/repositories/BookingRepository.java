package dev.fResult.goutTogether.bookings.repositories;

import dev.fResult.goutTogether.bookings.entities.Booking;
import org.springframework.data.repository.ListCrudRepository;

public interface BookingRepository extends ListCrudRepository<Booking, Integer> {}
