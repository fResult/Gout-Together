package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.entities.UserLogin;

public interface AuthService {
  UserLogin findUserCredentialByEmail(String email);
}
