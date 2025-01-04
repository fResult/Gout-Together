package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.auths.services.CustomUserDetailsService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.entities.UserRole;
import dev.fResult.goutTogether.users.repositories.UserRoleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
  private final String EMAIL = "user1@example.com";

  @InjectMocks private CustomUserDetailsService customUserDetailsService;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;

  @Test
  void whenLoadUserAdminDetailsByEmail_ThenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var ROLE = UserRoleName.ADMIN;
    final var HASHED_PASSWORD = "H@shedP@ssw0rd";
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var roleRef = AggregateReference.<Role, Integer>to(ROLE.getId());
    final var mockUserLogin = UserLogin.of(1, userRef, EMAIL, HASHED_PASSWORD);
    final var mockUserRole = UserRole.of(1, userRef, roleRef);
    final var expectedAuthenticatedUser =
        AuthenticatedUser.of(USER_ID, EMAIL, HASHED_PASSWORD, ROLE);

    when(userLoginRepository.findOneByEmail(anyString())).thenReturn(Optional.of(mockUserLogin));
    when(userRoleRepository.findOneByUserId(mockUserLogin.userId()))
        .thenReturn(Optional.of(mockUserRole));

    // Actual
    final var actualLoadedUserDetails = customUserDetailsService.loadUserByUsername(EMAIL);

    // Assert
    assertEquals(expectedAuthenticatedUser, actualLoadedUserDetails);
  }

  @Test
  void whenLoadUserConsumerDetailsByEmailSuccess() {
    // Arrange
    final var USER_ID = 2;
    final var ROLE = UserRoleName.CONSUMER;
    final var HASHED_PASSWORD = "H@shedP@ssw0rd";
    final var userRef = AggregateReference.<User, Integer>to(USER_ID);
    final var roleRef = AggregateReference.<Role, Integer>to(ROLE.getId());
    final var mockUserLogin = UserLogin.of(2, userRef, EMAIL, HASHED_PASSWORD);
    final var mockUserRole = UserRole.of(2, userRef, roleRef);
    final var expectedAuthenticatedUser =
        AuthenticatedUser.of(USER_ID, EMAIL, HASHED_PASSWORD, ROLE);

    when(userLoginRepository.findOneByEmail(anyString())).thenReturn(Optional.of(mockUserLogin));
    when(userRoleRepository.findOneByUserId(mockUserLogin.userId()))
        .thenReturn(Optional.of(mockUserRole));

    // Actual
    final var actualLoadedUserDetails = customUserDetailsService.loadUserByUsername(EMAIL);

    // Assert
    assertEquals(expectedAuthenticatedUser, actualLoadedUserDetails);
  }

  @Test
  void whenLoadUserDetailsByUsername_ButUserNotFound_ThenThrowException() {
    // Arrange
    final var NOT_FOUND_EMAIL = "in_existing@email.com";
    final var expectedErrorMessage =
        String.format(
            "%s with email [%s] not found", UserLogin.class.getSimpleName(), NOT_FOUND_EMAIL);
    when(userLoginRepository.findOneByEmail(anyString())).thenReturn(Optional.empty());

    // Actual
    final Executable actualLoadedUserDetails =
        () -> customUserDetailsService.loadUserByUsername(NOT_FOUND_EMAIL);

    // Assert
    final var exception =
        assertThrowsExactly(EntityNotFoundException.class, actualLoadedUserDetails);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenLoadUserDetailsByUsername_ButUserRoleNotFound_ThenThrowException() {
    // Arrange
    final var NOT_FOUND_USER_ID = 99999;
    final var expectedErrorMessage =
        String.format(
            "%s with userId [%d] not found", UserLogin.class.getSimpleName(), NOT_FOUND_USER_ID);
    final var HASHED_PASSWORD = "H@shedP@ssw0rd";
    final var userRef = AggregateReference.<User, Integer>to(NOT_FOUND_USER_ID);
    final var mockUserLogin = UserLogin.of(1, userRef, EMAIL, HASHED_PASSWORD);

    when(userLoginRepository.findOneByEmail(anyString())).thenReturn(Optional.of(mockUserLogin));
    when(userRoleRepository.findOneByUserId(mockUserLogin.userId())).thenReturn(Optional.empty());

    // Actual
    final Executable actualLoadedUserDetails = () -> customUserDetailsService.loadUserByUsername(EMAIL);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualLoadedUserDetails);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenLoadCompanyDetailsByUsernameSuccess() {
    // Arrange
    final var USERNAME = "DisasterTour";
    final var TOUR_COMPANY_ID = 1;
    final var HASHED_PASSWORD = "H@shedP@ssw0rd";
    final var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
    final var mockTourCompanyLogin =
        TourCompanyLogin.of(1, tourCompanyRef, "DisasterTour", HASHED_PASSWORD);
    final var expectedAuthenticatedUser =
        AuthenticatedUser.of(TOUR_COMPANY_ID, USERNAME, HASHED_PASSWORD, UserRoleName.COMPANY);

    when(tourCompanyLoginRepository.findOneByUsername(anyString()))
        .thenReturn(Optional.of(mockTourCompanyLogin));

    // Actual
    final var actualLoadedCompanyDetails = customUserDetailsService.loadUserByUsername(USERNAME);

    // Assert
    assertEquals(expectedAuthenticatedUser, actualLoadedCompanyDetails);
  }

  @Test
  void whenLoadCompanyDetailsByUsername_ButCompanyNotFound_ThenThrowException() {
    // Arrange
    final var NOT_FOUND_USERNAME = "NonExistentCompany";
    final var expectedErrorMessage =
        String.format(
            "%s with username [%s] not found",
            TourCompanyLogin.class.getSimpleName(), NOT_FOUND_USERNAME);

    when(tourCompanyLoginRepository.findOneByUsername(anyString())).thenReturn(Optional.empty());

    // Actual
    final Executable actualLoadedCompanyDetails =
        () -> customUserDetailsService.loadUserByUsername(NOT_FOUND_USERNAME);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualLoadedCompanyDetails);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
