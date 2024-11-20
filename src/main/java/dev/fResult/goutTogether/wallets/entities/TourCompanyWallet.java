package dev.fResult.goutTogether.wallets.entities;

import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("tour_company_wallets")
public record TourCompanyWallet(
    @Id Integer id,
    AggregateReference<TourCompany, Integer> tourCompanyId,
    Instant lastUpdated,
    BigDecimal balance) {
  public static TourCompanyWallet of(
      Integer id,
      AggregateReference<TourCompany, Integer> tourCompanyId,
      Instant lastUpdated,
      BigDecimal balance) {
    return new TourCompanyWallet(id, tourCompanyId, lastUpdated, balance);
  }
}
