package dev.fResult.goutTogether.wallets.repositories;

import dev.fResult.goutTogether.wallets.entities.TourCompanyWallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourCompanyWalletRepository extends CrudRepository<TourCompanyWallet, Integer> {}
