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

  private Transaction buildTransaction(Integer id) {
    final var IDEMPOTENT_KEY = UUIDV7.randomUUID().toString();

    return Transaction.of(
        id,
        AggregateReference.to(1),
        AggregateReference.to(1),
        AggregateReference.to(1),
        Instant.now(),
        BigDecimal.valueOf(1200),
        TransactionType.TOP_UP,
        IDEMPOTENT_KEY);
  }

  @Test
  void whenCreateTransaction_thenSuccess() {
    // Arrange
    final var TRANSACTION_ID = 1;
    final var transactionToCreate = buildTransaction(null);
    final var createdTransaction = buildTransaction(TRANSACTION_ID);

    when(transactionRepository.save(any(Transaction.class))).thenReturn(createdTransaction);

    // Actual
    final var actualCreatedTransaction = transactionService.createTransaction(transactionToCreate);

    // Assert
    assertEquals(createdTransaction, actualCreatedTransaction);
  }
}
