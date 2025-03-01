package dev.fResult.goutTogether.auths.services;

import static dev.fResult.goutTogether.common.Constants.*;
import static java.util.function.Predicate.not;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.dtos.LogoutInfo;
import dev.fResult.goutTogether.auths.dtos.RefreshTokenRequest;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.EntityNotFoundException;
import dev.fResult.goutTogether.common.exceptions.RefreshTokenExpiredException;
import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.users.entities.User;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
  private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(AuthServiceImpl.class);

  private final UserLoginRepository userLoginRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  public AuthServiceImpl(
      UserLoginRepository userLoginRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository,
      TokenService tokenService,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      RefreshTokenRepository refreshTokenRepository) {

    this.userLoginRepository = userLoginRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    this.tokenService = tokenService;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public List<UserLogin> getUserCredentialsByUserIds(Collection<Integer> userIds) {
    logger.debug(
        "[findUserCredentialsByUserIds] Finding {}s by userIds: {}", UserLogin.class, userIds);
    final var foundCredentials = userLoginRepository.findByUserIdIn(userIds);

    throwExceptionIfSomeUserIdsNotFound(userIds, foundCredentials);

    logger.info(
        "[findUserCredentialsByUserIds] Found {} {}s",
        foundCredentials.size(),
        UserLogin.class.getSimpleName());

    return foundCredentials;
  }

  @Override
  public UserLogin getUserCredentialByUserId(int userId) {
    logger.debug(
        "[findUserCredentialByUserId] Finding {} by {}: {}", UserLogin.class, "userId", userId);

    return userLoginRepository
        .findOneByUserId(AggregateReference.to(userId))
        .orElseThrow(
            errorHelper.entityWithSubResourceNotFound(
                "findUserCredentialByUserId", UserLogin.class, "userId", String.valueOf(userId)));
  }

  @Override
  public Optional<UserLogin> findUserCredentialByEmail(String userEmail) {
    return userLoginRepository.findOneByEmail(userEmail);
  }

  @Override
  public Optional<UserLogin> findUserCredentialByEmailAndPassword(
      String userEmail, String password) {

    final var credentialToCheck = userLoginRepository.findOneByEmail(userEmail);
    final Predicate<UserLogin> checkUserPassword =
        credential -> passwordEncoder.matches(password, credential.password());

    return credentialToCheck.filter(checkUserPassword);
  }

  @Override
  public UserLogin createUserCredential(int userId, String email, String password) {
    logger.debug(
        "[createUserLogin] Creating new {} for userId: {}",
        UserLogin.class.getSimpleName(),
        userId);
    final var encryptedPassword = passwordEncoder.encode(password);

    final var userCredentialToCreate =
        new UserLogin(null, AggregateReference.to(userId), email, encryptedPassword);
    final var createdUserLogin = userLoginRepository.save(userCredentialToCreate);
    logger.info(
        "[createUserLogin] New {} is created: {}",
        UserLogin.class.getSimpleName(),
        createdUserLogin);

    return createdUserLogin;
  }

  @Override
  public UserLogin updateUserPasswordByUserId(int userId, String oldPassword, String newPassword) {
    final Predicate<UserLogin> checkUserPassword =
        credential -> passwordEncoder.matches(oldPassword, credential.password());
    final var userCredential =
        Optional.of(getUserCredentialByUserId(userId))
            .filter(checkUserPassword)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        String.format("%s password is in correct", User.class.getSimpleName())));

    final var passwordToUpdate = passwordEncoder.encode(newPassword);
    final var credentialToUpdate =
        UserLogin.of(
            userCredential.id(), userCredential.userId(), userCredential.email(), passwordToUpdate);

    return userLoginRepository.save(credentialToUpdate);
  }

  @Override
  public UserLogin updateUserPasswordByEmail(String email, String oldPassword, String newPassword) {
    final var userCredential =
        findUserCredentialByEmailAndPassword(email, oldPassword)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        String.format(
                            "%s's %s password is incorrect",
                            UserLogin.class.getSimpleName(), User.class.getSimpleName())));
    final var passwordToUpdate = passwordEncoder.encode(newPassword);
    final var credentialToUpdate =
        UserLogin.of(userCredential.id(), userCredential.userId(), email, passwordToUpdate);

    return userLoginRepository.save(credentialToUpdate);
  }

  @Override
  public boolean deleteUserCredentialByUserId(int userId) {
    logger.debug(
        "[deleteUserCredentialById] Deleting {} by id: {}",
        UserLogin.class.getSimpleName(),
        userId);
    final var credentialToDelete = getUserCredentialByUserId(userId);

    userLoginRepository.delete(credentialToDelete);
    logger.info(
        "[deleteUserCredentialById] {} id [{}] is deleted",
        UserLogin.class.getSimpleName(),
        userId);

    return true;
  }

  @Override
  public Optional<TourCompanyLogin> findTourCompanyCredentialByUsername(String username) {
    logger.debug(
        "[findTourCompanyCredentialByUsername] Finding {} by username: {}",
        TourCompanyLogin.class,
        username);

    return tourCompanyLoginRepository.findOneByUsername(username);
  }

  @Override
  public TourCompanyLogin findTourCompanyCredentialByTourCompanyId(int tourCompanyId) {
    logger.debug(
        "[findTourCompanyCredentialByUsername] Finding {} by {}: {}",
        TourCompanyLogin.class,
        "tourCompanyId",
        tourCompanyId);

    return tourCompanyLoginRepository
        .findOneByTourCompanyId(AggregateReference.to(tourCompanyId))
        .orElseThrow(
            errorHelper.entityWithSubResourceNotFound(
                "findTourCompanyCredentialByUsername",
                TourCompanyLogin.class,
                "tourCompanyId",
                String.valueOf(tourCompanyId)));
  }

  @Override
  public TourCompanyLogin createTourCompanyLogin(
      int tourCompanyId, String username, String password) {

    logger.debug(
        "[createTourCompanyLogin] Creating new {} for tourCompanyId: {}",
        TourCompanyLogin.class.getSimpleName(),
        tourCompanyId);

    final AggregateReference<TourCompany, Integer> companyRef =
        AggregateReference.to(tourCompanyId);
    final var encryptedPassword = passwordEncoder.encode(password);
    final var companyCredentialToCreate =
        TourCompanyLogin.of(null, companyRef, username, encryptedPassword);

    final var createdCompanyCredential = tourCompanyLoginRepository.save(companyCredentialToCreate);
    logger.info(
        "[createTourCompanyLogin] New {} is created: {}",
        TourCompanyLogin.class.getSimpleName(),
        createdCompanyCredential);

    return createdCompanyCredential;
  }

  @Override
  public boolean deleteTourCompanyLoginByTourCompanyId(int id) {
    logger.debug(
        "[deleteTourCompanyLoginById] Deleting {} by id: {}",
        TourCompanyLogin.class.getSimpleName(),
        id);
    final var credentialToDelete = findTourCompanyCredentialByTourCompanyId(id);

    tourCompanyLoginRepository.delete(credentialToDelete);
    logger.info(
        "[deleteTourCompanyLoginById] {} id [{}] is deleted",
        TourCompanyLogin.class.getSimpleName(),
        id);

    return true;
  }

  @Override
  @Transactional
  public LoginResponse login(LoginRequest body) {
    logger.debug("[login] Logging in by username [{}]", body.username());
    final var authInfo = new UsernamePasswordAuthenticationToken(body.username(), body.password());
    final var authentication = authenticationManager.authenticate(authInfo);
    final var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

    final var now = Instant.now();
    final var accessToken = tokenService.issueAccessToken(authenticatedUser, now);
    final var refreshToken = tokenService.issueRefreshToken();

    logout(authenticatedUser);

    final var refreshTokenToCreate =
        RefreshToken.of(
            null,
            refreshToken,
            now,
            authenticatedUser.roleName(),
            authenticatedUser.userId(),
            false);

    refreshTokenRepository.save(refreshTokenToCreate);

    final var loggedInInfo =
        LoginResponse.of(authenticatedUser.userId(), TOKEN_TYPE, accessToken, refreshToken);
    logger.info("[login] {} is logged in: {}", UserLogin.class.getSimpleName(), loggedInInfo);

    return loggedInInfo;
  }

  @Override
  @Transactional
  public LoginResponse refreshToken(RefreshTokenRequest body) {
    logger.debug(
        "[refreshToken] {} token [{}] is refreshing",
        RefreshToken.class.getSimpleName(),
        body.refreshToken());
    final var refreshToken =
        refreshTokenRepository
            .findOneByToken(body.refreshToken())
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "refreshToken", RefreshToken.class, "token", body.refreshToken()));

    final var resourceId = body.resourceId();
    final var refreshTokenExpired = tokenService.isRefreshTokenExpired(refreshToken);
    throwExceptionIfRefreshTokenExpired(refreshTokenExpired, refreshToken, resourceId);

    final var refreshedAccessToken =
        switch (body.usage()) {
          case UserRoleName.COMPANY -> issueTourCompanyAccessToken(resourceId);
          case UserRoleName.ADMIN, UserRoleName.CONSUMER -> issueUserAccessToken(resourceId);
        };

    final var refreshTokenRotation = tokenService.rotateRefreshTokenIfNeed(refreshToken);

    if (!refreshTokenRotation.equals(refreshToken.token())) {
      final var refreshTokenToBeExpired =
          RefreshToken.of(
              refreshToken.id(),
              refreshToken.token(),
              refreshToken.issuedDate(),
              refreshToken.usage(),
              refreshToken.resourceId(),
              true);
      refreshTokenRepository.save(refreshTokenToBeExpired);

      final var newRefreshToken =
          RefreshToken.of(
              null,
              refreshTokenRotation,
              Instant.now(),
              refreshToken.usage(),
              refreshToken.resourceId(),
              false);
      final var rotatedRefreshToken = refreshTokenRepository.save(newRefreshToken);

      return LoginResponse.of(
          rotatedRefreshToken.resourceId(), TOKEN_TYPE, refreshedAccessToken, refreshTokenRotation);
    }

    return LoginResponse.of(
        refreshToken.resourceId(), TOKEN_TYPE, refreshedAccessToken, refreshToken.token());
  }

  @Override
  public boolean logout(AuthenticatedUser authenticatedUser) {
    logger.debug("[logout] Logging out by username [{}]", authenticatedUser.email());

    refreshTokenRepository.updateRefreshTokenByResource(
        authenticatedUser.roleName(), authenticatedUser.userId(), true);

    logger.info("[logout] username [{}]'s tokens all are expired", authenticatedUser.email());

    return true;
  }

  @Override
  public boolean logout(LogoutInfo logoutInfo) {
    logger.debug("[logout] Logging out by resourceId [{}]", logoutInfo.resourceId());

    final var roleName = UserRoleName.valueOf(logoutInfo.roles());
    refreshTokenRepository.updateRefreshTokenByResource(roleName, logoutInfo.resourceId(), true);

    logger.info(
        "[logout] {} resourceId [{}]'s tokens all are expired",
        RefreshToken.class.getSimpleName(),
        logoutInfo.resourceId());

    return true;
  }

  private void throwExceptionIfSomeUserIdsNotFound(
      Collection<Integer> userIdsToFind, List<UserLogin> foundCredentials) {
    final var foundCredentialUserIds =
        foundCredentials.stream()
            .map(credential -> credential.userId().getId())
            .collect(Collectors.toMap(Function.identity(), x -> true));

    Predicate<Integer> foundUserIdInDb = id -> foundCredentialUserIds.getOrDefault(id, false);

    final var notFoundUserIds =
        userIdsToFind.stream().filter(not(foundUserIdInDb)).collect(Collectors.toUnmodifiableSet());

    if (!notFoundUserIds.isEmpty())
      throw errorHelper
          .someEntitiesMissing("findUserCredentialsByUserIds", UserLogin.class, notFoundUserIds)
          .get();
  }

  private void throwExceptionIfRefreshTokenExpired(
      boolean refreshTokenExpired, RefreshToken refreshToken, Integer resourceId) {

    if (refreshTokenExpired) {
      logger.info(
          "[refreshToken] {} token [{}] is already expired, please re-login",
          refreshToken.token(),
          RefreshToken.class.getSimpleName());
      final var logoutInfo = LogoutInfo.of(resourceId, refreshToken.usage().name());
      logout(logoutInfo);

      logger.warn("[refreshToken] {} is already expired, please re-login", refreshToken.token());
      throw new RefreshTokenExpiredException(
          String.format(
              "%s is already expired, please re-login", RefreshToken.class.getSimpleName()));
    }
  }

  private String issueUserAccessToken(int userId) {
    final var userCredential = getUserCredentialByUserId(userId);
    return tokenService.issueAccessToken(userCredential, Instant.now());
  }

  private String issueTourCompanyAccessToken(int tourCompanyId) {
    final var companyCredential = findTourCompanyCredentialByTourCompanyId(tourCompanyId);
    return tokenService.issueAccessToken(companyCredential, Instant.now());
  }
}
