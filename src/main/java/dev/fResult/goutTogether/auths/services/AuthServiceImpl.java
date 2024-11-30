package dev.fResult.goutTogether.auths.services;

import static dev.fResult.goutTogether.common.Constants.TOKEN_TYPE;
import static java.util.function.Predicate.not;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.RefreshTokenRepository;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
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
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;

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
  public List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds) {
    logger.debug(
        "[findUserCredentialsByUserIds] Finding {}s by userIds: {}", UserLogin.class, userIds);
    var foundCredentials = userLoginRepository.findByUserIdIn(userIds);

    throwExceptionIfSomeUserIdsNotFound(userIds, foundCredentials);

    logger.info(
        "[findUserCredentialsByUserIds] Found {} {}s",
        foundCredentials.size(),
        UserLogin.class.getSimpleName());

    return foundCredentials;
  }

  @Override
  public UserLogin findUserCredentialByUserId(int userId) {
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
  public UserLogin createUserCredential(int userId, String email, String password) {
    logger.debug(
        "[createUserLogin] Creating new {} for userId: {}",
        UserLogin.class.getSimpleName(),
        userId);
    var encryptedPassword = passwordEncoder.encode(password);

    var userCredentialToCreate =
        new UserLogin(null, AggregateReference.to(userId), email, encryptedPassword);
    var createdUserLogin = userLoginRepository.save(userCredentialToCreate);
    logger.info(
        "[createUserLogin] New {} is created: {}",
        UserLogin.class.getSimpleName(),
        createdUserLogin);

    return createdUserLogin;
  }

  @Override
  public boolean deleteUserCredentialByUserId(int userId) {
    logger.debug(
        "[deleteUserCredentialById] Deleting {} by id: {}",
        UserLogin.class.getSimpleName(),
        userId);
    var credentialToDelete = findUserCredentialByUserId(userId);

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

    AggregateReference<TourCompany, Integer> companyReference =
        AggregateReference.to(tourCompanyId);
    var encryptedPassword = passwordEncoder.encode(password);
    var companyCredentialToCreate =
        TourCompanyLogin.of(null, companyReference, username, encryptedPassword);

    var createdCompanyCredential = tourCompanyLoginRepository.save(companyCredentialToCreate);
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
    var credentialToDelete = findTourCompanyCredentialByTourCompanyId(id);

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
    var authInfo = new UsernamePasswordAuthenticationToken(body.username(), body.password());
    var authentication = authenticationManager.authenticate(authInfo);
    var authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

    var now = Instant.now();
    var accessToken = tokenService.issueAccessToken(authentication, now);
    var refreshToken = tokenService.issueRefreshToken(authentication, now);

    refreshTokenRepository.updateRefreshTokenByResource(
        authenticatedUser.roleName(), authenticatedUser.userId(), true);

    var refreshTokenToCreate =
        RefreshToken.of(
            null,
            refreshToken,
            now,
            authenticatedUser.roleName(),
            authenticatedUser.userId(),
            false);

    refreshTokenRepository.save(refreshTokenToCreate);

    var loggedInInfo =
        LoginResponse.of(authenticatedUser.userId(), TOKEN_TYPE, accessToken, refreshToken);
    logger.info("[login] {} is logged in", loggedInInfo);

    return loggedInInfo;
  }

  private void throwExceptionIfSomeUserIdsNotFound(
      Collection<Integer> userIdsToFind, List<UserLogin> foundCredentials) {
    var foundCredentialUserIds =
        foundCredentials.stream()
            .map(credential -> credential.userId().getId())
            .collect(Collectors.toMap(Function.identity(), x -> true));

    Predicate<Integer> foundUserIdInDb = id -> foundCredentialUserIds.getOrDefault(id, false);

    var notFoundUserIds =
        userIdsToFind.stream().filter(not(foundUserIdInDb)).collect(Collectors.toUnmodifiableSet());

    if (!notFoundUserIds.isEmpty())
      throw errorHelper
          .someEntitiesMissing("findUserCredentialsByUserIds", UserLogin.class, notFoundUserIds)
          .get();
  }
}
