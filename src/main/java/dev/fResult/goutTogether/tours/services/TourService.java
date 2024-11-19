package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TourService {
    Tour createTour(TourRequest body);

    Tour getTourById(Integer id);

    Page<Tour> getTours(Pageable pageable);
}
