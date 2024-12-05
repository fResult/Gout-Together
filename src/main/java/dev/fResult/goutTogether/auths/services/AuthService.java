package dev.fResult.goutTogether.auths.services;

import dev.fResult.goutTogether.auths.dtos.AuthenticatedUser;
import dev.fResult.goutTogether.auths.dtos.LoginRequest;
import dev.fResult.goutTogether.auths.dtos.LoginResponse;
import dev.fResult.goutTogether.auths.dtos.LogoutInfo;
import dev.fResult.goutTogether.auths.dtos.RefreshTokenRequest;
import dev.fResult.goutTogether.auths.entities.TourCompanyLogin;
import dev.fResult.goutTogether.auths.entities.UserLogin;
import java.util.*;

public interface AuthService {
  List<UserLogin> findUserCredentialsByUserIds(Collection<Integer> userIds);

  UserLogin findUserCredentialByUserId(int userId);

  Optional<UserLogin> findUserCredentialByEmail(String userEmail);

  Optional<UserLogin> findUserCredentialByEmailAndPassword(String userEmail, String password);

  UserLogin createUserCredential(int userId, String email, String password);

  UserLogin updateUserPassword(String email, String oldPassword, String newPassword);

  boolean deleteUserCredentialByUserId(int userId);

  Optional<TourCompanyLogin> findTourCompanyCredentialByUsername(String username);

  TourCompanyLogin findTourCompanyCredentialByTourCompanyId(int id);

  TourCompanyLogin createTourCompanyLogin(int tourCompanyId, String username, String password);

  boolean deleteTourCompanyLoginByTourCompanyId(int tourCompanyId);

  LoginResponse login(LoginRequest body);

  LoginResponse refreshToken(RefreshTokenRequest body);

  boolean logout(AuthenticatedUser authenticatedUser);

  boolean logout(LogoutInfo logoutInfo);
}
