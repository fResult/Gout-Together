package dev.fResult.goutTogether.users.services;

import dev.fResult.goutTogether.users.entities.User;

import java.util.List;

public interface UserService {
  List<User> getUsers();

  User getUserById(int id);

  User register(User user);

  User updateUser(int id, User user);

  void deleteUser(int id);
}
