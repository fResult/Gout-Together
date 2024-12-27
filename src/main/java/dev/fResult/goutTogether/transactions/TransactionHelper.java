package dev.fResult.goutTogether.transactions;

import dev.fResult.goutTogether.common.enumurations.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

public class TransactionHelper {
  public static Transaction buildTopUpTransaction(
      Integer userId, Integer bookingId, BigDecimal amount, String idempotentKey) {

    return Transaction.of(
        null,
        AggregateReference.to(userId),
        null,
        null,
        Instant.now(),
        amount,
        TransactionType.TOP_UP,
        idempotentKey);
  }

  public static Transaction buildBookingTransaction(
      String idempotentKey,
      Integer userId,
      Integer bookingId,
      Integer tourCompanyId,
      BigDecimal amount) {

    return Transaction.of(
        null,
        AggregateReference.to(userId),
        AggregateReference.to(tourCompanyId),
        AggregateReference.to(bookingId),
        Instant.now(),
        amount,
        TransactionType.BOOKING,
        idempotentKey);
  }

  public static Transaction buildRefundTransaction(
      String idempotentKey,
      Integer userId,
      Integer bookingId,
      Integer tourCompanyId,
      BigDecimal amount) {

    return Transaction.of(
        null,
        AggregateReference.to(userId),
        AggregateReference.to(tourCompanyId),
        AggregateReference.to(bookingId),
        Instant.now(),
        amount,
        TransactionType.REFUND,
        idempotentKey);
  }
}
