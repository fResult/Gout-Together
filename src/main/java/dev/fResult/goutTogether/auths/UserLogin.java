package dev.fResult.goutTogether.auths;

import dev.fResult.goutTogether.users.entities.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_logins")
public record UserLogin(
    @Id Integer id, AggregateReference<User, Integer> userId, String email, String password) {
  public static UserLogin of(
      Integer id, AggregateReference<User, Integer> userId, String email, String password) {
    return new UserLogin(id, userId, email, password);
  }
}
