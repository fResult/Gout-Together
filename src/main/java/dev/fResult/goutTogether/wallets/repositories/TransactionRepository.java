package dev.fResult.goutTogether.wallets.repositories;

import dev.fResult.goutTogether.wallets.entities.Transaction;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface TransactionRepository extends ListCrudRepository<Transaction, Integer> {
    Optional<Transaction> findByIdempotentKey(String idempotentKey);
}
