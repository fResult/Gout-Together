package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import dev.fResult.goutTogether.auths.repositories.UserLoginRepository;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import dev.fResult.goutTogether.common.helpers.ErrorHelper;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.tourCompanies.repositories.TourCompanyLoginRepository;
import dev.fResult.goutTogether.users.entities.User;
import dev.fResult.goutTogether.users.repositories.UserRoleRepository;
import java.util.Objects;
import java.util.function.Predicate;
import org.apache.commons.validator.routines.EmailValidator;
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
  private final TourCompanyLoginRepository tourCompanyLoginRepository;

  public CustomUserDetailsService(
      UserLoginRepository userLoginRepository,
      UserRoleRepository userRoleRepository,
      TourCompanyLoginRepository tourCompanyLoginRepository) {

    this.userLoginRepository = userLoginRepository;
    this.userRoleRepository = userRoleRepository;
    this.tourCompanyLoginRepository = tourCompanyLoginRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.debug(
        "[loadUserByUsername] {} username {} is loading",
        UserLogin.class.getSimpleName(),
        username);

    if (isEmail(username)) return loginUser(username);
    return loginTourCompany(username);
  }

  private boolean isEmail(String username) {
    return EmailValidator.getInstance().isValid(username);
  }

  private AuthenticatedUser loginUser(String email) {
    logger.debug("[loginUser] {} is logging in", User.class.getSimpleName());
    var userLogin =
        userLoginRepository
            .findOneByEmail(email)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "loginUser", UserLogin.class, "email", email));

    var userId = Objects.requireNonNull(userLogin.userId().getId());

    var userRole =
        userRoleRepository
            .findOneByUserId(AggregateReference.to(userId))
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "loginUser", UserLogin.class, "userId", String.valueOf(userId)));

    Predicate<Integer> isAdmin =
        roleId -> Objects.requireNonNull(roleId) == UserRoleName.ADMIN.getId();

    var role = isAdmin.test(userRole.roleId().getId()) ? UserRoleName.ADMIN : UserRoleName.CONSUMER;

    return AuthenticatedUser.of(userId, userLogin.email(), userLogin.password(), role);
  }

  private AuthenticatedUser loginTourCompany(String username) {
    logger.debug("[loginTourCompany] {} is logging in", TourCompany.class.getSimpleName());
    var companyCredential =
        tourCompanyLoginRepository
            .findOneByUsername(username)
            .orElseThrow(
                errorHelper.entityWithSubResourceNotFound(
                    "loginTourCompany", TourCompanyLogin.class, "username", username));

    return AuthenticatedUser.of(
        companyCredential.id(),
        companyCredential.username(),
        companyCredential.password(),
        UserRoleName.COMPANY);
  }
}
