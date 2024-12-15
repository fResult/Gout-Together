package dev.fResult.goutTogether.payments.services;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import java.awt.image.BufferedImage;

public interface PaymentService {
  BufferedImage generatePaymentQr(int id) throws WriterException;

  BookingInfoResponse payByBookingId(int bookingId, String idempotentKey);

  boolean refundBooking(int bookingId, String idempotentKey);
}
