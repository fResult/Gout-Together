package dev.fResult.goutTogether.payments.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import jakarta.validation.constraints.Min;
import java.awt.image.BufferedImage;

public interface PaymentService {
  BufferedImage generatePaymentQr(Integer id);

  BookingInfoResponse payByBookingId(int bookingId, String idempotentKey);

  boolean refundBooking(int bookingId, String idempotentKey);
}
