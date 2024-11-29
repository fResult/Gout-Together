package dev.fResult.goutTogether.auths.dtos;

import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthenticatedUser(int userId, String email, String password, UserRoleName roleName)
    implements UserDetails {

  public static AuthenticatedUser of(
      int userId, String email, String password, UserRoleName roleName) {

    return new AuthenticatedUser(userId, email, password, roleName);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return switch (roleName) {
      case UserRoleName.ADMIN -> List.of(new SimpleGrantedAuthority(UserRoleName.ADMIN.name()));
      default -> List.of(new SimpleGrantedAuthority(UserRoleName.CONSUMER.name()));
    };
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }
}
