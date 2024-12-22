package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.auths.services.CustomUserDetailsService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
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

  @InjectMocks private CustomUserDetailsService customUserDetailsService;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;

  @Test
  void whenLoadCompanyDetailsByUsernameSuccess() {
    // Arrange
    var USERNAME = "DisasterTour";
    var TOUR_COMPANY_ID = 1;
    var HASHED_PASSWORD = "H@shedP@ssw0rd";
    var tourCompanyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
    var mockTourCompanyLogin =
        TourCompanyLogin.of(1, tourCompanyRef, "DisasterTour", HASHED_PASSWORD);
    var expectedAuthenticatedUser =
        AuthenticatedUser.of(TOUR_COMPANY_ID, USERNAME, HASHED_PASSWORD, UserRoleName.COMPANY);

    when(tourCompanyLoginRepository.findOneByUsername(anyString()))
        .thenReturn(Optional.of(mockTourCompanyLogin));

    // Actual
    var actualLoadedCompanyDetails = customUserDetailsService.loadUserByUsername(USERNAME);

    // Assert
    assertEquals(expectedAuthenticatedUser, actualLoadedCompanyDetails);
  }

  @Test
  void whenLoadCompanyDetailsByUsernameButNotFoundThenThrowException() {
    // Arrange
    var USERNAME = "DisasterTour";
    var expectedErrorMessage =
        String.format(
            "%s with username [%s] not found", TourCompanyLogin.class.getSimpleName(), USERNAME);

    when(tourCompanyLoginRepository.findOneByUsername(anyString())).thenReturn(Optional.empty());

    // Actual
    Executable actualLoadedCompanyDetails =
        () -> customUserDetailsService.loadUserByUsername(USERNAME);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualLoadedCompanyDetails);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
