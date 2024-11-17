package dev.fResult.goutTogether.tours.repositories;

import dev.fResult.goutTogether.tours.entities.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TourRepository extends CrudRepository<Tour, Integer> {
    Page<Tour> findAll(Pageable pageable);
}
