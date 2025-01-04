package dev.fResult.goutTogether.users.repositories;

import dev.fResult.goutTogether.users.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ListCrudRepository<User, Integer> {
  Page<User> findByFirstNameContaining(String firstName, Pageable pageable);
}
