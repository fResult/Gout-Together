package dev.fResult.goutTogether.tours;

import dev.fResult.goutTogether.enumurations.TourStatus;
import dev.fResult.goutTogether.tourCompanies.entities.TourCompany;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("tours")
public record Tour(
    @Id Integer id,
    AggregateReference<TourCompany, Integer> tourCompanyId,
    String title,
    String description,
    String location,
    int numberOfPeople,
    Instant activityDate,
    TourStatus status) {

  public static Tour of(
      Integer id,
      AggregateReference<TourCompany, Integer> tourCompanyId,
      String title,
      String description,
      String location,
      int numberOfPeople,
      Instant activityDate,
      TourStatus status) {
    return new Tour(
        id, tourCompanyId, title, description, location, numberOfPeople, activityDate, status);
  }
}
