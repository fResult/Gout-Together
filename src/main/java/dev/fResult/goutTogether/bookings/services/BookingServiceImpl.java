package dev.fResult.goutTogether.bookings.services;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingCancellationRequest;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.exceptions.BookingExistsException;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService {
  private final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

  private final BookingRepository bookingRepository;
  private final QrCodeService qrCodeService;

  public BookingServiceImpl(BookingRepository bookingRepository, QrCodeService qrCodeService) {
    this.bookingRepository = bookingRepository;
    this.qrCodeService = qrCodeService;
  }

  @Override
  public Optional<Booking> findBookingById(int id) {
    logger.debug("[BookingServiceImpl] Finding {} id [{}]", Booking.class.getSimpleName(), id);

    return bookingRepository.findById(id);
  }

  @Override
  @Transactional
  public BookingInfoResponse bookTour(
      Authentication authentication, int tourId, String idempotentKey) {
    logger.debug("[bookTour] New {} is creating", Booking.class.getSimpleName());

    var jwt = (Jwt) authentication.getPrincipal();
    var userId = jwt.getClaimAsString(RESOURCE_ID_CLAIM);

    var existingBookingOpt =
        bookingRepository.findOneByUserIdAndTourId(
            AggregateReference.to(userId), AggregateReference.to(tourId));

    Predicate<Booking> isCompletedBooking =
        booking -> Objects.equals(booking.status(), BookingStatus.COMPLETED.name());
    existingBookingOpt.filter(isCompletedBooking).ifPresent(throwExceptionIfBookingExists);

    if (existingBookingOpt.isPresent()) {
      var qrCodeRef = qrCodeService.getQrCodeRefByBookingId(existingBookingOpt.get().id());
      Function<BookingInfoResponse, BookingInfoResponse> toResponseWithQrCodeRefId =
          bookingInfo -> bookingInfo.withQrReference(qrCodeRef.id());

      return existingBookingOpt
          .map(BookingInfoResponse::fromDao)
          .map(toResponseWithQrCodeRefId)
          .get();
    }

    var bookingToCreate =
        Booking.of(
            null,
            AggregateReference.to(Integer.valueOf(userId)),
            AggregateReference.to(tourId),
            BookingStatus.PENDING.name(),
            Instant.now(),
            Instant.now(),
            idempotentKey);
    var createdBooking = bookingRepository.save(bookingToCreate);
    logger.info("[bookTour] New {} is created: {}", Booking.class.getSimpleName(), createdBooking);
    var qrCodeReference = qrCodeService.createQrCodeRefForBooking(createdBooking.id());

    return BookingInfoResponse.fromDao(createdBooking).withQrReference(qrCodeReference.id());
  }

  @Override
  public BookingInfoResponse cancelTour(
      Authentication authentication, BookingCancellationRequest body, String idempotentKey) {

    logger.debug(
        "[cancelTour] Canceling {} with idempotentKey [{}]",
        Booking.class.getSimpleName(),
        idempotentKey);

    throw new UnsupportedOperationException("Not Implement Yet.");
  }

  private final Consumer<Booking> throwExceptionIfBookingExists =
      booking -> {
        throw new BookingExistsException(
            String.format(
                "UserId: [%d] already booked tourId [%d]",
                booking.userId().getId(), booking.tourId().getId()));
      };
}
