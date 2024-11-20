package dev.fResult.goutTogether.users.repositories;

import dev.fResult.goutTogether.users.entities.User;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ListCrudRepository<User, Integer> {}
