package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.auths.AuthService;
import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import dev.fResult.goutTogether.wallets.services.WalletService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

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
  public User getUserById(int id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> {
              logger.error("[getUserById] User with id {} not found", id);
              return new EntityNotFound(String.format("User id [%s] not found", id));
            });
  }

  // TODO: Create User + Credential + Wallet
  @Override
  public User register(User user) {
    return userRepository.save(user);
  }

  @Override
  public User updateUser(int id, User user) {
    var userToUpdate = getUserById(id);
    return userRepository.save(userToUpdate);
  }

  // TODO: Delete User + Credential + Wallet (Cascade)
  @Override
  public void deleteUser(int id) {
    var userToDelete = getUserById(id);
    userRepository.delete(userToDelete);
  }
}
