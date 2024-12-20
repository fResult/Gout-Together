package dev.fResult.goutTogether.auths.entities;

import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("refresh_tokens")
public record RefreshToken(
    @Id Integer id,
    String token,
    Instant issuedDate,
    UserRoleName usage,
    int resourceId,
    boolean isExpired) {

  public static RefreshToken of(
      Integer id,
      String token,
      Instant issuedDate,
      UserRoleName usage,
      int resourceId,
      boolean isExpired) {

    return new RefreshToken(id, token, issuedDate, usage, resourceId, isExpired);
  }
}
