package dev.fResult.goutTogether.payments;

import static dev.fResult.goutTogether.common.Constants.API_PAYMENT_PATH;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.payments.services.PaymentService;
import jakarta.validation.constraints.Min;
import java.awt.image.BufferedImage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(API_PAYMENT_PATH)
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping(value = "/qr/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<BufferedImage> getQrCodeImageById(@PathVariable @Min(1) Integer id) {
        return ResponseEntity.ok(paymentService.generatePaymentQr(id));
    }
    
    @PostMapping("/{bookingId}")
    public ResponseEntity<BookingInfoResponse> payByBookingId(
            @RequestHeader("idempotent-key") String idempotentKey,
            @PathVariable @Min(1) Integer bookingId) {

        return ResponseEntity.ok(paymentService.payByBookingId(bookingId, idempotentKey));
    }
}
