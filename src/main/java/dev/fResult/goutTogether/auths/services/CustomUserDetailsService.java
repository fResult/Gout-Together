package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.helpers.ErrorHelper;
import dev.fResult.goutTogether.users.repositories.UserRoleRepository;
import java.util.Objects;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
  private final ErrorHelper errorHelper = new ErrorHelper(CustomUserDetailsService.class);

  private final UserLoginRepository userLoginRepository;
  private final UserRoleRepository userRoleRepository;

  public CustomUserDetailsService(
      UserLoginRepository userLoginRepository, UserRoleRepository userRoleRepository) {

    this.userLoginRepository = userLoginRepository;
    this.userRoleRepository = userRoleRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug(
        "[loadUserByUsername] {} username {} is loading",
        UserLogin.class.getSimpleName(),
        username);
    var userLogin =
        userLoginRepository
            .findOneByEmail(username)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "loadUserByUsername", UserLogin.class, "email", username));

    var userId = Objects.requireNonNull(userLogin.userId().getId());

    var userRole =
        userRoleRepository
            .findOneByUserId(AggregateReference.to(userId))
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "loadUserByUsername", UserLogin.class, "userId", String.valueOf(userId)));

    Predicate<Integer> isAdmin =
        roleId -> Objects.requireNonNull(roleId) == UserRoleName.ADMIN.getId();

    var role = isAdmin.test(userRole.roleId().getId()) ? UserRoleName.ADMIN : UserRoleName.CONSUMER;

    return AuthenticatedUser.of(userId, userLogin.email(), userLogin.password(), role);
  }
}
