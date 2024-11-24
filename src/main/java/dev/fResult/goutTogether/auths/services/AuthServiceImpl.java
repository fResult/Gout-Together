package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.UserLoginRepository;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
  private final PasswordEncoder passwordEncoder;

  public AuthServiceImpl(UserLoginRepository userLoginRepository, PasswordEncoder passwordEncoder) {
    this.userLoginRepository = userLoginRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds) {
    logger.debug(
        "[findUserCredentialsByUserIds] Finding {}s by userIds: {}", UserLogin.class, userIds);

    return userLoginRepository.findAllById(userIds);
  }

  @Override
  public Optional<UserLogin> findUserCredentialByEmail(String userEmail) {
    return userLoginRepository.findOneByEmail(userEmail);
  }

  @Override
  public UserLogin createUserLogin(int userId, String email, String password) {
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
  public UserLogin findUserCredentialByUserId(Integer id) {
    logger.debug("[findUserCredentialByUserId] Finding {} by id: {}", UserLogin.class, id);

    return userLoginRepository
        .findById(id)
        .orElseThrow(errorHelper.entityNotFound("findUserCredentialByUserId", UserLogin.class, id));
  }
}
