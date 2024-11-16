package dev.fResult.goutTogether.tourCompanies.repositories;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompanyWallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourCompanyWalletRepository extends CrudRepository<TourCompanyWallet, Integer> {
}
