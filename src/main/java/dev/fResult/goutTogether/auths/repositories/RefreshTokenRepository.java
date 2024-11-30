package dev.fResult.goutTogether.auths.repositories;

import dev.fResult.goutTogether.auths.entities.RefreshToken;
import org.springframework.data.repository.ListCrudRepository;

public interface RefreshTokenRepository extends ListCrudRepository<RefreshToken, Integer> {}
