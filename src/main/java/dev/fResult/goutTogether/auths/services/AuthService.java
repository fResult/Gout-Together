package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import java.util.*;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {
  List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds);

  UserLogin findUserCredentialByUserId(int userId);

  Optional<UserLogin> findUserCredentialByEmail(String userEmail);

  UserLogin createUserCredential(int userId, String email, String password);

  boolean deleteUserCredentialByUserId(int userId);

  Optional<TourCompanyLogin> findTourCompanyCredentialByUsername(String username);

  TourCompanyLogin findTourCompanyCredentialByTourCompanyId(int id);

  TourCompanyLogin createTourCompanyLogin(int tourCompanyId, String username, String password);

  boolean deleteTourCompanyLoginByTourCompanyId(int tourCompanyId);

  LoginResponse login(LoginRequest body);

  boolean logout(AuthenticatedUser authenticatedUser);

  boolean logout(Jwt jwt);
}
