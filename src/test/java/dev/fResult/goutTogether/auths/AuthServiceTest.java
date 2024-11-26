package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthServiceImpl;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import dev.fResult.goutTogether.users.entities.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @InjectMocks private AuthServiceImpl authService;

  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private final int USER_ID_1 = 1;
  private final int USER_ID_2 = 3;
  private final int USER_ID_3 = 5;
  private final int NOT_FOUND_USER_ID_1 = 88888;
  private final int NOT_FOUND_USER_ID_2 = 99999;
  private final List<Integer> USER_IDS = List.of(USER_ID_1, USER_ID_2, USER_ID_3);

  @Nested
  class FindUserCredentialTest {
    private final List<Integer> SOME_NOT_FOUND_USER_IDS =
        List.of(NOT_FOUND_USER_ID_2, USER_ID_1, NOT_FOUND_USER_ID_1, USER_ID_3);

    @Test
    void whenFindUserCredentialByEmailThenSuccess() {
      // Arrange
      var TARGET_EMAIL = "target@email.com";
      var mockUserLogin =
          UserLogin.of(1, AggregateReference.to(USER_ID_1), TARGET_EMAIL, "encryptedPassword");

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
      var NOT_FOUND_EMAIL = "in_existing@email.com";

      when(userLoginRepository.findOneByEmail(NOT_FOUND_EMAIL)).thenReturn(Optional.empty());

      // Actual
      var actualFoundUserLogin = authService.findUserCredentialByEmail(NOT_FOUND_EMAIL);

      // Assert
      assertTrue(actualFoundUserLogin.isEmpty());
    }

    @Test
    void whenFindUserCredentialsByUserIdsThenSuccess() {
      // Arrange
      var mockUserLogin1 =
          UserLogin.of(
              1, AggregateReference.to(USER_ID_1), "email1@example.com", "encryptedPassword");
      var mockUserLogin2 =
          UserLogin.of(
              2, AggregateReference.to(USER_ID_2), "email2@example.com", "encryptedPassword");
      var mockUserLogin3 =
          UserLogin.of(
              3, AggregateReference.to(USER_ID_3), "email3@example.com", "encryptedPassword");
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
              1, AggregateReference.to(USER_ID_1), "email1@example.com", "encryptedPassword");

      var mockUserLogin3 =
          UserLogin.of(
              3, AggregateReference.to(USER_ID_3), "email3@example.com", "encryptedPassword");
      var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      Executable actualExecutable =
          () -> authService.findUserCredentialsByUserIds(SOME_NOT_FOUND_USER_IDS);

      // Assert
      var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class FindTourCompanyLoginTest {
    @Test
    void whenFindCompanyLoginByUsernameThenSuccess() {
      // Arrange
      var TARGET_USERNAME = "target_username";
      var mockTourCompanyLogin =
          TourCompanyLogin.of(1, AggregateReference.to(1), TARGET_USERNAME, "encryptedPassword");
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
    void whenFindCompanyLoginByUsernameButNotFoundThenReturnEmpty() {
      // Arrange
      var NOT_FOUND_USERNAME = "in_existing_username";
      when(tourCompanyLoginRepository.findOneByUsername(NOT_FOUND_USERNAME))
          .thenReturn(Optional.empty());

      // Actual
      var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByUsername(NOT_FOUND_USERNAME);

      // Assert
      assertTrue(actualFoundCompanyLogin.isEmpty());
    }
  }

  @Nested
  class DeleteUserCredentialTest {
    @Test
    void whenDeleteUserCredentialByIdThenSuccess() {
      // Arrange
      AggregateReference<User, Integer> userRef = AggregateReference.to(USER_ID_1);
      var mockCredentialToDelete = UserLogin.of(1, userRef, "email@example.com", "password");
      when(userLoginRepository.findOneByUserId(userRef))
          .thenReturn(Optional.of(mockCredentialToDelete));

      // Actual
      var actualDeleteResult = authService.deleteUserCredentialById(USER_ID_1);

      // Assert
      verify(userLoginRepository, times(1)).delete(mockCredentialToDelete);
      assertTrue(actualDeleteResult);
    }

    @Test
    void whenDeleteUserCredentialByIdButNotFoundThenThrowException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s id [%d] not found", UserLogin.class.getSimpleName(), NOT_FOUND_USER_ID_1);

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
  }
}
