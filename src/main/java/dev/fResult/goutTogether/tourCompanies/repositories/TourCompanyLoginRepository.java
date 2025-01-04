package dev.fResult.goutTogether.tourCompanies.repositories;

import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourCompanyLoginRepository extends CrudRepository<TourCompanyLogin, Integer> {
  Optional<TourCompanyLogin> findOneByUsername(String username);

  Optional<TourCompanyLogin> findOneByTourCompanyId(
      AggregateReference<TourCompany, Integer> tourCompanyId);
}
