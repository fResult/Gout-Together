package dev.fResult.goutTogether.auths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthServiceImpl;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import java.util.List;
import java.util.Set;
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

  @Nested
  class FindUserCredentialsByUsersIdTest {
    private final int USER_ID_1 = 1;
    private final int USER_ID_2 = 3;
    private final int USER_ID_3 = 5;
    private final int NOT_FOUND_USER_ID_1 = 88888;
    private final int NOT_FOUND_USER_ID_2 = 99999;
    private final List<Integer> USER_IDS = List.of(USER_ID_1, USER_ID_2, USER_ID_3);
    private final List<Integer> SOME_NOT_FOUND_USER_IDS =
        List.of(NOT_FOUND_USER_ID_2, USER_ID_1, NOT_FOUND_USER_ID_1, USER_ID_3);

    @Test
    void whenFindUserCredentialsByUserIdsThenSuccess() {
      // Arrange
      var mockUserLogin1 =
          new UserLogin(
              1, AggregateReference.to(USER_ID_1), "email1@example.com", "encryptedPassword");
      var mockUserLogin2 =
          new UserLogin(
              2, AggregateReference.to(USER_ID_2), "email2@example.com", "encryptedPassword");
      var mockUserLogin3 =
          new UserLogin(
              3, AggregateReference.to(USER_ID_3), "email3@example.com", "encryptedPassword");
      var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin2, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      var actualFoundUserLogins = authService.findUserCredentialsByUserIds(USER_IDS);

      // Assert
      assertEquals(mockFoundUserLogins, actualFoundUserLogins);
    }

    @Test
    void whenFindUserCredentialsByUserIdsButSomeAreNotFoundThenThrowXxxException() {
      // Arrange
      var expectedErrorMessage =
          String.format(
              "%s ids [%s] not found",
              UserLogin.class.getSimpleName(),
              Set.of(NOT_FOUND_USER_ID_2, NOT_FOUND_USER_ID_1)
                  .toString()
                  .replaceAll("[\\[\\]]", ""));
      var mockUserLogin1 =
          new UserLogin(
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
}
