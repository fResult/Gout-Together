package dev.fResult.goutTogether.payments;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;

import com.google.zxing.WriterException;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.payments.services.PaymentService;
import jakarta.validation.constraints.Min;
import java.awt.image.BufferedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping(API_PAYMENT_PATH)
public class PaymentController {
  private final Logger logger = LoggerFactory.getLogger(PaymentController.class);

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @GetMapping(value = "/qr/{qrCodeRefId}", produces = MediaType.IMAGE_PNG_VALUE)
  public ResponseEntity<BufferedImage> getQrCodeImageById(@PathVariable @Min(1) Integer qrCodeRefId)
      throws WriterException {

    logger.debug("[getQrCodeImageById] Getting QR code image by id [{}]", qrCodeRefId);

    return ResponseEntity.ok(paymentService.generatePaymentQr(qrCodeRefId));
  }

  @PostMapping("/{bookingId}")
  public ResponseEntity<BookingInfoResponse> payByBookingId(
      @RequestHeader("idempotent-key") String idempotentKey,
      @PathVariable @Min(1) Integer bookingId) {

    logger.debug("[payByBookingId] Paying by booking id [{}]", bookingId);

    return ResponseEntity.ok(paymentService.payByBookingId(bookingId, idempotentKey));
  }
}
