package dev.fResult.goutTogether.bookings;

import static dev.fResult.goutTogether.common.Constants.RESOURCE_ID_CLAIM;
import static dev.fResult.goutTogether.common.Constants.ROLES_CLAIM;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fResult.goutTogether.bookings.dtos.BookingCancellationRequest;
import dev.fResult.goutTogether.bookings.dtos.BookingInfoResponse;
import dev.fResult.goutTogether.bookings.entities.Booking;
import dev.fResult.goutTogether.bookings.repositories.BookingRepository;
import dev.fResult.goutTogether.bookings.services.BookingService;
import dev.fResult.goutTogether.common.enumurations.BookingStatus;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.BookingExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tours.entities.Tour;
import dev.fResult.goutTogether.tours.entities.TourCount;
import dev.fResult.goutTogether.tours.repositories.TourCountRepository;
import dev.fResult.goutTogether.users.entities.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.http.MediaType;
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
  private final int BOOKING_ID = 59;
  private final int TOUR_ID = 101;
  private final String EMAIL = "email@example.com";
  private final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BookingService bookingService;
  @MockitoBean private TourCountRepository tourCountRepository;
  @MockitoBean private BookingRepository bookingRepository;

  private MockMvc mockMvc;
  private BookingController.SimpleService simpleService;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private Authentication buildAuthentication(int resourceId, UserRoleName roleName, String email) {
    final var jwt =
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
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    final var mockCreatedTourBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.COMPLETED, 1);

    when(bookingService.bookTour(any(Authentication.class), anyInt(), anyString()))
        .thenReturn(mockCreatedTourBookingInfo);

    // Actual
    final var resultActions =
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
  void whenBookTourByTourId_ButInvalidIdempotentKey_ThenReturn400() throws Exception {
    // Arrange
    final var INVALID_IDEMPOTENT_KEY = "invalid-idempotent-key";
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(BOOKING_API + "/tours/{tourId}", TOUR_ID)
                .principal(authentication)
                .header("idempotent-key", INVALID_IDEMPOTENT_KEY));

    // Assert
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void whenBookTourByTourId_ButTourIdIsInvalid_ThenReturn400() throws Exception {
    // Arrange
    final var INVALID_TOUR_ID = 0;
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(BOOKING_API + "/tours/{tourId}", INVALID_TOUR_ID)
                .principal(authentication)
                .header("idempotent-key", IDEMPOTENT_KEY));

    // Assert
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  void whenBookTourByTourId_ButUserTourAlreadyBooked_ThenReturn409() throws Exception {
    // Arrange
    final var TOUR_ID = 120;
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    final var expectedErrorMessage =
        String.format("UserId [%d] already booked tourId [%d]", USER_ID, TOUR_ID);

    when(bookingService.bookTour(any(Authentication.class), anyInt(), anyString()))
        .thenThrow(new BookingExistsException(expectedErrorMessage));

    // Actual
    final var resultActions =
        mockMvc.perform(
            post(BOOKING_API + "/tours/{tourId}", TOUR_ID)
                .header("idempotent-key", IDEMPOTENT_KEY)
                .principal(authentication));

    // Assert
    resultActions
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value(expectedErrorMessage));
  }

  @Test
  void whenCancelTourBookingById_ThenReturn200() throws Exception {
    // Arrange
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    final var body = BookingCancellationRequest.of(TOUR_ID);
    final var mockCancelledTourBookingInfo =
        BookingInfoResponse.of(BOOKING_ID, USER_ID, TOUR_ID, BookingStatus.CANCELLED, 1);

    when(bookingService.cancelTour(any(Authentication.class), anyInt(), any(), anyString()))
        .thenReturn(mockCancelledTourBookingInfo);

    // Actual
    final var resultActions =
        mockMvc.perform(
            put(BOOKING_API + "/{id}/cancel", BOOKING_ID)
                .principal(authentication)
                .header("idempotent-key", IDEMPOTENT_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(BOOKING_ID))
        .andExpect(jsonPath("$.userId").value(USER_ID))
        .andExpect(jsonPath("$.tourId").value(TOUR_ID))
        .andExpect(jsonPath("$.status").value(BookingStatus.CANCELLED.name()));
  }

  @Test
  void whenCancelTourBookingById_ButNotFound_ThenReturn404() throws Exception {
    // Arrange
    final var NOT_FOUND_BOOKING_ID = 99999;
    final var authentication = buildAuthentication(USER_ID, UserRoleName.CONSUMER, EMAIL);
    final var body = BookingCancellationRequest.of(TOUR_ID);
    final var expectedErrorMessage =
        String.format(
            "%s with id [%d] not found", Booking.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

    when(bookingService.cancelTour(any(Authentication.class), anyInt(), any(), anyString()))
        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

    // Actual
    final var resultActions =
        mockMvc.perform(
            put(BOOKING_API + "/{id}/cancel", NOT_FOUND_BOOKING_ID)
                .header("idempotent-key", IDEMPOTENT_KEY)
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));

    // Assert
    resultActions
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value(expectedErrorMessage));
  }
}

@ExtendWith(MockitoExtension.class)
class SimpleServiceTest {
  private final int USER_ID = 9;
  private final int BOOKING_ID = 59;
  private final int TOUR_ID = 101;
  private final int NOT_FOUND_TOUR_ID = 99999;
  private final String IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();

  @InjectMocks private BookingController bookingController;

  @Mock private BookingService bookingService;
  @Mock private TourCountRepository tourCountRepository;
  @Mock private BookingRepository bookingRepository;

  private BookingController.SimpleService simpleService;

  @BeforeEach
  public void setup() {
    simpleService = bookingController.new SimpleService();
  }

  private Booking buildCompletedBooking(int bookingId, int userId, int tourId) {
    return Booking.of(
        bookingId,
        AggregateReference.to(userId),
        AggregateReference.to(tourId),
        BookingStatus.COMPLETED.name(),
        Instant.now(),
        Instant.now().minusSeconds(12),
        IDEMPOTENT_KEY);
  }

  @Test
  void whenIncreaseTourCountByBookingId_ThenVerifyActions() {
    // Arrange
    final var TOUR_COUNT_ID = 99;
    final var TOUR_COUNT_AMOUNT = 10;
    final var AMOUNT_TO_ADD = 5;
    final var TOUR_COUNT_AMOUNT_AFTER_ADDED = TOUR_COUNT_AMOUNT + AMOUNT_TO_ADD;
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var mockCompletedBooking = buildCompletedBooking(BOOKING_ID, USER_ID, TOUR_ID);
    final var mockTourCount = TourCount.of(TOUR_COUNT_ID, tourRef, TOUR_COUNT_AMOUNT);
    final var mockIncresedTourCount =
        TourCount.of(TOUR_COUNT_ID, tourRef, TOUR_COUNT_AMOUNT_AFTER_ADDED);

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(mockCompletedBooking));
    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.of(mockTourCount));
    when(tourCountRepository.save(any(TourCount.class))).thenReturn(mockIncresedTourCount);

    // Actual
    simpleService.updateTourCountById(BOOKING_ID, 5);

    // Assert
    verify(bookingRepository, times(1)).findById(BOOKING_ID);
    verify(tourCountRepository, times(1)).findOneByTourId(tourRef);
    verify(tourCountRepository, times(1)).save(mockIncresedTourCount);
  }

  @Test
  void whenIncreaseTourCountByBookingId_ButBookingNotFound_ThenVerifyActions() {
    // Arrange
    final var NOT_FOUND_BOOKING_ID = 99999;
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var expectedErrorMessage =
        String.format("%s id [%d] not found", Booking.class.getSimpleName(), NOT_FOUND_BOOKING_ID);

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> simpleService.updateTourCountById(NOT_FOUND_BOOKING_ID, 5);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
    verify(bookingRepository, times(1)).findById(NOT_FOUND_BOOKING_ID);
    verify(tourCountRepository, never()).findOneByTourId(tourRef);
    verify(tourCountRepository, never()).save(any(TourCount.class));
  }

  @Test
  void whenIncreaseTourCountByBookingId_ButTourCountNotFound_ThenVerifyActions() {
    // Arrange
    final var notFoundTourRef = AggregateReference.<Tour, Integer>to(NOT_FOUND_TOUR_ID);
    final var mockCompletedBooking = buildCompletedBooking(BOOKING_ID, USER_ID, NOT_FOUND_TOUR_ID);
    final var expectedErrorMessage =
        String.format(
            "%s with %s [%d] not found",
            TourCount.class.getSimpleName(), "tourId", NOT_FOUND_TOUR_ID);

    when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(mockCompletedBooking));
    when(tourCountRepository.findOneByTourId(notFoundTourRef)).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable = () -> simpleService.updateTourCountById(BOOKING_ID, 5);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
    verify(bookingRepository, times(1)).findById(BOOKING_ID);
    verify(tourCountRepository, times(1)).findOneByTourId(notFoundTourRef);
    verify(tourCountRepository, never()).save(any(TourCount.class));
  }

  @Test
  void whenIncreaseTourCountByTourId_ThenVerifyActions() {
    // Arrange
    final var TOUR_COUNT_ID = 99;
    final var AMOUNT_TO_ADD = 5;
    final var TOUR_COUNT_AMOUNT = 10;
    final var TOUR_COUNT_AMOUNT_AFTER_ADDED = TOUR_COUNT_AMOUNT + AMOUNT_TO_ADD;
    final var tourRef = AggregateReference.<Tour, Integer>to(TOUR_ID);
    final var mockTourCount = TourCount.of(TOUR_COUNT_ID, tourRef, TOUR_COUNT_AMOUNT);
    final var mockIncresedTourCount =
        TourCount.of(TOUR_COUNT_ID, tourRef, TOUR_COUNT_AMOUNT_AFTER_ADDED);

    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.of(mockTourCount));
    when(tourCountRepository.save(any(TourCount.class))).thenReturn(mockIncresedTourCount);

    // Actual
    simpleService.updateTourCountByTourId(TOUR_ID, 5);

    // Assert
    verify(tourCountRepository, times(1)).findOneByTourId(tourRef);
    verify(tourCountRepository, times(1)).save(mockIncresedTourCount);
  }

  @Test
  void whenIncreaseTourCountByTourId_ButTourCountNotFound_ThenVerifyActions() {
    // Arrange
    final var tourRef = AggregateReference.<Tour, Integer>to(NOT_FOUND_TOUR_ID);
    final var expectedErrorMessage =
        String.format(
            "%s with %s [%s] not found",
            TourCount.class.getSimpleName(), "tourId", NOT_FOUND_TOUR_ID);

    when(tourCountRepository.findOneByTourId(tourRef)).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable =
        () -> simpleService.updateTourCountByTourId(NOT_FOUND_TOUR_ID, 5);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
    verify(tourCountRepository, times(1)).findOneByTourId(tourRef);
    verify(tourCountRepository, never()).save(any(TourCount.class));
  }
}
