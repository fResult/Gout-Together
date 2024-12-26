package dev.fResult.goutTogether.bookings;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.BookingExistsException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
  private final String BOOKING_API = "/api/v1/bookings";
  private final int USER_ID = 9;
  private final String EMAIL = "email@example.com";
  private final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BookingService bookingService;
  @MockitoBean private TourCountRepository tourCountRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private Authentication buildAuthentication(int resourceId, UserRoleName roleName, String email) {
    var jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", email)
            .claim(RESOURCE_ID_CLAIM, String.valueOf(resourceId))
            .claim(ROLES_CLAIM, List.of("ROLE_" + roleName.name()))
            .build();
    return new JwtAuthenticationToken(jwt);
  }

  @Test
  void whenBookTourByTourId_ThenReturn201() throws Exception {
    // Arrange
    var BOOKING_ID = 59;
    var TOUR_ID = 101;
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var mockCreatedTourBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.COMPLETED, 1);

    when(bookingService.bookTour(any(Authentication.class), anyInt(), anyString()))
        .thenReturn(mockCreatedTourBookingInfo);

    // Actual
    var resultActions =
        mockMvc.perform(
            post(BOOKING_API + "/tours/{tourId}", TOUR_ID)
                .principal(authentication)
                .header("idempotent-key", IDEMPOTENT_KEY));

    // Assert
    resultActions
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(BOOKING_ID))
        .andExpect(jsonPath("$.userId").value(USER_ID))
        .andExpect(jsonPath("$.tourId").value(TOUR_ID))
        .andExpect(jsonPath("$.status").value(BookingStatus.COMPLETED.name()));
  }

  @Test
  void whenBookingTourByTourId_ButUserTourAlreadyBooked_ThenReturn409() throws Exception {
    // Arrange
    var TOUR_ID = 120;
    var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    var expectedErrorMessage =
        String.format("UserId [%d] already booked tourId [%d]", USER_ID, TOUR_ID);

    when(bookingService.bookTour(any(Authentication.class), anyInt(), anyString()))
        .thenThrow(new BookingExistsException(expectedErrorMessage));

    // Actual
    var resultActions =
        mockMvc.perform(
            post(BOOKING_API + "/tours/{tourId}", TOUR_ID)
                .header("idempotent-key", IDEMPOTENT_KEY)
                .principal(authentication));

    // Assert
    resultActions
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value(expectedErrorMessage));
  }
}
