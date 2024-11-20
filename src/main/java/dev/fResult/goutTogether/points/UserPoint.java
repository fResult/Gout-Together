package dev.fResult.goutTogether.points;

import dev.fResult.goutTogether.users.entities.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("user_points")
public record UserPoint(
    @Id Integer id,
    AggregateReference<User, Integer> userId,
    Instant lastUpdated,
    Integer balance) {
  public static UserPoint of(
      Integer id, AggregateReference<User, Integer> userId, Instant lastUpdated, Integer balance) {
    return new UserPoint(id, userId, lastUpdated, balance);
  }
}
