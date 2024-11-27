package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.users.services.UserServiceImpl;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  private static final int USER_ID = 1;
  private static final int NOT_FOUND_USER_ID = 99999;

  @InjectMocks private UserServiceImpl userService;

  @Mock UserRepository userRepository;
  @Mock AuthService authService;
  @Mock WalletService walletService;

  @Test
  void whenGetUsersThenSuccess() {
    // Arrange
    var ANOTHER_USER_ID = 2;
    var mockUser1 = User.of(USER_ID, "John", "Wick", "0999999999");
    var mockUser2 = User.of(ANOTHER_USER_ID, "Thomas", "Anderson", "0666666666");
    var mockUsers = List.of(mockUser1, mockUser2);

    var mockCredential1 =
        UserLogin.of(
            10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword1");
    var mockCredential2 =
        UserLogin.of(
            20,
            AggregateReference.to(ANOTHER_USER_ID),
            "thomas.a@example.com",
            "encryptedPassword2");
    var mockUserCredentials = List.of(mockCredential1, mockCredential2);

    var mockUserInfo1 =
        UserInfoResponse.of(
            USER_ID,
            mockUser1.firstName(),
            mockUser1.lastName(),
            mockCredential1.email(),
            mockUser1.phoneNumber());
    var mockUserInfo2 =
        UserInfoResponse.of(
            ANOTHER_USER_ID,
            mockUser2.firstName(),
            mockUser2.lastName(),
            mockCredential2.email(),
            mockUser2.phoneNumber());
    var expectedUsersResp = List.of(mockUserInfo1, mockUserInfo2);

    when(userRepository.findAll()).thenReturn(mockUsers);
    when(authService.findUserCredentialsByUserIds(anyCollection())).thenReturn(mockUserCredentials);

    // Actual
    var actualUsersResp = userService.getUsers();

    // Assert
    assertEquals(expectedUsersResp, actualUsersResp);
  }

  @Test
  void whenGetUserByIdThenSuccess() {
    // Arrange
    var mockUser = User.of(USER_ID, "John", "Wick", "0999999999");
    var mockUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword");
    var expectedUserResp =
        UserInfoResponse.of(
            USER_ID,
            mockUser.firstName(),
            mockUser.lastName(),
            mockUserCredential.email(),
            mockUser.phoneNumber());
    when(authService.findUserCredentialByUserId(USER_ID)).thenReturn(mockUserCredential);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(mockUser));

    // Actual
    var actualUserResp = userService.getUserById(USER_ID);

    // Assert
    assertEquals(expectedUserResp, actualUserResp);
  }

  @Test
  void whenGetUserByIdButNotFoundThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> userService.getUserById(NOT_FOUND_USER_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
