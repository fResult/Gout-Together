package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.entities.UserLogin;

import java.util.*;

public interface AuthService {
  List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds);

  Optional<UserLogin> findUserCredentialByEmail(String userEmail);

  UserLogin findUserCredentialByUserId(Integer id);

  UserLogin createUserLogin(int userId, String email, String password);
}
