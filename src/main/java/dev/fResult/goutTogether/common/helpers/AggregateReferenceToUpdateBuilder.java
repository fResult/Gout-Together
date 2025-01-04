package dev.fResult.goutTogether.common.helpers;

import static org.springframework.util.StringUtils.capitalize;

import java.util.Optional;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

public class AggregateReferenceToUpdateBuilder {

  public static <T, ID> AggregateReference<T, ID> build(
      ID idFromRequest, AggregateReference<T, ID> aggregateRefIdFromDb, String resourceName) {
    final var existingId =
        Optional.ofNullable(aggregateRefIdFromDb.getId())
            .orElseThrow(
                () ->
                    new RuntimeException(String.format("%s id is null", capitalize(resourceName))));

    return Optional.ofNullable(idFromRequest)
        .map(AggregateReference::<T, ID>to)
        .orElse(AggregateReference.to(existingId));
  }
}
