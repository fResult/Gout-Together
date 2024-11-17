package dev.fResult.goutTogether.tours.services;

import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import org.springframework.stereotype.Service;

@Service
public class TourCountService {
    private final TourCountRepository tourCountRepository;

    public TourCountService(TourCountRepository tourCountRepository) {
        this.tourCountRepository = tourCountRepository;
    }

    public TourCount createTourCount(TourCount tourCount) {
        return tourCountRepository.save(tourCount);
    }

    public void incrementTourCount() {
        // Increment the tour count
    }
}
