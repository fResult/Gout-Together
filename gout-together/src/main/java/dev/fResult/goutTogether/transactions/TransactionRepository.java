package dev.fResult.goutTogether.transactions;

import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface TransactionRepository extends ListCrudRepository<Transaction, Integer> {
  Optional<Transaction> findOneByIdempotentKey(String idempotentKey);
}
