package dev.fResult.goutTogether.tourCompanies.repositories;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompanyLogin;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourCompanyLoginRepository extends CrudRepository<TourCompanyLogin, Integer> {}
