package dev.fResult.goutTogether.helpers;

import java.util.Objects;
import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

import static org.springframework.util.StringUtils.capitalize;

public class AggregateReferenceToUpdateBuilder {
  public static <T, ID> AggregateReference<T, ID> build(
      ID idFromRequest, AggregateReference<T, ID> aggregateRefIdFromDb, String resourceName) {
    var existingId =
        Optional.ofNullable(aggregateRefIdFromDb.getId())
            .orElseThrow(
                () ->
                    new RuntimeException(String.format("%s id is null", capitalize(resourceName))));

    return Optional.ofNullable(idFromRequest)
        .map(AggregateReference::<T, ID>to)
        .orElse(AggregateReference.to(existingId));
  }
}
