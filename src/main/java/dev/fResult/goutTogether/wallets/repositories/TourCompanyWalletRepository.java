package dev.fResult.goutTogether.wallets.repositories;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.sql.LockMode;
import org.springframework.data.relational.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourCompanyWalletRepository extends CrudRepository<TourCompanyWallet, Integer> {
  @Lock(LockMode.PESSIMISTIC_WRITE)
  Optional<TourCompanyWallet> findOneByTourCompanyId(
      AggregateReference<TourCompany, Integer> tourCompanyId);
}
