package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.common.exceptions.EntityNotFound;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
  private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
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

  @Override
  public User register(User user) {
    return userRepository.save(user);
  }
}
