package dev.fResult.goutTogether.helpers;

import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

public class AggregateReferenceToUpdateBuilder {
  public static <T, ID> AggregateReference<T, ID> build(
      ID idFromRequest, AggregateReference<T, ID> aggregateRefIdFromDb) {
    return Optional.ofNullable(idFromRequest)
        .map(AggregateReference::<T, ID>to)
        .orElse(AggregateReference.to(Objects.requireNonNull(aggregateRefIdFromDb.getId())));
  }
}
