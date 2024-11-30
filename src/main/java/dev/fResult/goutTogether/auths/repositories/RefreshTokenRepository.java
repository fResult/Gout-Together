package dev.fResult.goutTogether.auths.repositories;

import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface RefreshTokenRepository extends ListCrudRepository<RefreshToken, Integer> {
  @Modifying
  @Query(
      """
      UPDATE refresh_tokens
      SET is_expired = :isExpired
      WHERE usage = :usage AND resource_id = :resourceId;
      """)
  void updateRefreshTokenByResource(UserRoleName usage, int resourceId, boolean isExpired);
}
