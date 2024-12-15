package dev.fResult.goutTogether.payments.services;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;

import java.awt.image.BufferedImage;

public class PaymentServiceImpl implements PaymentService {
  @Override
  public BufferedImage generatePaymentQr(Integer id) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  @Override
  public BookingInfoResponse payByBookingId(int bookingId, String idempotentKey) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }

  @Override
  public boolean refundBooking(int bookingId, String idempotentKey) {
    throw new UnsupportedOperationException("Not Implement Yet");
  }
}
