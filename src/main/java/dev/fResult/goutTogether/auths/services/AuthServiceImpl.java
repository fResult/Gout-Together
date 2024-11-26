package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.UserLoginRepository;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(AuthServiceImpl.class);

  private final UserLoginRepository userLoginRepository;
  private final TourCompanyLoginRepository tourCompanyLoginRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthServiceImpl(
      UserLoginRepository userLoginRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository,
      PasswordEncoder passwordEncoder) {
    this.userLoginRepository = userLoginRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
    this.passwordEncoder = passwordEncoder;
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
    logger.debug("[findUserCredentialByUserId] Finding {} by id: {}", UserLogin.class, userId);

    return userLoginRepository
        .findOneByUserId(AggregateReference.to(userId))
        .orElseThrow(
            errorHelper.entityNotFound("findUserCredentialByUserId", UserLogin.class, userId));
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
  public TourCompanyLogin findTourCompanyCredentialByTourCompanyId(int id) {
    logger.debug(
        "[findTourCompanyCredentialByUsername] Finding {} by id: {}", TourCompanyLogin.class, id);

    return tourCompanyLoginRepository
        .findById(id)
        .orElseThrow(
            errorHelper.entityNotFound(
                "findTourCompanyCredentialByUsername", TourCompanyLogin.class, id));
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

    return companyCredentialToCreate;
  }

  @Override
  public boolean deleteUserCredentialById(int id) {
    logger.debug(
        "[deleteUserCredentialById] Deleting {} by id: {}", UserLogin.class.getSimpleName(), id);
    var credentialToDelete = findUserCredentialByUserId(id);

    userLoginRepository.delete(credentialToDelete);
    logger.info(
        "[deleteUserCredentialById] {} id [{}] is deleted", UserLogin.class.getSimpleName(), id);

    return true;
  }

  private void throwExceptionIfSomeUserIdsNotFound(
      Collection<Integer> userIds, List<UserLogin> foundCredentials) {
    var foundCredentialUserIds =
        foundCredentials.stream()
            .map(credential -> credential.userId().getId())
            .collect(Collectors.toSet());

    // TODO: Refactor this part
    var userIdsToFind = new HashSet<>(Set.copyOf(userIds));
    userIdsToFind.removeAll(foundCredentialUserIds);
    var notFoundUserIds = new HashSet<>(userIdsToFind);

    if (!userIdsToFind.isEmpty())
      throw errorHelper
          .someEntitiesMissing("findUserCredentialsByUserIds", UserLogin.class, notFoundUserIds)
          .get();
  }
}
