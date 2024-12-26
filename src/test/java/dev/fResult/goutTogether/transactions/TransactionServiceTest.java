package dev.fResult.goutTogether.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.fResult.goutTogether.common.enumurations.TransactionType;
import dev.fResult.goutTogether.common.utils.UUIDV7;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
  @InjectMocks private TransactionService transactionService;

  @Mock private TransactionRepository transactionRepository;

  @Test
  void whenCreateTransaction_thenSuccess() {
    // Arrange
    final var USER_ID = 1;
    final var TOUR_COMPANY_ID = 1;
    final var BOOKING_ID = 1;
    final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();
    var transactionInput =
        Transaction.of(
            null,
            AggregateReference.to(USER_ID),
            AggregateReference.to(TOUR_COMPANY_ID),
            AggregateReference.to(BOOKING_ID),
            Instant.now(),
            BigDecimal.valueOf(1200),
            TransactionType.TOP_UP,
            IDEMPOTENT_KEY);
    var createdTransaction =
        Transaction.of(
            1,
            AggregateReference.to(USER_ID),
            AggregateReference.to(TOUR_COMPANY_ID),
            AggregateReference.to(BOOKING_ID),
            Instant.now(),
            BigDecimal.valueOf(1200),
            TransactionType.TOP_UP,
            IDEMPOTENT_KEY);

    when(transactionRepository.save(any(Transaction.class))).thenReturn(createdTransaction);

    // Actual
    var actualCreatedTransaction = transactionService.createTransaction(transactionInput);

    // Assert
    assertEquals(createdTransaction, actualCreatedTransaction);
  }
}
