package dev.fResult.goutTogether.auths.entities;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("tour_company_logins")
public record TourCompanyLogin(
    @Id Integer id,
    AggregateReference<TourCompany, Integer> tourCompanyId,
    String username,
    String password) {
  public static TourCompanyLogin of(
      Integer id,
      AggregateReference<TourCompany, Integer> tourCompanyId,
      String username,
      String password) {
    return new TourCompanyLogin(id, tourCompanyId, username, password);
  }
}
