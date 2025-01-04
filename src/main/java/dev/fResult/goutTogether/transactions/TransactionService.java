package dev.fResult.goutTogether.transactions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
  private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
  private final TransactionRepository transactionRepository;

  public TransactionService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public Transaction createTransaction(Transaction transaction) {
    logger.debug(
        "[createTransaction] Creating {}: {}", Transaction.class.getSimpleName(), transaction);

    final var createdTransaction = transactionRepository.save(transaction);
    logger.info(
        "[createTransaction] New {} is created: {}",
        Transaction.class.getSimpleName(),
        createdTransaction);

    return createdTransaction;
  }
}
