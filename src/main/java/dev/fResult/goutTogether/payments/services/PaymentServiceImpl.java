package dev.fResult.goutTogether.payments.services;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.qrcodes.QrCodeService;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentServiceImpl implements PaymentService {
  private final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

  private final QrCodeService qrCodeService;

  public PaymentServiceImpl(QrCodeService qrCodeService) {
    this.qrCodeService = qrCodeService;
  }

  @Override
  public BufferedImage generatePaymentQr(int id) throws WriterException {
    return qrCodeService.generateQrCodeImageById(id);
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
