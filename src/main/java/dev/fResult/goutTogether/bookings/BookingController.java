package dev.fResult.goutTogether.bookings;

import dev.fResult.goutTogether.bookings.dtos.BookingCancellationRequest;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.common.constraints.UUID;
import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Instant;
import org.jobrunr.scheduling.BackgroundJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@Validated
public class BookingController {
  private final Logger logger = LoggerFactory.getLogger(BookingController.class);
  private final ErrorHelper errorHelper = new ErrorHelper(BookingController.class);

  private final BookingService bookingService;

  // Note: For Pessimistic Lock Testing Purpose
  private final TourCountRepository tourCountRepository;
  private final BookingRepository bookingRepository;

  public BookingController(
      BookingService bookingService,
      TourCountRepository tourCountRepository,
      BookingRepository bookingRepository) {

    this.bookingService = bookingService;
    this.tourCountRepository = tourCountRepository;
    this.bookingRepository = bookingRepository;
  }

  @PostMapping("/tours/{tourId}")
  public ResponseEntity<BookingInfoResponse> bookTour(
      @UUID(message = "wrong format for headers `idempotent-key`") @RequestHeader("idempotent-key")
          String idempotentKey,
      @PathVariable @Min(1) Integer tourId,
      Authentication authentication) {

    logger.debug("[bookTour] Booking tour with tourId [{}]", tourId);

    final var createdTourBooking = bookingService.bookTour(authentication, tourId, idempotentKey);
    final var createdUri = URI.create("/api/v1/bookings/" + createdTourBooking.id());

    return ResponseEntity.created(createdUri).body(createdTourBooking);
  }

  @PutMapping("/{id}/cancel")
  public ResponseEntity<BookingInfoResponse> cancelTourById(
      @UUID(message = "wrong format for headers `idempotent-key`") @RequestHeader("idempotent-key")
          String idempotentKey,
      @PathVariable @NotNull @Min(1) Integer id,
      @Validated @RequestBody BookingCancellationRequest body,
      Authentication authentication) {

    logger.debug("[cancelTourById] Canceling tour booking with tourId [{}]", body.tourId());
    final var cancelledTour = bookingService.cancelTour(authentication, id, body, idempotentKey);

    return ResponseEntity.ok(cancelledTour);
  }

  /*
   * Note: For Pessimistic Lock Testing Purpose
   */
  @PostMapping("/by-booking")
  public ResponseEntity<?> updateTourCountByBookingId(@RequestBody Payload body) {
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), 5));
    BackgroundJob.<SimpleService>schedule(
        body.time, x -> x.updateTourCountById(body.bookingId(), -3));

    return ResponseEntity.noContent().build();
  }

  /*
   * Note: For Pessimistic Lock Testing Purpose
   */
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
      final var booking =
          bookingRepository
              .findById(bookingId)
              .orElseThrow(
                  errorHelper.entityNotFound("updateTourCountById", Booking.class, bookingId));
      final var tourCount =
          tourCountRepository
              .findOneByTourId(booking.tourId())
              .orElseThrow(
                  errorHelper.entityWithSubResourceNotFound(
                      "updateTourCountById",
                      TourCount.class,
                      "tourId",
                      String.valueOf(booking.tourId().getId())));

      final var tourCountToUpdate = tourCount.increaseAmount(value);

      final var updatedTourCount = tourCountRepository.save(tourCountToUpdate);
      System.out.println("[updateTourCountById] TourCount is Updated: " + updatedTourCount);
    }

    // With pessimistic lock
    @Transactional
    public void updateTourCountByTourId(Integer tourId, int value) {
      final var tourRef = AggregateReference.<Tour, Integer>to(tourId);
      final var tourCount =
          tourCountRepository
              .findOneByTourId(tourRef)
              .orElseThrow(
                  errorHelper.entityWithSubResourceNotFound(
                      "updateTourCountByTourId",
                      TourCount.class,
                      "tourId",
                      String.valueOf(tourId)));

      final var tourCountToUpdate = tourCount.increaseAmount(value);

      final var updatedTourCount = tourCountRepository.save(tourCountToUpdate);
      System.out.println("[updateTourCountByTourId] TourCount is Updated: " + updatedTourCount);
    }
  }

  public record Payload(Instant time, Integer bookingId, Integer tourId) {}
}
