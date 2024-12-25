package dev.fResult.goutTogether.payments;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.payments.services.PaymentService;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
  private final String PAYMENT_API = "/api/v1/payments";
  private final int QR_CODE_REF_ID = 1;

  @Autowired private WebApplicationContext webApplicationContext;

  @MockitoBean private PaymentService paymentService;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  void getQrCodeImageById() throws Exception {
    // Arrange
    var mockQrCodeImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
    when(paymentService.generatePaymentQr(anyInt())).thenReturn(mockQrCodeImage);

    // Actual
    var resultActions = mockMvc.perform(get(PAYMENT_API + "/qr/{qrCodeRefId}", QR_CODE_REF_ID));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$").exists());
  }

  @Test
  void payByBookingId() throws Exception {
    // Arrange
    var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    var BOOKING_ID = 1;
    var mockBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, 1, 1, BookingStatus.COMPLETED, QR_CODE_REF_ID);

    when(paymentService.payByBookingId(anyInt(), anyString())).thenReturn(mockBookingInfo);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(PAYMENT_API + "/{bookingId}", BOOKING_ID)
                .header("idempotent-key", IDEMPOTENT_KEY));

    // Assert
    resultActions.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(BOOKING_ID));
  }
}
