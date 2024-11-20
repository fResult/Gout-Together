package dev.fResult.goutTogether.wallets;

import dev.fResult.goutTogether.users.entities.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("user_wallets")
public record UserWallet(
    @Id Integer id,
    AggregateReference<User, Integer> userId,
    Instant lastUpdated,
    BigDecimal balance) {
  public static UserWallet of(
      Integer id,
      AggregateReference<User, Integer> userId,
      Instant lastUpdated,
      BigDecimal balance) {
    return new UserWallet(id, userId, lastUpdated, balance);
  }
}
