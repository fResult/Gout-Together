package dev.fResult.goutTogether.bookings.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.dtos.BookingRequest;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.QrCodeStatus;
import dev.fResult.goutTogether.common.exceptions.BookingExistsException;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;

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
  public BookingInfoResponse bookTour(
      Authentication authentication, BookingRequest body, String idempotentKey) {
    logger.debug("[bookTour] New {} is creating", Booking.class.getSimpleName());

    var jwt = (Jwt) authentication.getPrincipal();
    var userId = jwt.getClaimAsString(RESOURCE_ID_CLAIM);

    var existingBookingOpt =
        bookingRepository.findOneByUserIdAndTourId(
            AggregateReference.to(userId), AggregateReference.to(body.tourId()));

    Predicate<Booking> isCompletedBooking =
        booking -> Objects.equals(booking.status(), BookingStatus.COMPLETED.name());
    existingBookingOpt.filter(isCompletedBooking).ifPresent(throwExceptionIfBookingExists);

    if (existingBookingOpt.isPresent()) {
      return existingBookingOpt.map(BookingInfoResponse::fromDao).get();
    }

    var bookingToCreate =
        Booking.of(
            null,
            AggregateReference.to(Integer.valueOf(userId)),
            null,
            null,
            null,
            Instant.now(),
            idempotentKey);
    var createdBooking = bookingRepository.save(bookingToCreate);
    logger.info("[bookTour] New {} is created: {}", Booking.class.getSimpleName(), createdBooking);
    var qrCodeReference =
        qrCodeService.updateQrCodeRefStatusByBookingId(createdBooking.id(), QrCodeStatus.ACTIVATED);

    return BookingInfoResponse.fromDao(bookingToCreate).withQrReference(qrCodeReference.id());
  }

  @Override
  public BookingInfoResponse cancelTour(String idempotentKey, BookingRequest body) {
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
