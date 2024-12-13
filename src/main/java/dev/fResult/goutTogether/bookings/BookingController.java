package dev.fResult.goutTogether.bookings;

import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import java.time.Instant;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
  private final ErrorHelper errorHelper = new ErrorHelper(BookingController.class);
  private final TourCountRepository tourCountRepository;

  public BookingController(TourCountRepository tourCountRepository) {
    this.tourCountRepository = tourCountRepository;
  }

  @PostMapping("/by-booking")
  public ResponseEntity<?> updateTourCountByBookingId(@RequestBody Payload body) {
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), 5));
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), -3));

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/by-tour")
  public ResponseEntity<?> updateTourCountByTourId(@RequestBody Payload body) {
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountByTourId(body.tourId(), 5));
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountByTourId(body.tourId(), -3));

    return ResponseEntity.noContent().build();
  }

  @Service
  public class SimpleService {
    // Simulate Race Condition
    @Transactional
    public void updateTourCountById(Integer bookingId, int value) {
      var tourCount =
          tourCountRepository
              .findById(bookingId)
              .orElseThrow(
                  errorHelper.entityNotFound("updateTourCountById", TourCount.class, bookingId));

      var amountToUpdate = tourCount.amount() + value;
      var tourCountToUpdate = TourCount.of(tourCount.id(), tourCount.tourId(), amountToUpdate);

      var updatedTourCount = tourCountRepository.save(tourCountToUpdate);
      System.out.println("[updateTourCountById] TourCount is Updated: " + updatedTourCount);
    }

    // With pessimistic lock
    @Transactional
    public void updateTourCountByTourId(Integer tourId, int value) {
      var tourRef = AggregateReference.<Tour, Integer>to(tourId);
      var tourCount =
          tourCountRepository
              .findOneByTourId(tourRef)
              .orElseThrow(
                  errorHelper.entityWithSubResourceNotFound(
                      "updateTourCountByTourId",
                      TourCount.class,
                      "tourId",
                      String.valueOf(tourId)));

      var amountToUpdate = tourCount.amount() + value;
      var tourCountToUpdate = TourCount.of(tourCount.id(), tourCount.tourId(), amountToUpdate);

      var updatedTourCount = tourCountRepository.save(tourCountToUpdate);
      System.out.println("[updateTourCountByTourId] TourCount is Updated: " + updatedTourCount);
    }
  }

  public static record Payload(Instant time, Integer bookingId, Integer tourId) {}
}
