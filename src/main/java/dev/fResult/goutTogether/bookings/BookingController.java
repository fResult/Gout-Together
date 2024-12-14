package dev.fResult.goutTogether.bookings;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingRequest;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.validator.constraints.UUID;
import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
  private final ErrorHelper errorHelper = new ErrorHelper(BookingController.class);

  private final BookingService bookingService;

  // Note: For Pessimistic Lock Testing Purpose
  private final TourCountRepository tourCountRepository;

  public BookingController(BookingService bookingService, TourCountRepository tourCountRepository) {
    this.bookingService = bookingService;
    this.tourCountRepository = tourCountRepository;
  }

  @PostMapping
  public ResponseEntity<BookingInfoResponse> bookTour(
      @UUID(message = "wrong format for headers `idempotent-key`") @RequestHeader("idempotent-key")
          String idempotentKey,
      @Validated @RequestBody BookingRequest body,
      Authentication authentication) {

    var bookedTour = bookingService.bookTour(idempotentKey, body);

    return ResponseEntity.ok(bookedTour);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<BookingInfoResponse> cancelTourById(
      @UUID(message = "wrong format for headers `idempotent-key`") @RequestHeader("idempotent-key")
          String idempotentKey,
      @NotNull @Min(1) Integer id,
      @Validated @RequestBody BookingRequest body,
      Authentication authentication) {

    var cancelledTour = bookingService.cancelTour(idempotentKey, body);

    return ResponseEntity.ok(cancelledTour);
  }

  // Note: For Pessimistic Lock Testing Purpose
  @PostMapping("/by-booking")
  public ResponseEntity<?> updateTourCountByBookingId(@RequestBody Payload body) {
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), 5));
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), -3));

    return ResponseEntity.noContent().build();
  }

  // Note: For Pessimistic Lock Testing Purpose
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