package dev.fResult.goutTogether.transactions;

import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.users.entities.User;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("transactions")
public record Transaction(
    @Id Integer id,
    AggregateReference<User, Integer> userId,
    AggregateReference<TourCompany, Integer> tourCompanyId,
    Instant transactionDate,
    BigDecimal amount,
    TransactionType type,
    String idempotentKey) {

  public static Transaction of(
      Integer id,
      AggregateReference<User, Integer> userId,
      AggregateReference<TourCompany, Integer> tourCompanyId,
      Instant transactionDate,
      BigDecimal amount,
      TransactionType type,
      String idempotentKey) {

    return new Transaction(id, userId, tourCompanyId, transactionDate, amount, type, idempotentKey);
  }
}
