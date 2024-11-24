package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.UserForgotPasswordRequest;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final AuthService authService;
  private final WalletService walletService;

  public UserServiceImpl(
      UserRepository userRepository, AuthService authService, WalletService walletService) {
    this.userRepository = userRepository;
    this.authService = authService;
    this.walletService = walletService;
  }

  @Override
  public List<UserInfoResponse> getUsers() {
    logger.debug("[getUsers] Getting all {}s", User.class.getSimpleName());
    var users = userRepository.findAll();
    var userIdToCredentialMap = buildUserIdToCredentialMap(users);
    var toResponse = UserInfoResponse.fromUserDaoWithUserCredentialMap(userIdToCredentialMap);

    return users.stream().map(toResponse).toList();
  }

  @Override
  public UserInfoResponse getUserById(int id) {
    return getUserInfoById(id)
        .orElseThrow(errorHelper.entityNotFound("getUserById", User.class, id));
  }

  @Override
  @Transactional
  public UserInfoResponse register(UserRegistrationRequest body) {
    logger.debug("[register] new {} is registering", User.class.getSimpleName());

    var existingUserCredential = authService.findUserCredentialByEmail(body.email());
    if (existingUserCredential.isPresent()) {
      logger.warn(
          "[register] {} email [{}] already exists", User.class.getSimpleName(), body.email());
      throw new CredentialExistsException(
          String.format(
              "%s email [%s] already exists", User.class.getSimpleName(), body.email()));
    }

    var userToRegister = User.of(null, body.firstName(), body.lastName(), body.phoneNumber());
    var registeredUser = userRepository.save(userToRegister);
    logger.info("[register] New {} is registered: {}", User.class.getSimpleName(), registeredUser);

    var createdUserCredential =
        authService.createUserLogin(registeredUser.id(), body.email(), body.password());
    walletService.createConsumerWallet(registeredUser.id());

    return UserInfoResponse.fromUserDao(registeredUser, createdUserCredential);
  }

  @Override
  public UserInfoResponse updateUserById(int id, UserUpdateRequest body) {
    var toUserUpdate = UserUpdateRequest.dtoToUserUpdate(body);
    var userToUpdate =
        userRepository
            .findById(id)
            .map(toUserUpdate)
            .orElseThrow(errorHelper.entityNotFound("updateUserById", User.class, id));
    var updatedUser = userRepository.save(userToUpdate);
    logger.info("[updateUserById] {}: {} is updated", User.class.getSimpleName(), updatedUser);

    return toResponseWithUserCredential(updatedUser);
  }

  // TODO: Delete User + Credential + Wallet (Cascade)
  @Override
  @Transactional
  public void deleteUserById(int id) {
    var userEntityName = User.class.getSimpleName();
    logger.debug("[deleteUser] {} id [{}] is deleting", userEntityName, id);
    getUserInfoById(id).orElseThrow(errorHelper.entityNotFound("deleteUser", User.class, id));

    userRepository.deleteById(id);
    logger.info("[deleteUser] {} id [{}] is deleted", userEntityName, id);
  }

  @Override
  public UpdatePasswordResult changePassword(UserForgotPasswordRequest body) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private Set<Integer> buildUniqueUserIds(List<User> users) {
    return users.stream().map(User::id).collect(Collectors.toSet());
  }

  private Map<Integer, UserLogin> buildUserIdToCredentialMap(List<User> users) {
    var userIds = buildUniqueUserIds(users);

    return authService.findUserCredentialsByUserIds(userIds).stream()
        .collect(Collectors.toMap(cred -> cred.userId().getId(), Function.identity()));
  }

  private Optional<UserInfoResponse> getUserInfoById(int id) {
    return userRepository
        .findById(id)
        .flatMap(opt -> Optional.of(this.toResponseWithUserCredential(opt)));
  }

  private UserInfoResponse toResponseWithUserCredential(User user) {
    var userCredential = authService.findUserCredentialByUserId(user.id());
    return UserInfoResponse.fromUserDao(user, userCredential);
  }
}
