package dev.fResult.goutTogether.tours.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table("tour_counts")
public record TourCount(@Id Integer id, AggregateReference<Tour, Integer> tourId, int amount) {
  public static TourCount of(Integer id, AggregateReference<Tour, Integer> tourId, int amount) {
    return new TourCount(id, tourId, amount);
  }

  public TourCount increaseAmount(int amountToAdd) {
    return new TourCount(id, tourId, amount + amountToAdd);
  }
}
