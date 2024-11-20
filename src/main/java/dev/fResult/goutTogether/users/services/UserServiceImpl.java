package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.AuthService;
import dev.fResult.goutTogether.auths.UserForgotPasswordRequest;
import dev.fResult.goutTogether.common.enumurations.UpdatePasswordResult;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.users.dtos.UserInfoResponse;
import dev.fResult.goutTogether.users.dtos.UserRegistrationRequest;
import dev.fResult.goutTogether.users.dtos.UserUpdateRequest;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
  public List<User> getUsers() {
    return userRepository.findAll();
  }

  @Override
  public UserInfoResponse getUserById(int id) {
    return userRepository
        .findById(id)
        .map(UserInfoResponse::fromDao)
        .orElseThrow(errorHelper.entityNotFound("getUserById", User.class, 888));
  }

  // TODO: Create User + Credential + Wallet
  @Override
  public UserInfoResponse register(UserRegistrationRequest body) {
    var userToRegister = User.of(null, body.firstName(), body.lastName(), body.phoneNumber());
    var registeredUser = userRepository.save(userToRegister);
    logger.info("[register] new user: {} is registered", registeredUser);

    return Optional.of(registeredUser).map(UserInfoResponse::fromDao).get();
  }

  @Override
  public UserInfoResponse updateUser(int id, UserUpdateRequest body) {
    var bodyToUserUpdate = toUserUpdate(body);
    var userToUpdate =
        userRepository
            .findById(id)
            .map(bodyToUserUpdate)
            .orElseThrow(errorHelper.entityNotFound("updateUser", User.class, id));
    var updatedUser = userRepository.save(userToUpdate);
    logger.info("[updateUser] user: {} is updated", updatedUser);

    return Optional.of(updatedUser).map(UserInfoResponse::fromDao).get();
  }

  // TODO: Delete User + Credential + Wallet (Cascade)
  @Override
  public void deleteUser(int id) {
    userRepository.deleteById(id);
  }

  @Override
  public UpdatePasswordResult changePassword(UserForgotPasswordRequest body) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  // FIXME: rename this method to be easier to understand
  private Function<User, User> toUserUpdate(UserUpdateRequest body) {
    return user ->
        User.of(
            user.id(),
            Optional.ofNullable(body.firstName()).orElse(user.firstName()),
            Optional.ofNullable(body.lastName()).orElse(user.lastName()),
            Optional.ofNullable(body.phoneNumber()).orElse(user.phoneNumber()));
  }
}
