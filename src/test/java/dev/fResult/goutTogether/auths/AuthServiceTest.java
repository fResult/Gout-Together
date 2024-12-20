package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.auths.services.AuthServiceImpl;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.users.entities.User;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks @Spy private AuthServiceImpl authService;

  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private final int USER_ID_1 = 1;
  private final int USER_ID_2 = 3;
  private final int USER_ID_3 = 5;
  private final int NOT_FOUND_USER_ID_1 = 88888;
  private final int NOT_FOUND_USER_ID_2 = 99999;
  private final List<Integer> USER_IDS = List.of(USER_ID_1, USER_ID_2, USER_ID_3);
  private final String TARGET_EMAIL = "target@email.com";
  private final String NOT_FOUND_EMAIL = "in_existing@email.com";

  private final int TOUR_COMPANY_ID = 1;
  private final int NOT_FOUND_TOUR_COMPANY_ID = 99999;
  private final String TARGET_USERNAME = "target_username";
  private final String NOT_FOUND_USERNAME = "in_existing_username";

  private final String ENCRYPTED_PASSWORD =
      "$argon2id$v=19$m=16384,t=2,p=1$NDMxQRhx/yCgg2uNT4GMfA$DZEh4+dJNDDtv8DHxvw7Tm1rXSHWlG/Wxf8S/rrAtTI";

  @Nested
  class FindUserCredentialTest {
    private final List<Integer> SOME_NOT_FOUND_USER_IDS =
        List.of(NOT_FOUND_USER_ID_2, USER_ID_1, NOT_FOUND_USER_ID_1, USER_ID_3);

    @Test
    void whenFindUserCredentialsByUserIdsThenSuccess() {
      // Arrange
      var mockUserLogin1 =
          UserLogin.of(
              1, AggregateReference.to(USER_ID_1), "email1@example.com", ENCRYPTED_PASSWORD);
      var mockUserLogin2 =
          UserLogin.of(
              2, AggregateReference.to(USER_ID_2), "email2@example.com", ENCRYPTED_PASSWORD);
      var mockUserLogin3 =
          UserLogin.of(
              3, AggregateReference.to(USER_ID_3), "email3@example.com", ENCRYPTED_PASSWORD);
      var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin2, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      var actualFoundUserLogins = authService.findUserCredentialsByUserIds(USER_IDS);

      // Assert
      assertEquals(mockFoundUserLogins, actualFoundUserLogins);
    }

    @Test
    void whenFindUserCredentialsByUserIdsButSomeAreNotFoundThenThrowEntityNotFoundException() {
      // Arrange
      var expectedErrorMessage =
          String.format( // TODO: Refactor this part to a ErrorMessageHelper's method
              "%s ids [%s] not found",
              UserLogin.class.getSimpleName(),
              new HashSet<>(List.of(NOT_FOUND_USER_ID_2, NOT_FOUND_USER_ID_1))
                  .toString()
                  .replaceAll("[\\[\\]]", ""));
      var mockUserLogin1 =
          UserLogin.of(
              1, AggregateReference.to(USER_ID_1), "email1@example.com", ENCRYPTED_PASSWORD);

      var mockUserLogin3 =
          UserLogin.of(
              3, AggregateReference.to(USER_ID_3), "email3@example.com", ENCRYPTED_PASSWORD);
      var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      Executable actualExecutable =
          () -> authService.findUserCredentialsByUserIds(SOME_NOT_FOUND_USER_IDS);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void whenFindUserCredentialByUserIdThenSuccess() {
      // Arrange
      var mockUserLogin =
          UserLogin.of(
              1, AggregateReference.to(USER_ID_1), "email@example.com", ENCRYPTED_PASSWORD);
      var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      when(userLoginRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserLogin));

      // Actual
      var actualFoundUserLogin = authService.findUserCredentialByUserId(USER_ID_1);

      // Assert
      assertEquals(mockUserLogin, actualFoundUserLogin);
    }

    @Test
    void whenFindUserCredentialByUserIdButNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              UserLogin.class.getSimpleName(), "userId", NOT_FOUND_USER_ID_1);
      AggregateReference<User, Integer> notFoundUserRef =
          AggregateReference.to(NOT_FOUND_USER_ID_1);
      when(userLoginRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

      // Actual
      Executable actualExecutable =
          () -> authService.findUserCredentialByUserId(NOT_FOUND_USER_ID_1);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void whenFindUserCredentialByEmailThenSuccess() {
      // Arrange
      var mockUserLogin =
          UserLogin.of(1, AggregateReference.to(USER_ID_1), TARGET_EMAIL, ENCRYPTED_PASSWORD);

      when(userLoginRepository.findOneByEmail(TARGET_EMAIL)).thenReturn(Optional.of(mockUserLogin));

      // Actual
      var actualFoundUserLogin = authService.findUserCredentialByEmail(TARGET_EMAIL);

      // Assert
      assertTrue(actualFoundUserLogin.isPresent());
      assertEquals(mockUserLogin, actualFoundUserLogin.get());
    }

    @Test
    void whenFindUserCredentialByEmailButNotFoundThenReturnEmpty() {
      // Arrange
      when(userLoginRepository.findOneByEmail(NOT_FOUND_EMAIL)).thenReturn(Optional.empty());

      // Actual
      var actualFoundUserLogin = authService.findUserCredentialByEmail(NOT_FOUND_EMAIL);

      // Assert
      assertTrue(actualFoundUserLogin.isEmpty());
    }
  }

  @Nested
  class CreateUserCredentialTest {
    @Test
    void whenCreateUserCredentialThenSuccess() {
      // Arrange
      var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      var mockUserLoginToCreate = UserLogin.of(1, userRef, TARGET_EMAIL, ENCRYPTED_PASSWORD);
      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(userLoginRepository.save(any(UserLogin.class))).thenReturn(mockUserLoginToCreate);

      // Actual
      var actualCreatedUserLogin =
          authService.createUserCredential(USER_ID_1, TARGET_EMAIL, "password");

      // Assert
      assertEquals(ENCRYPTED_PASSWORD, mockUserLoginToCreate.password());
      assertEquals(mockUserLoginToCreate, actualCreatedUserLogin);
    }
  }

  @Nested
  class DeleteUserCredentialTest {
    @Test
    void whenDeleteUserCredentialByIdThenSuccess() {
      // Arrange
      var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      var mockCredentialToDelete = UserLogin.of(1, userRef, "email@example.com", "password");
      doReturn(mockCredentialToDelete).when(authService).findUserCredentialByUserId(anyInt());
      doNothing().when(userLoginRepository).delete(any(UserLogin.class));

      // Actual
      var actualDeleteResult = authService.deleteUserCredentialByUserId(USER_ID_1);

      // Assert
      verify(userLoginRepository, times(1)).delete(mockCredentialToDelete);
      assertTrue(actualDeleteResult);
    }

    @Test
    void whenDeleteUserCredentialByIdButNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              UserLogin.class.getSimpleName(), "userId", NOT_FOUND_USER_ID_1);

      doAnswer(
              invocation -> {
                var targetUserId = invocation.getArgument(0, Integer.class);
                throw new EntityNotFoundException(
                    String.format(
                        "%s with %s [%d] not found",
                        UserLogin.class.getSimpleName(), "userId", targetUserId));
              })
          .when(authService)
          .findUserCredentialByUserId(anyInt());

      // Actual
      Executable actualExecutable =
          () -> authService.deleteUserCredentialByUserId(NOT_FOUND_USER_ID_1);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class FindTourCompanyCredentialTest {
    @Test
    void whenFindCompanyCredentialByUsernameThenSuccess() {
      // Arrange
      var mockTourCompanyLogin =
          TourCompanyLogin.of(1, AggregateReference.to(1), TARGET_USERNAME, ENCRYPTED_PASSWORD);
      when(tourCompanyLoginRepository.findOneByUsername(TARGET_USERNAME))
          .thenReturn(Optional.of(mockTourCompanyLogin));

      // Actual
      var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByUsername(TARGET_USERNAME);

      // Assert
      assertTrue(actualFoundCompanyLogin.isPresent());
      assertEquals(mockTourCompanyLogin, actualFoundCompanyLogin.get());
    }

    @Test
    void whenFindCompanyCredentialByUsernameButNotFoundThenReturnEmpty() {
      // Arrange
      when(tourCompanyLoginRepository.findOneByUsername(NOT_FOUND_USERNAME))
          .thenReturn(Optional.empty());

      // Actual
      var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByUsername(NOT_FOUND_USERNAME);

      // Assert
      assertTrue(actualFoundCompanyLogin.isEmpty());
    }

    @Test
    void whenFindCompanyCredentialByCompanyIdThenSuccess() {
      // Arrange
      AggregateReference<TourCompany, Integer> tourCompanyRef =
          AggregateReference.to(TOUR_COMPANY_ID);
      var mockTourCompanyLogin =
          TourCompanyLogin.of(1, tourCompanyRef, "MyTour", ENCRYPTED_PASSWORD);
      when(tourCompanyLoginRepository.findOneByTourCompanyId(tourCompanyRef))
          .thenReturn(Optional.of(mockTourCompanyLogin));

      // Actual
      var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByTourCompanyId(TOUR_COMPANY_ID);

      // Assert
      assertEquals(mockTourCompanyLogin, actualFoundCompanyLogin);
    }

    @Test
    void whenFindCompanyCredentialByCompanyIdButNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              TourCompanyLogin.class.getSimpleName(), "tourCompanyId", NOT_FOUND_TOUR_COMPANY_ID);
      AggregateReference<TourCompany, Integer> notFoundTourCompanyRef =
          AggregateReference.to(NOT_FOUND_TOUR_COMPANY_ID);

      when(tourCompanyLoginRepository.findOneByTourCompanyId(notFoundTourCompanyRef))
          .thenReturn(Optional.empty());

      // Actual
      Executable actualExecutable =
          () -> authService.findTourCompanyCredentialByTourCompanyId(NOT_FOUND_TOUR_COMPANY_ID);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class CreateTourCompanyLoginTest {
    @Test
    void whenCreateCompanyLoginThenSuccess() {
      // Arrange
      var companyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
      var mockCompanyLoginToCreate =
          TourCompanyLogin.of(1, companyRef, "MyTour", ENCRYPTED_PASSWORD);
      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(tourCompanyLoginRepository.save(any(TourCompanyLogin.class)))
          .thenReturn(mockCompanyLoginToCreate);

      // Actual
      var actualCreatedUserLogin =
          authService.createTourCompanyLogin(TOUR_COMPANY_ID, "MyTour", "password");

      // Assert
      assertEquals(ENCRYPTED_PASSWORD, mockCompanyLoginToCreate.password());
      assertEquals(mockCompanyLoginToCreate, actualCreatedUserLogin);
    }
  }

  @Nested
  class DeleteCompanyLoginTest {
    @Test
    void whenDeleteCompanyLoginByIdThenSuccess() {
      // Arrange
      var companyRef = AggregateReference.<TourCompany, Integer>to(USER_ID_1);
      var mockCompanyLoginToDelete =
          TourCompanyLogin.of(1, companyRef, "MyTour", ENCRYPTED_PASSWORD);
      doReturn(mockCompanyLoginToDelete)
          .when(authService)
          .findTourCompanyCredentialByTourCompanyId(anyInt());

      // Actual
      var actualDeleteResult = authService.deleteTourCompanyLoginByTourCompanyId(TOUR_COMPANY_ID);

      // Assert
      verify(tourCompanyLoginRepository, times(1)).delete(mockCompanyLoginToDelete);
      assertTrue(actualDeleteResult);
    }

    @Test
    void whenDeleteCompanyLoginByIdButNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              TourCompanyLogin.class.getSimpleName(), "tourCompanyId", NOT_FOUND_TOUR_COMPANY_ID);

      AggregateReference<TourCompany, Integer> notFoundCompanyRef =
          AggregateReference.to(NOT_FOUND_TOUR_COMPANY_ID);

      doAnswer(
              invocation -> {
                var targetCompanyId = invocation.getArgument(0, Integer.class);
                throw new EntityNotFoundException(
                    String.format(
                        "%s with %s [%d] not found",
                        TourCompanyLogin.class.getSimpleName(), "tourCompanyId", targetCompanyId));
              })
          .when(authService)
          .findTourCompanyCredentialByTourCompanyId(anyInt());

      // Actual
      Executable actualExecutable =
          () -> authService.deleteTourCompanyLoginByTourCompanyId(NOT_FOUND_TOUR_COMPANY_ID);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }
}
