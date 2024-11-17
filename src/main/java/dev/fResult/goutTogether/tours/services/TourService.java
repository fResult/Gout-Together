package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.tours.dtos.TourRequest;
import dev.fResult.goutTogether.tours.entities.Tour;
import org.springframework.data.domain.Page;

public interface TourService {
    Tour createTour(TourRequest body);

    Tour getTourById(Integer id);

    Page<Tour> getTours(Integer page, Integer size);
}
