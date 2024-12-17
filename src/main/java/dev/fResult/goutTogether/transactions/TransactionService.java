package dev.fResult.goutTogether.transactions;

import dev.fResult.goutTogether.common.ResponseAdviceHandler;
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
    logger.debug("[createTransaction] Creating transaction: {}", transaction);

    var createdTransaction = transactionRepository.save(transaction);
    logger.info("[createTransaction] Transaction created: {}", createdTransaction);

    return createdTransaction;
  }
}
