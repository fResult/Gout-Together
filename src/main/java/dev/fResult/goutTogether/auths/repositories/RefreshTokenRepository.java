package dev.fResult.goutTogether.auths.repositories;

import dev.fResult.goutTogether.auths.entities.RefreshToken;
import dev.fResult.goutTogether.common.enumurations.UserRoleName;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

public interface RefreshTokenRepository extends ListCrudRepository<RefreshToken, Integer> {
  Optional<RefreshToken> findOneByToken(String token);

  @Modifying
  @Query(
      """
      UPDATE refresh_tokens
      SET is_expired = :isExpired
      WHERE usage = :usage AND resource_id = :resourceId;
      """)
  void updateRefreshTokenByResource(UserRoleName usage, int resourceId, boolean isExpired);

  @Modifying
  @Query(
      """
      UPDATE refresh_tokens
      SET is_expired = :isExpired
      WHERE is_expired = false AND issued_date <= :thresholdDate;
      """)
  void updateRefreshTokenThatExpired(boolean isExpired, Instant thresholdDate);
}
