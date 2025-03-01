package dev.fResult.goutTogether.auths;

import static dev.fResult.goutTogether.common.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import dev.fResult.goutTogether.auths.dtos.*;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.auths.services.AuthServiceImpl;
import dev.fResult.goutTogether.auths.services.TokenService;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.RefreshTokenExpiredException;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.users.entities.User;
import java.time.Instant;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  private final int USER_ID_1 = 1;
  private final int USER_ID_2 = 3;
  private final int USER_ID_3 = 5;
  private final int NOT_FOUND_USER_ID_1 = 88888;
  private final int NOT_FOUND_USER_ID_2 = 99999;
  private final List<Integer> USER_IDS = List.of(USER_ID_1, USER_ID_2, USER_ID_3);
  private final String TARGET_EMAIL = "target@email.com";
  private final String NOT_FOUND_EMAIL = "in_existing@email.com";
  private final String PASSWORD = "P@$$w0rd";

  private final int TOUR_COMPANY_ID = 1;
  private final int NOT_FOUND_TOUR_COMPANY_ID = 99999;
  private final String TARGET_USERNAME = "target_username";
  private final String NOT_FOUND_USERNAME = "in_existing_username";
  private final String NOT_MATCHED_PASSWORD = "wrong_password";

  private final String ENCRYPTED_PASSWORD =
      "$argon2id$v=19$m=16384,t=2,p=1$NDMxQRhx/yCgg2uNT4GMfA$DZEh4+dJNDDtv8DHxvw7Tm1rXSHWlG/Wxf8S/rrAtTI";
  @InjectMocks @Spy private AuthServiceImpl authService;
  @Mock private UserLoginRepository userLoginRepository;
  @Mock private TokenService tokenService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private TourCompanyLoginRepository tourCompanyLoginRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private RefreshTokenRepository refreshTokenRepository;

  private UserLogin buildUserLogin(int id, int userId) {
    return UserLogin.of(id, AggregateReference.to(userId), TARGET_EMAIL, ENCRYPTED_PASSWORD);
  }

  private TourCompanyLogin buildTourCompanyLogin(int id, int companyId) {
    return TourCompanyLogin.of(
        id, AggregateReference.to(companyId), TARGET_USERNAME, ENCRYPTED_PASSWORD);
  }

  @Nested
  class FindUserCredentialTest {
    private final List<Integer> SOME_NOT_FOUND_USER_IDS =
        List.of(NOT_FOUND_USER_ID_2, USER_ID_1, NOT_FOUND_USER_ID_1, USER_ID_3);

    @Test
    void byUserIds_ThenSuccess() {
      // Arrange
      final var mockUserLogin1 = buildUserLogin(1, USER_ID_1);
      final var mockUserLogin2 = buildUserLogin(2, USER_ID_2);
      final var mockUserLogin3 = buildUserLogin(3, USER_ID_3);
      final var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin2, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      final var actualFoundUserLogins = authService.getUserCredentialsByUserIds(USER_IDS);

      // Assert
      assertEquals(mockFoundUserLogins, actualFoundUserLogins);
    }

    @Test
    void byUserIds_ButSomeAreNotFound_ThenThrowEntityNotFoundException() {
      // Arrange
      final var expectedErrorMessage =
          String.format( // TODO: Refactor this part to an ErrorMessageHelper's method
              "%s ids [%s] not found",
              UserLogin.class.getSimpleName(),
              new HashSet<>(List.of(NOT_FOUND_USER_ID_2, NOT_FOUND_USER_ID_1))
                  .toString()
                  .replaceAll("[\\[\\]]", ""));
      final var mockUserLogin1 = buildUserLogin(1, USER_ID_1);
      final var mockUserLogin3 = buildUserLogin(3, USER_ID_3);
      final var mockFoundUserLogins = List.of(mockUserLogin1, mockUserLogin3);

      when(userLoginRepository.findByUserIdIn(anyList())).thenReturn(mockFoundUserLogins);

      // Actual
      final Executable actualExecutable =
          () -> authService.getUserCredentialsByUserIds(SOME_NOT_FOUND_USER_IDS);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void byUserId_ThenSuccess() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      final var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      when(userLoginRepository.findOneByUserId(userRef)).thenReturn(Optional.of(mockUserLogin));

      // Actual
      final var actualFoundUserLogin = authService.getUserCredentialByUserId(USER_ID_1);

      // Assert
      assertEquals(mockUserLogin, actualFoundUserLogin);
    }

    @Test
    void byUserId_ButNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              UserLogin.class.getSimpleName(), "userId", NOT_FOUND_USER_ID_1);
      AggregateReference<User, Integer> notFoundUserRef =
          AggregateReference.to(NOT_FOUND_USER_ID_1);
      when(userLoginRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable =
          () -> authService.getUserCredentialByUserId(NOT_FOUND_USER_ID_1);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void byEmail_ThenSuccess() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      when(userLoginRepository.findOneByEmail(TARGET_EMAIL)).thenReturn(Optional.of(mockUserLogin));

      // Actual
      final var actualFoundUserLogin = authService.findUserCredentialByEmail(TARGET_EMAIL);

      // Assert
      assertTrue(actualFoundUserLogin.isPresent());
      assertEquals(mockUserLogin, actualFoundUserLogin.get());
    }

    @Test
    void byEmail_ButNotFound_ThenReturnEmpty() {
      // Arrange
      when(userLoginRepository.findOneByEmail(NOT_FOUND_EMAIL)).thenReturn(Optional.empty());

      // Actual
      final var actualFoundUserLogin = authService.findUserCredentialByEmail(NOT_FOUND_EMAIL);

      // Assert
      assertTrue(actualFoundUserLogin.isEmpty());
    }

    @Test
    void byEmailAndPassword_ThenSuccess() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      when(userLoginRepository.findOneByEmail(TARGET_EMAIL)).thenReturn(Optional.of(mockUserLogin));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

      // Actual
      final var actualFoundUserLogin =
          authService.findUserCredentialByEmailAndPassword(TARGET_EMAIL, PASSWORD);

      // Assert
      assertEquals(Optional.of(mockUserLogin), actualFoundUserLogin);
    }

    @Test
    void byEmailAndPassword_ButNotFound_ThenReturnEmpty() {
      // Arrange
      when(userLoginRepository.findOneByEmail(TARGET_EMAIL)).thenReturn(Optional.empty());

      // Actual
      final var actualFoundUserLogin =
          authService.findUserCredentialByEmailAndPassword(TARGET_EMAIL, PASSWORD);

      // Assert
      assertEquals(Optional.empty(), actualFoundUserLogin);
    }

    @Test
    void byEmailAndPassword_ButPasswordNotMatched_ThenReturnEmpty() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      when(userLoginRepository.findOneByEmail(TARGET_EMAIL)).thenReturn(Optional.of(mockUserLogin));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      // Actual
      final var actualFoundUserLogin =
          authService.findUserCredentialByEmailAndPassword(TARGET_EMAIL, NOT_MATCHED_PASSWORD);

      // Assert
      assertEquals(Optional.empty(), actualFoundUserLogin);
    }
  }

  @Nested
  class CreateUserCredentialTest {
    @Test
    void thenSuccess() {
      // Arrange
      final var mockUserLoginToCreate = buildUserLogin(1, USER_ID_1);

      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(userLoginRepository.save(any(UserLogin.class))).thenReturn(mockUserLoginToCreate);

      // Actual
      final var actualCreatedUserLogin =
          authService.createUserCredential(USER_ID_1, TARGET_EMAIL, "password");

      // Assert
      assertEquals(ENCRYPTED_PASSWORD, mockUserLoginToCreate.password());
      assertEquals(mockUserLoginToCreate, actualCreatedUserLogin);
    }
  }

  @Nested
  class UpdateUserPasswordTest {
    private final String NEW_PASSWORD = "N3wP@$$w0rd";

    @Test
    void byUserId_ThenSuccess() {
      // Arrange
      final var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      final var mockUserLoginToUpdate = UserLogin.of(1, userRef, TARGET_EMAIL, ENCRYPTED_PASSWORD);

      doReturn(mockUserLoginToUpdate).when(authService).getUserCredentialByUserId(anyInt());
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(userLoginRepository.save(any(UserLogin.class))).thenReturn(mockUserLoginToUpdate);

      // Actual
      final var actualUpdatedUserLogin =
          authService.updateUserPasswordByUserId(USER_ID_1, PASSWORD, NEW_PASSWORD);

      // Assert
      assertEquals(ENCRYPTED_PASSWORD, mockUserLoginToUpdate.password());
      assertEquals(mockUserLoginToUpdate, actualUpdatedUserLogin);
    }

    @Test
    void byUserId_ButNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              UserLogin.class.getSimpleName(), "userId", NOT_FOUND_USER_ID_1);
      AggregateReference<User, Integer> notFoundUserRef =
          AggregateReference.to(NOT_FOUND_USER_ID_1);
      when(userLoginRepository.findOneByUserId(notFoundUserRef)).thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable =
          () -> authService.updateUserPasswordByUserId(NOT_FOUND_USER_ID_1, PASSWORD, NEW_PASSWORD);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void byUserId_ButPasswordNotMatched_ThenThrowException() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      final var expectedErrorMessage =
          String.format("%s password is in correct", User.class.getSimpleName());

      when(userLoginRepository.findOneByUserId(AggregateReference.to(USER_ID_1)))
          .thenReturn(Optional.of(mockUserLogin));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      // Actual
      final Executable actualExecutable =
          () ->
              authService.updateUserPasswordByUserId(USER_ID_1, NOT_MATCHED_PASSWORD, NEW_PASSWORD);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void byEmail_ThenSuccess() {
      // Arrange
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      final var expectedUpdatedUserLogin =
          UserLogin.of(1, AggregateReference.to(USER_ID_1), TARGET_EMAIL, "NewEncryptedPassword");

      doReturn(Optional.of(mockUserLogin))
          .when(authService)
          .findUserCredentialByEmailAndPassword(anyString(), anyString());
      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(userLoginRepository.save(any(UserLogin.class))).thenReturn(expectedUpdatedUserLogin);

      // Actual
      final var actualUpdatedUserLogin =
          authService.updateUserPasswordByEmail(TARGET_EMAIL, PASSWORD, NEW_PASSWORD);

      // Assert
      assertEquals(expectedUpdatedUserLogin, actualUpdatedUserLogin);
    }

    @Test
    void byEmail_ButPasswordIsIncorrect_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s's %s password is incorrect",
              UserLogin.class.getSimpleName(), User.class.getSimpleName());
      doReturn(Optional.empty())
          .when(authService)
          .findUserCredentialByEmailAndPassword(anyString(), anyString());

      // Actual
      final Executable actualExecutable =
          () -> authService.updateUserPasswordByEmail(TARGET_EMAIL, PASSWORD, NEW_PASSWORD);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class DeleteUserCredentialTest {
    @Test
    void byId_ThenSuccess() {
      // Arrange
      final var userRef = AggregateReference.<User, Integer>to(USER_ID_1);
      final var mockCredentialToDelete = buildUserLogin(1, USER_ID_1);
      doReturn(mockCredentialToDelete).when(authService).getUserCredentialByUserId(anyInt());
      doNothing().when(userLoginRepository).delete(any(UserLogin.class));

      // Actual
      final var actualDeleteResult = authService.deleteUserCredentialByUserId(USER_ID_1);

      // Assert
      verify(userLoginRepository, times(1)).delete(mockCredentialToDelete);
      assertTrue(actualDeleteResult);
    }

    @Test
    void byId_ButNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              UserLogin.class.getSimpleName(), "userId", NOT_FOUND_USER_ID_1);

      doAnswer(
              invocation -> {
                final var targetUserId = invocation.getArgument(0, Integer.class);
                throw new EntityNotFoundException(
                    String.format(
                        "%s with %s [%d] not found",
                        UserLogin.class.getSimpleName(), "userId", targetUserId));
              })
          .when(authService)
          .getUserCredentialByUserId(anyInt());

      // Actual
      final Executable actualExecutable =
          () -> authService.deleteUserCredentialByUserId(NOT_FOUND_USER_ID_1);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class FindTourCompanyCredentialTest {
    @Test
    void byUsername_ThenSuccess() {
      // Arrange
      final var mockTourCompanyLogin = buildTourCompanyLogin(1, TOUR_COMPANY_ID);
      when(tourCompanyLoginRepository.findOneByUsername(TARGET_USERNAME))
          .thenReturn(Optional.of(mockTourCompanyLogin));

      // Actual
      final var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByUsername(TARGET_USERNAME);

      // Assert
      assertTrue(actualFoundCompanyLogin.isPresent());
      assertEquals(mockTourCompanyLogin, actualFoundCompanyLogin.get());
    }

    @Test
    void byUsername_ButNotFound_ThenReturnEmpty() {
      // Arrange
      when(tourCompanyLoginRepository.findOneByUsername(NOT_FOUND_USERNAME))
          .thenReturn(Optional.empty());

      // Actual
      final var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByUsername(NOT_FOUND_USERNAME);

      // Assert
      assertTrue(actualFoundCompanyLogin.isEmpty());
    }

    @Test
    void byCompanyId_ThenSuccess() {
      // Arrange
      AggregateReference<TourCompany, Integer> tourCompanyRef =
          AggregateReference.to(TOUR_COMPANY_ID);
      final var mockTourCompanyLogin = buildTourCompanyLogin(1, TOUR_COMPANY_ID);

      when(tourCompanyLoginRepository.findOneByTourCompanyId(tourCompanyRef))
          .thenReturn(Optional.of(mockTourCompanyLogin));

      // Actual
      final var actualFoundCompanyLogin =
          authService.findTourCompanyCredentialByTourCompanyId(TOUR_COMPANY_ID);

      // Assert
      assertEquals(mockTourCompanyLogin, actualFoundCompanyLogin);
    }

    @Test
    void byCompanyId_ButNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              TourCompanyLogin.class.getSimpleName(), "tourCompanyId", NOT_FOUND_TOUR_COMPANY_ID);
      final AggregateReference<TourCompany, Integer> notFoundTourCompanyRef =
          AggregateReference.to(NOT_FOUND_TOUR_COMPANY_ID);

      when(tourCompanyLoginRepository.findOneByTourCompanyId(notFoundTourCompanyRef))
          .thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable =
          () -> authService.findTourCompanyCredentialByTourCompanyId(NOT_FOUND_TOUR_COMPANY_ID);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class CreateTourCompanyLoginTest {
    @Test
    void thenSuccess() {
      // Arrange
      final var companyRef = AggregateReference.<TourCompany, Integer>to(TOUR_COMPANY_ID);
      final var mockCompanyLoginToCreate = buildTourCompanyLogin(1, TOUR_COMPANY_ID);

      when(passwordEncoder.encode(anyString())).thenReturn(ENCRYPTED_PASSWORD);
      when(tourCompanyLoginRepository.save(any(TourCompanyLogin.class)))
          .thenReturn(mockCompanyLoginToCreate);

      // Actual
      final var actualCreatedUserLogin =
          authService.createTourCompanyLogin(TOUR_COMPANY_ID, "MyTour", "password");

      // Assert
      assertEquals(ENCRYPTED_PASSWORD, mockCompanyLoginToCreate.password());
      assertEquals(mockCompanyLoginToCreate, actualCreatedUserLogin);
    }
  }

  @Nested
  class DeleteCompanyLoginTest {
    @Test
    void thenSuccess() {
      // Arrange
      final var mockCompanyLoginToDelete = buildTourCompanyLogin(1, TOUR_COMPANY_ID);
      doReturn(mockCompanyLoginToDelete)
          .when(authService)
          .findTourCompanyCredentialByTourCompanyId(anyInt());

      // Actual
      final var actualDeleteResult =
          authService.deleteTourCompanyLoginByTourCompanyId(TOUR_COMPANY_ID);

      // Assert
      verify(tourCompanyLoginRepository, times(1)).delete(mockCompanyLoginToDelete);
      assertTrue(actualDeleteResult);
    }

    @Test
    void byId_ButNotFound_ThenThrowException() {
      // Arrange
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%d] not found",
              TourCompanyLogin.class.getSimpleName(), "tourCompanyId", NOT_FOUND_TOUR_COMPANY_ID);

      doAnswer(
              invocation -> {
                final var targetCompanyId = invocation.getArgument(0, Integer.class);
                throw new EntityNotFoundException(
                    String.format(
                        "%s with %s [%d] not found",
                        TourCompanyLogin.class.getSimpleName(), "tourCompanyId", targetCompanyId));
              })
          .when(authService)
          .findTourCompanyCredentialByTourCompanyId(anyInt());

      // Actual
      final Executable actualExecutable =
          () -> authService.deleteTourCompanyLoginByTourCompanyId(NOT_FOUND_TOUR_COMPANY_ID);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Test
  void whenLogin_ThenSuccess() {
    // Arrange
    final var ROLE = UserRoleName.CONSUMER;
    final var body = LoginRequest.of(TARGET_EMAIL, PASSWORD);
    final var refreshToken = UUIDV7.randomUUID().toString();
    final var accessToken = "access_token";
    final var authenticatedUser =
        AuthenticatedUser.of(USER_ID_1, TARGET_EMAIL, ENCRYPTED_PASSWORD, ROLE);
    final var mockAuthentication =
        new UsernamePasswordAuthenticationToken(authenticatedUser, PASSWORD);
    final var mockUserLogin = buildUserLogin(1, USER_ID_1);
    final var mockRefreshToken =
        RefreshToken.of(1, refreshToken, Instant.now(), ROLE, USER_ID_1, false);
    final var expectedLoggedInResp =
        LoginResponse.of(USER_ID_1, TOKEN_TYPE, accessToken, refreshToken);

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenReturn(mockAuthentication);
    when(tokenService.issueAccessToken(any(AuthenticatedUser.class), any(Instant.class)))
        .thenReturn(accessToken);
    when(tokenService.issueRefreshToken()).thenReturn(refreshToken);
    doReturn(true).when(authService).logout(any(AuthenticatedUser.class));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);

    // Actual
    final var actualLoginResponse = authService.login(body);

    // Assert
    assertEquals(mockUserLogin.userId().getId(), actualLoginResponse.userId());
    assertEquals(expectedLoggedInResp, actualLoginResponse);
  }

  @Nested
  class RefreshTokenTest {
    private final int REFRESH_TOKEN_ID = 1;
    private final String REFRESH_TOKEN = UUIDV7.randomUUID().toString();

    private RefreshToken buildCurrentRefreshToken(
        String refreshToken, UserRoleName role, int resourceId) {

      return RefreshToken.of(
          REFRESH_TOKEN_ID, refreshToken, Instant.now().minusSeconds(90), role, resourceId, false);
    }

    private RefreshToken buildRotatedRefreshToken(
        String refreshToken, UserRoleName role, int resourceId) {

      return RefreshToken.of(
          REFRESH_TOKEN_ID, refreshToken, Instant.now(), role, resourceId, false);
    }

    @Test
    void ofConsumerAndTokenIsRotated_ThenSuccess() {
      // Arrange
      final var ROLE = UserRoleName.CONSUMER;
      final var ROTATED_REFRESH_TOKEN = UUIDV7.randomUUID().toString();
      final var NEW_ACCESS_TOKEN = "new_access_token";
      final var body = RefreshTokenRequest.of(ROLE, USER_ID_1, REFRESH_TOKEN);
      final var mockCurrentRefreshToken = buildCurrentRefreshToken(REFRESH_TOKEN, ROLE, USER_ID_1);
      final var mockRotatedRefreshToken =
          buildRotatedRefreshToken(ROTATED_REFRESH_TOKEN, ROLE, USER_ID_1);
      final var mockUserLogin = buildUserLogin(1, USER_ID_1);
      final var expectedLoggedInResp =
          LoginResponse.of(USER_ID_1, TOKEN_TYPE, NEW_ACCESS_TOKEN, ROTATED_REFRESH_TOKEN);

      when(refreshTokenRepository.findOneByToken(anyString()))
          .thenReturn(Optional.of(mockCurrentRefreshToken));
      when(tokenService.isRefreshTokenExpired(any(RefreshToken.class))).thenReturn(false);
      doReturn(mockUserLogin).when(authService).getUserCredentialByUserId(anyInt());
      when(tokenService.issueAccessToken(any(UserLogin.class), any(Instant.class)))
          .thenReturn(NEW_ACCESS_TOKEN);
      when(refreshTokenRepository.save(any(RefreshToken.class)))
          .thenReturn(mockRotatedRefreshToken);
      when(tokenService.rotateRefreshTokenIfNeed(any(RefreshToken.class)))
          .thenReturn(ROTATED_REFRESH_TOKEN);

      // Actual
      final var actualRefreshedToken = authService.refreshToken(body);

      // Assert
      assertEquals(expectedLoggedInResp, actualRefreshedToken);
    }

    @Test
    void ofConsumer_ThenReturnCurrentRefreshToken() {
      // Arrange
      final var ROLE = UserRoleName.CONSUMER;
      final var NEW_ACCESS_TOKEN = "new_access_token";
      final var body = RefreshTokenRequest.of(ROLE, USER_ID_1, REFRESH_TOKEN);
      final var mockRefreshToken = buildRotatedRefreshToken(REFRESH_TOKEN, ROLE, USER_ID_1);
      final var mockUserLogin = buildUserLogin(2, USER_ID_2);
      final var expectedLoggedInResp =
          LoginResponse.of(USER_ID_1, TOKEN_TYPE, NEW_ACCESS_TOKEN, REFRESH_TOKEN);

      when(refreshTokenRepository.findOneByToken(anyString()))
          .thenReturn(Optional.of(mockRefreshToken));
      when(tokenService.isRefreshTokenExpired(any(RefreshToken.class))).thenReturn(false);
      doReturn(mockUserLogin).when(authService).getUserCredentialByUserId(anyInt());
      when(tokenService.issueAccessToken(any(UserLogin.class), any(Instant.class)))
          .thenReturn(NEW_ACCESS_TOKEN);
      when(tokenService.rotateRefreshTokenIfNeed(any(RefreshToken.class)))
          .thenReturn(REFRESH_TOKEN);

      // Actual
      final var actualRefreshedToken = authService.refreshToken(body);

      // Assert
      assertEquals(expectedLoggedInResp, actualRefreshedToken);
    }

    @Test
    void ofConsumer_ButRefreshTokenAlreadyExpired_ThenThrowException() {
      // Arrange
      final var ROLE = UserRoleName.CONSUMER;
      final var body = RefreshTokenRequest.of(ROLE, USER_ID_1, REFRESH_TOKEN);
      final var mockRefreshToken = buildCurrentRefreshToken(REFRESH_TOKEN, ROLE, USER_ID_1);
      final var expectedErrorMessage =
          String.format(
              "%s is already expired, please re-login", RefreshToken.class.getSimpleName());

      when(refreshTokenRepository.findOneByToken(anyString()))
          .thenReturn(Optional.of(mockRefreshToken));
      when(tokenService.isRefreshTokenExpired(any(RefreshToken.class))).thenReturn(true);
      doReturn(true).when(authService).logout(any(LogoutInfo.class));

      // Actual
      final Executable actualExecutable = () -> authService.refreshToken(body);

      // Assert
      final var exception =
          assertThrowsExactly(RefreshTokenExpiredException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }

    @Test
    void ofTourCompanyAndTokenIsRotated_ThenSuccess() {
      // Arrange
      final var ROLE = UserRoleName.COMPANY;
      final var ROTATED_REFRESH_TOKEN = UUIDV7.randomUUID().toString();
      final var NEW_ACCESS_TOKEN = "new_access_token";
      final var body = RefreshTokenRequest.of(ROLE, TOUR_COMPANY_ID, REFRESH_TOKEN);
      final var mockCurrentRefreshToken =
          buildCurrentRefreshToken(REFRESH_TOKEN, ROLE, TOUR_COMPANY_ID);
      final var mockRotatedRefreshToken =
          buildRotatedRefreshToken(ROTATED_REFRESH_TOKEN, ROLE, TOUR_COMPANY_ID);
      final var mockTourCompanyLogin = buildTourCompanyLogin(1, TOUR_COMPANY_ID);
      final var expectedLoggedInResp =
          LoginResponse.of(TOUR_COMPANY_ID, TOKEN_TYPE, NEW_ACCESS_TOKEN, ROTATED_REFRESH_TOKEN);

      when(refreshTokenRepository.findOneByToken(anyString()))
          .thenReturn(Optional.of(mockCurrentRefreshToken));
      when(tokenService.isRefreshTokenExpired(any(RefreshToken.class))).thenReturn(false);
      doReturn(mockTourCompanyLogin)
          .when(authService)
          .findTourCompanyCredentialByTourCompanyId(anyInt());
      when(tokenService.issueAccessToken(any(TourCompanyLogin.class), any(Instant.class)))
          .thenReturn(NEW_ACCESS_TOKEN);
      when(refreshTokenRepository.save(any(RefreshToken.class)))
          .thenReturn(mockRotatedRefreshToken);
      when(tokenService.rotateRefreshTokenIfNeed(any(RefreshToken.class)))
          .thenReturn(ROTATED_REFRESH_TOKEN);

      // Actual
      final var actualRefreshedToken = authService.refreshToken(body);

      // Assert
      assertEquals(expectedLoggedInResp, actualRefreshedToken);
    }

    @Test
    void butNotFound_ThenThrowException() {
      // Arrange
      final var ROLE = UserRoleName.CONSUMER;
      final var NOT_FOUND_REFRESH_TOKEN = "NOT_FOUND_TOKEN";
      final var body = RefreshTokenRequest.of(ROLE, USER_ID_1, NOT_FOUND_REFRESH_TOKEN);
      final var expectedErrorMessage =
          String.format(
              "%s with %s [%s] not found",
              RefreshToken.class.getSimpleName(), "token", NOT_FOUND_REFRESH_TOKEN);

      when(refreshTokenRepository.findOneByToken(anyString())).thenReturn(Optional.empty());

      // Actual
      final Executable actualExecutable = () -> authService.refreshToken(body);

      // Assert
      final var exception = assertThrowsExactly(EntityNotFoundException.class, actualExecutable);
      assertEquals(expectedErrorMessage, exception.getMessage());
    }
  }

  @Nested
  class LogoutTest {
    @Test
    void byAuthenticatedUser_ThenSuccess() {
      // Arrange
      doNothing()
          .when(refreshTokenRepository)
          .updateRefreshTokenByResource(any(UserRoleName.class), anyInt(), eq(true));

      // Actual
      final var actualLogoutResult =
          authService.logout(
              AuthenticatedUser.of(USER_ID_1, TARGET_EMAIL, PASSWORD, UserRoleName.ADMIN));

      // Assert
      assertTrue(actualLogoutResult);
    }

    @Test
    void byLogoutInfo_ThenSuccess() {
      // Arrange
      final var logoutInfoInput = LogoutInfo.of(USER_ID_1, UserRoleName.ADMIN.name());

      doNothing()
          .when(refreshTokenRepository)
          .updateRefreshTokenByResource(any(UserRoleName.class), anyInt(), eq(true));

      // Actual
      final var actualLogoutResult = authService.logout(logoutInfoInput);

      // Assert
      assertTrue(actualLogoutResult);
    }
  }
}
