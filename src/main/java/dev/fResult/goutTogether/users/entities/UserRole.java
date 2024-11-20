package dev.fResult.goutTogether.users.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_roles")
public record UserRole(
    @Id Integer id,
    AggregateReference<User, Integer> userId,
    AggregateReference<Role, Integer> roleId) {
  public static UserRole of(
      Integer id,
      AggregateReference<User, Integer> userId,
      AggregateReference<Role, Integer> roleId) {
    return new UserRole(id, userId, roleId);
  }
}
