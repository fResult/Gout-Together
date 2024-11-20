package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.UserLogin;

public interface AuthService {
  UserLogin findUserCredentialByEmail(String email);
}
