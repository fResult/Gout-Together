package dev.fResult.goutTogether.users;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.Role;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.entities.UserRole;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.users.services.RoleService;
import dev.fResult.goutTogether.users.services.UserServiceImpl;
import dev.fResult.goutTogether.wallets.entities.UserWallet;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  private static final int USER_ID = 1;
  private static final int NOT_FOUND_USER_ID = 99999;
  private static final String EXISTING_EMAIL = "existing_email@example.com";

  @InjectMocks private UserServiceImpl userService;

  @Mock private UserRepository userRepository;
  @Mock private AuthService authService;
  @Mock private WalletService walletService;
  @Mock private RoleService roleService;

  private UserLogin buildUserCredential(int userId, String email) {
    return UserLogin.of(10, AggregateReference.to(userId), email, "encryptedPassword");
  }

  @Test
  void whenGetUsers_ThenSuccess() {
    // Arrange
    final var ANOTHER_USER_ID = 2;
    final var mockUser1 = User.of(USER_ID, "John", "Wick", "0999999999");
    final var mockUser2 = User.of(ANOTHER_USER_ID, "Thomas", "Anderson", "0666666666");
    final var mockUsers = List.of(mockUser1, mockUser2);

    final var mockCredential1 =
        UserLogin.of(
            10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword1");
    final var mockCredential2 =
        UserLogin.of(
            20,
            AggregateReference.to(ANOTHER_USER_ID),
            "thomas.a@example.com",
            "encryptedPassword2");
    final var mockUserCredentials = List.of(mockCredential1, mockCredential2);

    final var mockUserInfo1 =
        UserInfoResponse.of(
            USER_ID,
            mockUser1.firstName(),
            mockUser1.lastName(),
            mockCredential1.email(),
            mockUser1.phoneNumber());
    final var mockUserInfo2 =
        UserInfoResponse.of(
            ANOTHER_USER_ID,
            mockUser2.firstName(),
            mockUser2.lastName(),
            mockCredential2.email(),
            mockUser2.phoneNumber());
    final var mockUserPage = new PageImpl<User>(List.of(mockUser1, mockUser2));
    final var mockUsersResp = List.of(mockUserInfo1, mockUserInfo2);
    final var expectedUserInfoPage = new PageImpl<UserInfoResponse>(mockUsersResp);

    when(userRepository.findByFirstNameContaining(anyString(), any(Pageable.class)))
        .thenReturn(mockUserPage);
    when(authService.getUserCredentialsByUserIds(anyCollection())).thenReturn(mockUserCredentials);

    // Actual
    final var actualUsersResp = userService.getUsersByFirstName("", PageRequest.of(0, 3));

    // Assert
    assertEquals(expectedUserInfoPage.getContent(), actualUsersResp.getContent());
  }

  @Test
  void whenGetUserById_ThenSuccess() {
    // Arrange
    final var mockUser = User.of(USER_ID, "John", "Wick", "0999999999");
    final var mockUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword");
    final var expectedUserResp =
        UserInfoResponse.of(
            mockUser.id(),
            mockUser.firstName(),
            mockUser.lastName(),
            mockUserCredential.email(),
            mockUser.phoneNumber());
    when(authService.getUserCredentialByUserId(anyInt())).thenReturn(mockUserCredential);
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));

    // Actual
    final var actualUserResp = userService.getUserById(USER_ID);

    // Assert
    assertEquals(expectedUserResp, actualUserResp);
  }

  @Test
  void whenGetUserById_ButNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable = () -> userService.getUserById(NOT_FOUND_USER_ID);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenRegisterUser_ThenSuccess() {
    // Arrange
    final var mockUserRef = AggregateReference.<User, Integer>to(USER_ID);
    final var mockRoleRef = AggregateReference.<Role, Integer>to(UserRoleName.CONSUMER.getId());
    final var body =
        UserRegistrationRequest.of("John", "Wick", "john.w@example.com", "password", "0999999999");
    final var mockUserToRegister =
        User.of(USER_ID, body.firstName(), body.lastName(), body.phoneNumber());
    final var mockUserCredential = UserLogin.of(10, mockUserRef, body.email(), "encryptedPassword");
    final var expectedRegisteredUser =
        UserInfoResponse.of(
            mockUserToRegister.id(),
            body.firstName(),
            body.lastName(),
            body.email(),
            body.phoneNumber());
    final var mockBoundUserRole = UserRole.of(1, mockUserRef, mockRoleRef);
    final var mockCreatedWallet = UserWallet.of(1, mockUserRef, Instant.now(), BigDecimal.ZERO);

    when(authService.findUserCredentialByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(mockUserToRegister);
    when(roleService.bindNewUser(anyInt(), eq(UserRoleName.CONSUMER)))
        .thenReturn(mockBoundUserRole);
    when(authService.createUserCredential(anyInt(), anyString(), anyString()))
        .thenReturn(mockUserCredential);
    when(walletService.createConsumerWallet(expectedRegisteredUser.id()))
        .thenReturn(mockCreatedWallet);

    // Actual
    final var actualRegisteredUser = userService.registerUser(body);

    // Assert
    assertEquals(expectedRegisteredUser, actualRegisteredUser);
  }

  @Test
  void whenRegisterUser_ButUserEmailExists_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format("%s email [%s] already exists", User.class.getSimpleName(), EXISTING_EMAIL);
    final var body =
        UserRegistrationRequest.of("John", "Wick", EXISTING_EMAIL, "password", "0999999999");
    final var existingUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), EXISTING_EMAIL, "encryptedPassword");

    when(authService.findUserCredentialByEmail(anyString()))
        .thenReturn(Optional.of(existingUserCredential));

    // Actual
    final Executable actualExecutable = () -> userService.registerUser(body);

    // Assert
    final var exception = assertThrowsExactly(CredentialExistsException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenUpdateUserById_ThenSuccess() {
    // Arrange
    final var LAST_NAME_TO_UPDATE = "Constantine";
    final var PHONE_NUMBER_TO_UPDATE = "0888888888";
    final var body = UserUpdateRequest.of(null, LAST_NAME_TO_UPDATE, PHONE_NUMBER_TO_UPDATE);
    final var mockExistingUser = User.of(USER_ID, "John", "Wick", "0999999999");
    final var mockUpdatedUser =
        User.of(USER_ID, mockExistingUser.firstName(), LAST_NAME_TO_UPDATE, PHONE_NUMBER_TO_UPDATE);
    final var mockUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword");
    final var expectUpdatedUser =
        UserInfoResponse.of(
            mockUpdatedUser.id(),
            mockUpdatedUser.firstName(),
            LAST_NAME_TO_UPDATE,
            mockUserCredential.email(),
            PHONE_NUMBER_TO_UPDATE);

    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockExistingUser));
    when(userRepository.save(any(User.class))).thenReturn(mockUpdatedUser);
    when(authService.getUserCredentialByUserId(anyInt())).thenReturn(mockUserCredential);

    // Actual
    final var actualUpdatedUser = userService.updateUserById(USER_ID, body);

    // Assert
    assertEquals(expectUpdatedUser, actualUpdatedUser);
  }

  @Test
  void whenUpdateUserById_ButNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    final var body = UserUpdateRequest.of(null, "Constantine", "0888888888");

    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable = () -> userService.updateUserById(NOT_FOUND_USER_ID, body);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test()
  void whenChangePasswordByUserId_ThenSuccess() {
    // Arrange
    final var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    final var mockUserCredential = buildUserCredential(USER_ID, EXISTING_EMAIL);
    final var expectedChangedPassword = UpdatePasswordResult.SUCCESS;

    when(authService.updateUserPasswordByUserId(anyInt(), anyString(), anyString()))
        .thenReturn(mockUserCredential);

    // Actual
    final var actualChangedPassword = userService.changePasswordByUserId(USER_ID, body);

    // Assert
    assertEquals(expectedChangedPassword, actualChangedPassword);
  }

  @Test()
  void whenChangePasswordByUserId_ButUserNotFound_ThenReturn404() {
    // Arrange
    final var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    final var expectedErrorMessage =
        String.format("%s password is in correct", User.class.getSimpleName());

    when(authService.updateUserPasswordByUserId(anyInt(), anyString(), anyString()))
        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

    // Actual
    final Executable actualExecutable = () -> userService.changePasswordByUserId(USER_ID, body);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test()
  void whenChangePasswordByEmail_ThenSuccess() {
    // Arrange
    final var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    final var mockUserCredential = buildUserCredential(USER_ID, EXISTING_EMAIL);
    final var expectedChangedPassword = UpdatePasswordResult.SUCCESS;

    when(authService.updateUserPasswordByEmail(anyString(), anyString(), anyString()))
        .thenReturn(mockUserCredential);

    // Actual
    final var actualChangedPassword = userService.changePasswordByEmail(EXISTING_EMAIL, body);

    // Assert
    assertEquals(expectedChangedPassword, actualChangedPassword);
  }

  @Test()
  void whenChangePasswordByEmail_ButUserNotFound_ThenReturn404() {
    // Arrange
    final var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    final var expectedErrorMessage =
        String.format("%s password is in correct", User.class.getSimpleName());

    when(authService.updateUserPasswordByEmail(anyString(), anyString(), anyString()))
        .thenThrow(new EntityNotFoundException(expectedErrorMessage));

    // Actual
    final Executable actualExecutable = () -> userService.changePasswordByEmail(EXISTING_EMAIL, body);

    // Assert
    final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenDeleteUserById_ThenSuccess() {
    // Arrange
    final var mockUserToDelete = User.of(USER_ID, "John", "Wick", "0999999999");
    final var mockDeleteCredentialSuccess = true;
    final var mockDeleteWalletWalletSuccess = true;

    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockUserToDelete));
    when(authService.deleteUserCredentialByUserId(anyInt()))
        .thenReturn(mockDeleteCredentialSuccess);
    when(walletService.deleteConsumerWalletByUserId(anyInt()))
        .thenReturn(mockDeleteWalletWalletSuccess);
    doNothing().when(userRepository).delete(any(User.class));

    // Actual
    final var actualIsDeleteSuccess = userService.deleteUserById(USER_ID);

    // Assert
    verify(userRepository, times(1)).delete(mockUserToDelete);
    assertTrue(actualIsDeleteSuccess);
  }

  @Test
  void whenDeleteUserById_ButNotFound_ThenThrowException() {
    // Arrange
    final var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

    // Actual
    final Executable actualExecutable = () -> userService.deleteUserById(NOT_FOUND_USER_ID);

    // Assert
    final var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
