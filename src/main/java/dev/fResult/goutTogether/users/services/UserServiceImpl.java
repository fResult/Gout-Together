package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.dtos.UserChangePasswordRequest;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.services.AuthService;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.exceptions.CredentialExistsException;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
  private final ErrorHelper errorHelper = new ErrorHelper(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final AuthService authService;
  private final WalletService walletService;
  private final RoleService roleService;

  public UserServiceImpl(
      UserRepository userRepository,
      AuthService authService,
      WalletService walletService,
      RoleService roleService) {
    this.userRepository = userRepository;
    this.authService = authService;
    this.walletService = walletService;
    this.roleService = roleService;
  }

  public Page<UserInfoResponse> getUsersByFirstName(String keyword, Pageable pageable) {
    logger.debug("[getUsers] Getting all {}s", User.class.getSimpleName());
    logger.info("By First Name: {}", keyword);
    var userPage = userRepository.findByFirstNameContaining(keyword, pageable);
    var userIdToCredentialMap = buildUserIdToCredentialMap(userPage);
    var toResponse = UserInfoResponse.fromUserDaoWithUserCredentialMap(userIdToCredentialMap);
    var userInfos = userPage.stream().map(toResponse).toList();

    return new PageImpl<>(userInfos);
  }

  @Override
  public UserInfoResponse getUserById(int id) {
    return userRepository
        .findById(id)
        .map(this::toResponseWithUserCredential)
        .orElseThrow(errorHelper.entityNotFound("getUserById", User.class, id));
  }

  @Override
  @Transactional
  public UserInfoResponse registerUser(UserRegistrationRequest body) {
    logger.debug("[register] New {} is registering", User.class.getSimpleName());
    throwExceptionIfUserEmailAlreadyExists(body.email());

    var userToRegister = User.of(null, body.firstName(), body.lastName(), body.phoneNumber());
    var registeredUser = userRepository.save(userToRegister);
    logger.info("[register] New {} is registered: {}", User.class.getSimpleName(), registeredUser);

    var boundUserRole = roleService.bindNewUser(registeredUser.id(), UserRoleName.CONSUMER);
    var createdUserCredential =
        authService.createUserCredential(registeredUser.id(), body.email(), body.password());
    walletService.createConsumerWallet(registeredUser.id());

    return UserInfoResponse.fromUserDao(registeredUser, createdUserCredential);
  }

  @Override
  public UserInfoResponse updateUserById(int id, UserUpdateRequest body) {
    logger.debug("[updateUserById] {} id {} is updating", User.class.getSimpleName(), id);

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

  @Override
  public UpdatePasswordResult changePasswordByUserId(int id, UserChangePasswordRequest body) {
    var updatedUserCredential =
        authService.updateUserPasswordByUserId(id, body.oldPassword(), body.newPassword());
    logger.info(
        "[changePasswordByUserId] {} id [{}] is updated",
        UserLogin.class.getSimpleName(),
        updatedUserCredential.id());

    return UpdatePasswordResult.SUCCESS;
  }

  @Override
  public UpdatePasswordResult changePassword(String email, UserChangePasswordRequest body) {
    logger.debug("[changePassword] {} email {} is updating", User.class.getSimpleName(), email);
    var updatedUserCredential =
        authService.updateUserPassword(email, body.oldPassword(), body.newPassword());

    logger.info(
        "[changePassword] {} id [{}] is updated",
        UserLogin.class.getSimpleName(),
        updatedUserCredential.id());

    return UpdatePasswordResult.SUCCESS;
  }

  @Override
  @Transactional
  public boolean deleteUserById(int id) {
    var userEntityName = User.class.getSimpleName();
    logger.debug("[deleteUser] {} id [{}] is deleting", userEntityName, id);
    var userToDelete =
        userRepository
            .findById(id)
            .orElseThrow(errorHelper.entityNotFound("deleteUser", User.class, id));

    authService.deleteUserCredentialByUserId(id);
    walletService.deleteConsumerWalletByUserId(id);
    userRepository.delete(userToDelete);

    logger.info("[deleteUser] {} id [{}] is deleted", userEntityName, id);

    return true;
  }

  private Set<Integer> buildUniqueUserIds(Page<User> userPage) {
    return userPage.stream().map(User::id).collect(Collectors.toSet());
  }

  private Map<Integer, UserLogin> buildUserIdToCredentialMap(Page<User> userPage) {
    var userIds = buildUniqueUserIds(userPage);

    return authService.findUserCredentialsByUserIds(userIds).stream()
        .collect(Collectors.toMap(cred -> cred.userId().getId(), Function.identity()));
  }

  private UserInfoResponse toResponseWithUserCredential(User user) {
    var userCredential = authService.findUserCredentialByUserId(user.id());

    return UserInfoResponse.fromUserDao(user, userCredential);
  }

  private void throwExceptionIfUserEmailAlreadyExists(String email) {
    var existingUserCredential = authService.findUserCredentialByEmail(email);
    if (existingUserCredential.isPresent()) {
      logger.warn("[register] {} email [{}] already exists", User.class.getSimpleName(), email);
      throw new CredentialExistsException(
          String.format("%s email [%s] already exists", User.class.getSimpleName(), email));
    }
  }
}
