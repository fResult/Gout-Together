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
    var mockUserPage = new PageImpl<User>(List.of(mockUser1, mockUser2));
    var mockUsersResp = List.of(mockUserInfo1, mockUserInfo2);
    var expectedUserInfoPage = new PageImpl<UserInfoResponse>(mockUsersResp);

    when(userRepository.findByFirstNameContaining(anyString(), any(Pageable.class)))
        .thenReturn(mockUserPage);
    when(authService.findUserCredentialsByUserIds(anyCollection())).thenReturn(mockUserCredentials);

    // Actual
    var actualUsersResp = userService.getUsersByFirstName("", PageRequest.of(0, 3));

    // Assert
    assertEquals(expectedUserInfoPage.getContent(), actualUsersResp.getContent());
  }

  @Test
  void whenGetUserByIdThenSuccess() {
    // Arrange
    var mockUser = User.of(USER_ID, "John", "Wick", "0999999999");
    var mockUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword");
    var expectedUserResp =
        UserInfoResponse.of(
            mockUser.id(),
            mockUser.firstName(),
            mockUser.lastName(),
            mockUserCredential.email(),
            mockUser.phoneNumber());
    when(authService.findUserCredentialByUserId(anyInt())).thenReturn(mockUserCredential);
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockUser));

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
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> userService.getUserById(NOT_FOUND_USER_ID);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenRegisterUserThenSuccess() {
    // Arrange
    var mockUserRef = AggregateReference.<User, Integer>to(USER_ID);
    var mockRoleRef = AggregateReference.<Role, Integer>to(UserRoleName.CONSUMER.getId());
    var body =
        UserRegistrationRequest.of("John", "Wick", "john.w@example.com", "password", "0999999999");
    var mockUserToRegister =
        User.of(USER_ID, body.firstName(), body.lastName(), body.phoneNumber());
    var mockUserCredential = UserLogin.of(10, mockUserRef, body.email(), "encryptedPassword");
    var expectedRegisteredUser =
        UserInfoResponse.of(
            mockUserToRegister.id(),
            body.firstName(),
            body.lastName(),
            body.email(),
            body.phoneNumber());
    var mockBoundUserRole = UserRole.of(1, mockUserRef, mockRoleRef);
    var mockCreatedWallet = UserWallet.of(1, mockUserRef, Instant.now(), BigDecimal.ZERO);

    when(authService.findUserCredentialByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(mockUserToRegister);
    when(roleService.bindNewUser(anyInt(), eq(UserRoleName.CONSUMER)))
        .thenReturn(mockBoundUserRole);
    when(authService.createUserCredential(anyInt(), anyString(), anyString()))
        .thenReturn(mockUserCredential);
    when(walletService.createConsumerWallet(expectedRegisteredUser.id()))
        .thenReturn(mockCreatedWallet);

    // Actual
    var actualRegisteredUser = userService.registerUser(body);

    // Assert
    assertEquals(expectedRegisteredUser, actualRegisteredUser);
  }

  @Test
  void whenRegisterUserButUserEmailExistsThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s email [%s] already exists", User.class.getSimpleName(), EXISTING_EMAIL);
    var body = UserRegistrationRequest.of("John", "Wick", EXISTING_EMAIL, "password", "0999999999");
    var existingUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), EXISTING_EMAIL, "encryptedPassword");
    when(authService.findUserCredentialByEmail(anyString()))
        .thenReturn(Optional.of(existingUserCredential));

    // Actual
    Executable actualExecutable = () -> userService.registerUser(body);

    // Assert
    var exception = assertThrowsExactly(CredentialExistsException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void whenUpdateUserByIdThenSuccess() {
    // Arrange
    var LAST_NAME_TO_UPDATE = "Constantine";
    var PHONE_NUMBER_TO_UPDATE = "0888888888";
    var body = UserUpdateRequest.of(null, LAST_NAME_TO_UPDATE, PHONE_NUMBER_TO_UPDATE);
    var mockExistingUser = User.of(USER_ID, "John", "Wick", "0999999999");
    var mockUpdatedUser =
        User.of(USER_ID, mockExistingUser.firstName(), LAST_NAME_TO_UPDATE, PHONE_NUMBER_TO_UPDATE);
    var mockUserCredential =
        UserLogin.of(10, AggregateReference.to(USER_ID), "john.w@example.com", "encryptedPassword");
    var expectUpdatedUser =
        UserInfoResponse.of(
            mockUpdatedUser.id(),
            mockUpdatedUser.firstName(),
            LAST_NAME_TO_UPDATE,
            mockUserCredential.email(),
            PHONE_NUMBER_TO_UPDATE);

    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockExistingUser));
    when(userRepository.save(any(User.class))).thenReturn(mockUpdatedUser);
    when(authService.findUserCredentialByUserId(anyInt())).thenReturn(mockUserCredential);

    // Actual
    var actualUpdatedUser = userService.updateUserById(USER_ID, body);

    // Assert
    assertEquals(expectUpdatedUser, actualUpdatedUser);
  }

  @Test
  void whenUpdateUserByIdButNotFoundThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    var body = UserUpdateRequest.of(null, "Constantine", "0888888888");
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> userService.updateUserById(NOT_FOUND_USER_ID, body);

    // Assert
    var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test()
  void whenChangePasswordByUserIdThenSuccess() {
    // Arrange
    var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    var mockUserCredential = buildUserCredential(USER_ID, EXISTING_EMAIL);
    var expectedChangedPassword = UpdatePasswordResult.SUCCESS;

    when(authService.updateUserPasswordByUserId(anyInt(), anyString(), anyString()))
        .thenReturn(mockUserCredential);

    // Actual
    var actualChangedPassword = userService.changePasswordByUserId(USER_ID, body);

    // Assert
    assertEquals(expectedChangedPassword, actualChangedPassword);
  }

  @Test()
  void whenChangePasswordByEmailThenSuccess() {
    // Arrange
    var body = UserChangePasswordRequest.of("0ldP@$$w0rd", "N3wP@$$w0rd");
    var mockUserCredential = buildUserCredential(USER_ID, EXISTING_EMAIL);
    var expectedChangedPassword = UpdatePasswordResult.SUCCESS;
    when(authService.updateUserPasswordByEmail(anyString(), anyString(), anyString()))
        .thenReturn(mockUserCredential);

    // Actual
    var actualChangedPassword = userService.changePasswordByEmail(EXISTING_EMAIL, body);

    // Assert
    assertEquals(expectedChangedPassword, actualChangedPassword);
  }

  @Test
  void whenDeleteUserByIdThenSuccess() {
    // Arrange
    var mockUserToDelete = User.of(USER_ID, "John", "Wick", "0999999999");
    var mockDeleteCredentialSuccess = true;
    var mockDeleteWalletWalletSuccess = true;

    when(userRepository.findById(anyInt())).thenReturn(Optional.of(mockUserToDelete));
    when(authService.deleteUserCredentialByUserId(anyInt()))
        .thenReturn(mockDeleteCredentialSuccess);
    when(walletService.deleteConsumerWalletByUserId(anyInt()))
        .thenReturn(mockDeleteWalletWalletSuccess);
    doNothing().when(userRepository).delete(any(User.class));

    // Actual
    var actualIsDeleteSuccess = userService.deleteUserById(USER_ID);

    // Assert
    verify(userRepository, times(1)).delete(mockUserToDelete);
    assertTrue(actualIsDeleteSuccess);
  }

  @Test
  void whenDeleteUserByIdButNotFoundThenThrowException() {
    // Arrange
    var expectedErrorMessage =
        String.format("%s id [%d] not found", User.class.getSimpleName(), NOT_FOUND_USER_ID);
    when(userRepository.findById(NOT_FOUND_USER_ID)).thenReturn(Optional.empty());

    // Actual
    Executable actualExecutable = () -> userService.deleteUserById(NOT_FOUND_USER_ID);

    // Assert
    var exception = assertThrows(EntityNotFoundException.class, actualExecutable);
    assertEquals(expectedErrorMessage, exception.getMessage());
  }
}
