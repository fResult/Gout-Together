package dev.fResult.goutTogether.tours.repositories;

import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.CrudRepository;

public interface TourCountRepository extends CrudRepository<TourCount, Integer> {

  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<TourCount> findOneByTourId(AggregateReference<Tour, Integer> tourId);
}
